package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.project.ProjectDataSource
import com.tibi.tiptopo.domain.Project
import com.tibi.tiptopo.presentation.getAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreProjectDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProjectDataSource {

    private val path = "users/${Firebase.auth.currentUser?.uid}/projects"

    override suspend fun addProject(project: Project): Resource<Project> {
        val doc = firestore.collection(path)
            .document()
        project.id = doc.id
        doc.set(project).await()
        return Resource.Success(project)
    }

    override suspend fun getProject(projectId: String): Resource<Project> {
        val result = firestore.collection(path)
            .document(projectId)
            .get()
            .await()
            .toObject<Project>()
        requireNotNull(result)
        return Resource.Success(result)
    }

    override suspend fun updateProject(project: Project): Resource<Project> {
        val doc = firestore.collection(path)
            .document(project.id)
        doc.set(project).await()
        return Resource.Success(project)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getAllProjects() = firestore.collection(path).getAll<Project>()
}
