package com.tibi.tiptopo.data.measurement

import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Measurement
import kotlinx.coroutines.flow.Flow

interface MeasurementDataSource {
    suspend fun addMeasurement(measurement: Measurement): Resource<Measurement>
    suspend fun getMeasurement(measurementId: String): Resource<Measurement>
    suspend fun updateMeasurement(measurement: Measurement): Resource<Measurement>
    suspend fun getAllMeasurements(): Flow<Resource<List<Measurement>>>
    suspend fun deleteMeasurement(measurementId: String)
}
