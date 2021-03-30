package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.MapDataSource
import com.tibi.tiptopo.domain.Station
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreStationDataSource @Inject constructor() : MapDataSource<Station> {

    private val firestore = Firebase.firestore
    private val path = "users/${Firebase.auth.currentUser?.uid}/stations"

    override suspend fun add(item: Station): Resource<Station> {
        val doc = firestore.collection(path)
            .document()
        if (item.id.isEmpty()) {
            item.id = doc.id
        }
        doc.set(item).await()
        return Resource.Success(item)
    }

    override suspend fun get(id: String): Resource<Station> {
        val result = firestore.collection(path)
            .document(id)
            .get()
            .await()
        return Resource.Success(result.toObject()!!)
    }

    override suspend fun update(item: Station) = add(item)

    @ExperimentalCoroutinesApi
    override suspend fun getAll(): Flow<Resource<List<Station>>> = callbackFlow {
        val result = firestore
            .collection(path)
        val subscription = result.addSnapshotListener { snapshot, _ ->
            if (!snapshot!!.isEmpty) {
                val projectList = snapshot.map { it.toObject<Station>() }.toList()
                offer(Resource.Success(projectList))
            }
        }
        awaitClose { subscription.remove() }
    }
}
