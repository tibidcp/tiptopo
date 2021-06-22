package com.tibi.tiptopo.presentation

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateTransformTest {

    @Test
    fun testPointToLatLng() {
        val a = Point(0.0, 0.0)
        assertEquals(a.toLatLng().longitude.round(9), 55.667005122, 0.0)
        assertEquals(a.toLatLng().latitude.round(9), 37.498174661, 0.0)
    }

    @Test
    fun testLatLngToPoint() {
        val latLng = LatLng(55.672879, 37.504903)
        assertEquals(latLng.toPoint().y.round(3), 653.973, 0.0)
        assertEquals(latLng.toPoint().x.round(3), 423.359, 0.0)
    }
}