package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.station.StationDataSource
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import com.tibi.tiptopo.presentation.getAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreStationDataSource @Inject constructor(
    @CurrentProjectId private val projectId: String,
    private val firestore: FirebaseFirestore
) : StationDataSource {

    private val path = "users/${Firebase.auth.currentUser?.uid}/projects/$projectId/stations"

    override suspend fun addStation(station: Station): Resource<Station> {
        val doc = firestore.collection(path)
            .document()
        station.id = doc.id
        doc.set(station).await()
        return Resource.Success(station)
    }

    override suspend fun getStation(stationId: String): Resource<Station> {
        val result = firestore.collection(path)
            .document(stationId)
            .get()
            .await()
            .toObject<Station>() ?: return Resource.Loading()
        return Resource.Success(result)
    }

    override suspend fun updateStation(station: Station): Resource<Station> {
        firestore.collection(path)
            .document(station.id)
            .set(station)
            .await()
        return Resource.Success(station)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getAllStations() = firestore.collection(path).getAll<Station>()

    @ExperimentalCoroutinesApi
    override suspend fun getLastStation(): Flow<Resource<Station>> = callbackFlow {
        val result = firestore
            .collection(path)
        val query = result.orderBy("date", Query.Direction.DESCENDING).limit(1)

        query.get().addOnFailureListener {
            trySend(Resource.Failure(it))
            return@addOnFailureListener
        }.addOnSuccessListener { snapShot ->
            if (!snapShot.isEmpty) {
                val station = snapShot.first().toObject<Station>()
                trySend(Resource.Success(station))
            }
        }
        awaitClose()
    }
}
