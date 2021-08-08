package com.tibi.tiptopo.presentation

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateTransformTest {

    @Test
    fun testPointToLatLng() {
        val a = Point(50.0 / SCALE, 35.0 / SCALE)
        assertEquals(a.toLatLng().latitude.round(9), 55.667454227, 0.0)
        assertEquals(a.toLatLng().longitude.round(9), 37.498730831, 0.0)
    }

    @Test
    fun testLatLngToPoint() {
        val latLng = LatLng(55.672879, 37.504903)
        assertEquals(latLng.toPoint().x.round(3), (653.973 / SCALE).round(3), 0.0)
        assertEquals(latLng.toPoint().y.round(3), (423.359 / SCALE).round(3), 0.0)
    }
}
