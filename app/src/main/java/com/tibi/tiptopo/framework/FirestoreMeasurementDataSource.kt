package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction.DESCENDING
import com.google.firebase.firestore.Source
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

        val measurements = mutableListOf<Measurement>()

        val query = result.orderBy("date", DESCENDING)

        query.get(Source.CACHE)
            .addOnFailureListener {
                trySend(Resource.Failure(it))
                return@addOnFailureListener
            }
            .addOnSuccessListener { cashSnapshot ->
                if (cashSnapshot.isEmpty) {
                    query.get(Source.SERVER)
                        .addOnFailureListener {
                            trySend(Resource.Failure(it))
                            return@addOnFailureListener
                        }
                        .addOnSuccessListener { serverSnapshot ->
                            if (serverSnapshot.isEmpty) {
                                //emit empty list
                                trySend(Resource.Success(measurements))
                            } else {
                                //emit all data from server
                                trySend(Resource.Success(serverSnapshot
                                    .map { it.toObject<Measurement>() }))
                            }
                        }
                } else {
                    val latestCache = cashSnapshot.first().data["date"]
                    if (latestCache != null) {
                        measurements += cashSnapshot.map { it.toObject<Measurement>() }.toList()

                        result.orderBy("date")
                            .whereGreaterThan("date", latestCache)
                            .get(Source.SERVER)
                            .addOnFailureListener {
                                //emit all data from cache
                                trySend(Resource.Success(measurements))
                                return@addOnFailureListener
                            }
                            .addOnSuccessListener { serverList ->
                                if (serverList.isEmpty) {
                                    //emit all data from cache
                                    trySend(Resource.Success(measurements))
                                } else {
                                    //emit data from cache and server
                                    serverList.forEach { snapshot ->
                                        val serverMeasurement = snapshot.toObject<Measurement>()
                                        val cacheMeasurement = measurements
                                            .find { it.id == serverMeasurement.id }
                                        if (cacheMeasurement == null) {
                                            measurements += serverMeasurement
                                        } else {
                                            measurements[measurements.indexOf(cacheMeasurement)] =
                                                serverMeasurement
                                        }
                                    }
                                    trySend(Resource.Success(measurements))
                                }
                            }
                    }
                }
            }
        awaitClose()
    }

    override suspend fun deleteMeasurement(measurementId: String) {
        firestore.collection(path).document(measurementId).delete()
    }
}
