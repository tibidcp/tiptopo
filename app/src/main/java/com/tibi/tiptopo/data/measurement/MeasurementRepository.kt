package com.tibi.tiptopo.data.measurement

import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.presentation.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MeasurementRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataSource: MeasurementDataSource
) {
    suspend fun addMeasurement(measurement: Measurement) =
        withContext(ioDispatcher) { dataSource.addMeasurement(measurement) }

    suspend fun getMeasurement(measurementId: String) =
        withContext(ioDispatcher) { dataSource.getMeasurement(measurementId) }

    suspend fun updateMeasurement(measurement: Measurement) =
        withContext(ioDispatcher) { dataSource.updateMeasurement(measurement) }

    suspend fun getAllMeasurements() = withContext(ioDispatcher) {
        dataSource.getAllMeasurements()
    }
}
