package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.line.LineDataSource
import com.tibi.tiptopo.domain.Line
import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreLineDataSource @Inject constructor(
    @CurrentProjectId private val projectId: String
) : LineDataSource {

    private val firestore = Firebase.firestore
    private val path =
        "users/${Firebase.auth.currentUser?.uid}/projects/$projectId/lines"

    override suspend fun addLine(line: Line): Resource<Line> {
        val doc = firestore.collection(path)
            .document()
        line.id = doc.id
        line.date = System.currentTimeMillis()
        doc.set(line).await()
        return Resource.Success(line)
    }

    override suspend fun getLine(lineId: String): Resource<Line> {
        val result = firestore.collection(path)
            .document(lineId)
            .get()
            .await()
            .toObject<Line>() ?: return Resource.Loading()
        return Resource.Success(result)
    }

    override suspend fun updateLine(line: Line): Resource<Line> {
        firestore.collection(path)
            .document(line.id)
            .set(line)
            .await()
        return Resource.Success(line)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getAllLines(): Flow<Resource<List<Line>>> = callbackFlow {
        val result = firestore
            .collection(path)
        val subscription = result.addSnapshotListener { snapshot, _ ->
            if (!snapshot!!.isEmpty) {
                val linetList = snapshot.map { it.toObject<Line>() }.toList()
                offer(Resource.Success(linetList))
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun deleteLine(lineId: String) {
        firestore.collection(path).document(lineId).delete()
    }
}