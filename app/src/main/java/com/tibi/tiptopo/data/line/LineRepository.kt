package com.tibi.tiptopo.data.line

import com.tibi.tiptopo.domain.Line
import com.tibi.tiptopo.presentation.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LineRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataSource: LineDataSource
) {
    suspend fun addLine(line: Line) = withContext(ioDispatcher) { dataSource.addLine(line) }

    suspend fun getLine(lineId: String) = withContext(ioDispatcher) { dataSource.getLine(lineId) }

    suspend fun updateLine(line: Line) = withContext(ioDispatcher) { dataSource.updateLine(line) }

    suspend fun getAllLines() = withContext(ioDispatcher) { dataSource.getAllLines() }

    suspend fun deleteLine(lineId: String) = withContext(ioDispatcher) {
        dataSource.deleteLine(lineId)
    }
}
