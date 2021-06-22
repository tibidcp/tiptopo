package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.station.StationDataSource
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreStationDataSource @Inject constructor(
    @CurrentProjectId private val projectId: String
) : StationDataSource {

    private val firestore = Firebase.firestore
    private val path = "users/${Firebase.auth.currentUser?.uid}/projects/$projectId/stations"

    override suspend fun addStation(station: Station): Resource<Station> {
        val doc = firestore.collection(path)
            .document()
        station.id = doc.id
        station.date = System.currentTimeMillis()
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
    override suspend fun getAllStations(): Flow<Resource<List<Station>>> = callbackFlow {
        val result = firestore
            .collection(path)
        val subscription = result.addSnapshotListener { snapshot, _ ->
            if (!snapshot!!.isEmpty) {
                val stationList = snapshot.map { it.toObject<Station>() }.toList()
                offer(Resource.Success(stationList))
            }
        }
        awaitClose { subscription.remove() }
    }

    @ExperimentalCoroutinesApi
    override suspend fun getLastStation(): Flow<Resource<Station>> = callbackFlow {
        val result = firestore
            .collection(path)
        val subscription = result.addSnapshotListener { snapshot, _ ->
            if (!snapshot!!.isEmpty) {
                val station = snapshot
                    .map { it.toObject<Station>() }.maxByOrNull { it.date }
                if (station != null) {
                    trySend(Resource.Success(station))
                }
            }
        }
        awaitClose { subscription.remove() }
    }
}
