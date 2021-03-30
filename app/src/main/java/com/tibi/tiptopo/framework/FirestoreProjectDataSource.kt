package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.MapDataSource
import com.tibi.tiptopo.domain.Project
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProjectDataSource @Inject constructor() : MapDataSource<Project> {

    private val firestore = Firebase.firestore
    private val path = "users/${Firebase.auth.currentUser?.uid}/projects"

    override suspend fun add(item: Project): Resource<Project> {
        val doc = firestore.collection(path)
            .document()
        if (item.id.isEmpty()) {
            item.id = doc.id
        }
        item.date = System.currentTimeMillis()
        doc.set(item).await()
        return Resource.Success(item)
    }

    override suspend fun get(id: String): Resource<Project> {
        val result = firestore.collection(path)
            .document(id)
            .get()
            .await()
        return Resource.Success(result.toObject()!!)
    }

    override suspend fun update(item: Project) = add(item)

    @ExperimentalCoroutinesApi
    override suspend fun getAll(): Flow<Resource<List<Project>>> = callbackFlow {
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
