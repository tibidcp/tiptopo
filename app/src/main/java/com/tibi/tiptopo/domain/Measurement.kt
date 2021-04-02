package com.tibi.tiptopo.domain

import com.google.android.gms.maps.model.LatLng

data class Measurement(
    var id: String = "",
    var stationId: String = "",
    var name: String = "",
    var va: Double = 0.0,
    var ha: Double = 0.0,
    var sd: Double = 0.0,
    var ht: Double = 0.0,
    var latLng: LatLng? = null,
    var date: Long = 0
)
