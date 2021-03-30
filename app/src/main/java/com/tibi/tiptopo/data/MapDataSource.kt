package com.tibi.tiptopo.data

import kotlinx.coroutines.flow.Flow

interface MapDataSource<T> {
    suspend fun add(item: T): Resource<T>
    suspend fun get(id: String): Resource<T>
    suspend fun update(item: T): Resource<T>
    suspend fun getAll(): Flow<Resource<List<T>>>
}
