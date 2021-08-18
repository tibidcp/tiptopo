package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.measurement.MeasurementDataSource
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import com.tibi.tiptopo.presentation.getAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreMeasurementDataSource @Inject constructor(
    @CurrentProjectId private val projectId: String,
    private val firestore: FirebaseFirestore
) : MeasurementDataSource {

    private val path =
        "users/${Firebase.auth.currentUser?.uid}/projects/$projectId/measurements"

    override suspend fun addMeasurement(measurement: Measurement): Resource<Measurement> {
        val doc = firestore.collection(path)
            .document()
        measurement.id = doc.id
        doc.set(measurement).await()
        return Resource.Success(measurement)
    }

    override suspend fun getMeasurement(measurementId: String): Resource<Measurement> {
        val result = firestore.collection(path)
            .document(measurementId)
            .get(Source.CACHE)
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
    override suspend fun getAllMeasurements() = firestore.collection(path).getAll<Measurement>()

    override suspend fun deleteMeasurement(measurementId: String) {
        firestore.collection(path).document(measurementId).delete()
    }
}
