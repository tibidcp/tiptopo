package com.tibi.tiptopo.framework

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.data.line.LineDataSource
import com.tibi.tiptopo.domain.Line
import com.tibi.tiptopo.presentation.di.CurrentProjectId
import com.tibi.tiptopo.presentation.getAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreLineDataSource @Inject constructor(
    @CurrentProjectId private val projectId: String,
    private val firestore: FirebaseFirestore
) : LineDataSource {

    private val path =
        "users/${Firebase.auth.currentUser?.uid}/projects/$projectId/lines"

    override suspend fun addLine(line: Line): Resource<Line> {
        val doc = firestore.collection(path)
            .document()
        line.id = doc.id
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
    override suspend fun getAllLines() = firestore.collection(path).getAll<Line>()

    override suspend fun deleteLine(lineId: String) {
        firestore.collection(path).document(lineId).delete()
    }
}
