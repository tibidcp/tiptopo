package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tibi.core.data.ProjectDataSource
import com.tibi.core.domain.Project
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreProjectDataSource @Inject constructor() : ProjectDataSource {

    private val firestore = Firebase.firestore
    private val path = "users/${Firebase.auth.currentUser?.uid!!}/projects"

    override suspend fun addProject(project: Project) {
        firestore.collection(path)
            .document(project.name)
            .set(project)
            .await()
    }

    override fun getProject(name: String): Flow<Project?> = flow {
    }

    override fun updateProject(project: Project) {
        TODO("Not yet implemented")
    }

    override fun removeProject(name: String) {
        TODO("Not yet implemented")
    }

    override fun getAllProjects(): Flow<Project> {
        TODO("Not yet implemented")
    }
}
