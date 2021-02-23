package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.core.data.ProjectDataSource
import com.tibi.core.data.Resource
import com.tibi.core.domain.Project
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreProjectDataSource @Inject constructor() : ProjectDataSource {

    private val firestore = Firebase.firestore
    private val path = "users/${Firebase.auth.currentUser?.uid!!}/projects"

    override suspend fun addProject(project: Project): Resource<Project> {
        val doc = firestore.collection(path)
            .document()
        project.id = doc.id
        doc.set(project).await()
        return Resource.Success(project)
    }

    override suspend fun getProject(project: Project): Resource<Project> {
        val result = firestore.collection(path)
            .document(project.id)
            .get()
            .await()
        return Resource.Success(result.toObject()!!)
    }

    override suspend fun updateProject(project: Project) = addProject(project)

    override suspend fun getAllProjects(): Resource<List<Project>> {
        val result = firestore
            .collection(path)
            .get()
            .await()
            .map { it.toObject<Project>() }
            .toList()

        return Resource.Success(result)
    }
}
