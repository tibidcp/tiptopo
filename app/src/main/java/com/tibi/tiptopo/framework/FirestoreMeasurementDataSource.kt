package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.measurement.MeasurementDataSource
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreMeasurementDataSource @Inject constructor(
    @CurrentProjectId private val projectId: String
) : MeasurementDataSource {

    private val firestore = Firebase.firestore
    private val path =
        "users/${Firebase.auth.currentUser?.uid}/projects/$projectId/measurements"

    override suspend fun addMeasurement(measurement: Measurement): Resource<Measurement> {
        val doc = firestore.collection(path)
            .document()
        measurement.id = doc.id
        measurement.date = System.currentTimeMillis()
        doc.set(measurement).await()
        return Resource.Success(measurement)
    }

    override suspend fun getMeasurement(measurementId: String): Resource<Measurement> {
        val result = firestore.collection(path)
            .document(measurementId)
            .get()
            .await()
            .toObject<Measurement>() ?: return Resource.Loading()
        return Resource.Success(result)
    }

    override suspend fun updateMeasurement(measurement: Measurement): Resource<Measurement> {
        firestore.collection(path)
            .document(measurement.id)
            .set(measurement)
            .await()
        return Resource.Success(measurement)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getAllMeasurements(): Flow<Resource<List<Measurement>>> = callbackFlow {
        val result = firestore
            .collection(path)
        val subscription = result.addSnapshotListener { snapshot, _ ->
            if (!snapshot!!.isEmpty) {
                val measurementList = snapshot.map { it.toObject<Measurement>() }.toList()
                trySend(Resource.Success(measurementList))
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun deleteMeasurement(measurementId: String) {
        firestore.collection(path).document(measurementId).delete()
    }
}
