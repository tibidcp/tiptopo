package com.tibi.tiptopo.domain

data class Station(
    var id: String = "",
    var name: String = "",
    var hi: Double = 0.0,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var da: Double = 0.0,
    var date: Long = 0
)
