package com.tibi.tiptopo.domain

data class Project(
    var id: String = "",
    var name: String = "",
    var date: Long = 0,
    var totalStation: TotalStation = TotalStation.NikonNPL352
)

enum class TotalStation {
    NikonNPL352,
    TrimbleM3
}
