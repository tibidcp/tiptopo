package com.tibi.tiptopo.data

import com.tibi.tiptopo.presentation.di.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MapRepository<T> @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataSource: MapDataSource<T>
) {

    suspend fun add(item: T) =
        withContext(ioDispatcher) { dataSource.add(item) }

    suspend fun get(id: String) =
        withContext(ioDispatcher) { dataSource.get(id) }

    suspend fun update(item: T) =
        withContext(ioDispatcher) { dataSource.update(item) }

    suspend fun getAll() = withContext(ioDispatcher) { dataSource.getAll() }
}
