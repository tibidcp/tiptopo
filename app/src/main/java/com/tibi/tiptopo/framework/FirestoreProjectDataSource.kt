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
        firestore.collection(path)
            .document(project.name)
            .set(project)
            .await()
        return Resource.Success(project)
    }

    override suspend fun getProject(name: String): Resource<Project> {
        val result = firestore.collection(path)
            .document(name)
            .get()
            .await()
        return Resource.Success(result.toObject()!!)
    }

    override suspend fun updateProject(project: Project) = addProject(project)

    override suspend fun getAllProjects(): Resource<MutableList<Project>> {
        val result = firestore
            .collection(path)
            .get()
            .await()
            .map { it.toObject<Project>() }
            .toMutableList()

        return Resource.Success(result)
    }
}
