package com.tibi.tiptopo.data.station

import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.presentation.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StationRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataSource: StationDataSource
) {

    suspend fun addStation(station: Station) =
        withContext(ioDispatcher) { dataSource.addStation(station) }

    suspend fun getStation(stationId: String) =
        withContext(ioDispatcher) { dataSource.getStation(stationId) }

    suspend fun updateStation(station: Station) =
        withContext(ioDispatcher) { dataSource.updateStation(station) }

    suspend fun getAllStations() = withContext(ioDispatcher) { dataSource.getAllStations() }

    suspend fun getLastStation() = withContext(ioDispatcher) { dataSource.getLastStation() }
}
