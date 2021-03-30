package com.tibi.tiptopo.data.station

import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Station
import kotlinx.coroutines.flow.Flow

interface StationDataSource {
    fun setProjectId(projectId: String)
    suspend fun addStation(station: Station): Resource<Station>
    suspend fun getStation(stationId: String): Resource<Station>
    suspend fun updateStation(station: Station): Resource<Station>
    suspend fun getAllStations(): Flow<Resource<List<Station>>>
}