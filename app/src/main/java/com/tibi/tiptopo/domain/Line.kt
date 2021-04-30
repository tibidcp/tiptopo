package com.tibi.tiptopo.domain

import android.graphics.Color

data class Line(
    var id: String = "",
    var name: String = "",
    var color: Int = Color.BLACK,
    var type: LineType = LineType.Continuous,
    var vertices: List<Vertex> = listOf(),
    var date: Long = 0
)

enum class LineType {
    Continuous
}
