package com.tibi.tiptopo.presentation

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.FirestoreMembers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
suspend inline fun <reified T : FirestoreMembers> CollectionReference.getAll():
        Flow<Resource<List<T>>> {
    val firestoreCollection = this
    val result = mutableListOf<T>()
    val query = firestoreCollection.orderBy("date", Query.Direction.DESCENDING)

    return callbackFlow {
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
                                    trySend(Resource.Success(result))
                                } else {
                                    //emit all data from server
                                    result += serverSnapshot.map { it.toObject<T>() }.toList()
                                    trySend(Resource.Success(result))
                                }
                            }
                    } else {
                        val latestCache = cashSnapshot.first().data["date"]
                        if (latestCache != null) {
                            result += cashSnapshot.map { it.toObject<T>() }.toList()

                            firestoreCollection.orderBy("date")
                                .whereGreaterThan("date", latestCache)
                                .get(Source.SERVER)
                                .addOnFailureListener {
                                    //emit all data from cache
                                    trySend(Resource.Success(result))
                                    return@addOnFailureListener
                                }
                                .addOnSuccessListener { serverList ->
                                    if (serverList.isEmpty) {
                                        //emit all data from cache
                                        trySend(Resource.Success(result))
                                    } else {
                                        //emit data from cache and server
                                        serverList.forEach { snapshot ->
                                            val serverValue = snapshot.toObject<T>()
                                            val cacheValue = result
                                                .find { it.id == serverValue.id }
                                            if (cacheValue == null) {
                                                result += serverValue
                                            } else {
                                                result[result.indexOf(cacheValue)] =
                                                    serverValue
                                            }
                                        }
                                        trySend(Resource.Success(result))
                                    }
                                }
                        }
                    }
                }
            awaitClose()
    }
}