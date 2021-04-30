package com.tibi.tiptopo.data.line

import com.tibi.tiptopo.data.Resource
import com.tibi.tiptopo.domain.Line
import kotlinx.coroutines.flow.Flow

interface LineDataSource {
    suspend fun addLine(line: Line): Resource<Line>
    suspend fun getLine(lineId: String): Resource<Line>
    suspend fun updateLine(line: Line): Resource<Line>
    suspend fun getAllLines(): Flow<Resource<List<Line>>>
    suspend fun deleteLine(lineId: String)
}