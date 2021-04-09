package com.tibi.tiptopo.domain

import android.graphics.Color

data class Line(
    var id: String = "",
    var color: Int = Color.BLACK,
    var type: LineType = LineType.Continuous,
    var vertices: List<Vertex> = listOf()
)

enum class LineType {
    Continuous
}
