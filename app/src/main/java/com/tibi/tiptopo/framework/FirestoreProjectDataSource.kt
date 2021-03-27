package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectDataSource
import com.tibi.tiptopo.domain.Project
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProjectDataSource @Inject constructor() : ProjectDataSource {

    private val firestore = Firebase.firestore
    private val path = "users/${Firebase.auth.currentUser?.uid}/projects"

    override suspend fun addProject(project: Project): Resource<Project> {
        val doc = firestore.collection(path)
            .document()
        if (project.id.isEmpty()) {
            project.id = doc.id
        }
        project.date = System.currentTimeMillis()
        doc.set(project).await()
        return Resource.Success(project)
    }

    override suspend fun getProject(projectId: String): Resource<Project> {
        val result = firestore.collection(path)
            .document(projectId)
            .get()
            .await()
        return Resource.Success(result.toObject()!!)
    }

    override suspend fun updateProject(project: Project) = addProject(project)

    @ExperimentalCoroutinesApi
    override suspend fun getAllProjects(): Flow<Resource<List<Project>>> = callbackFlow {
        val result = firestore
            .collection(path)
        val subscription = result.addSnapshotListener { snapshot, _ ->
            if (!snapshot!!.isEmpty) {
                val projectList = snapshot.map { it.toObject<Project>() }.toList()
                offer(Resource.Success(projectList))
            }
        }
        awaitClose { subscription.remove() }
    }
}
