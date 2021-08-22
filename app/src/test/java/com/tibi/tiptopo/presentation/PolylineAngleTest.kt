package com.tibi.tiptopo.presentation

import com.google.maps.android.geometry.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class PolylineAngleTest {

    @Test
    fun test1() {
        val a = Point(0.0, 0.0)
        val b = Point(0.0, 10.0)
        assertEquals(a.polylineAngleTo(b).round(6), 0.0, 0.0)
    }

    @Test
    fun test2() {
        val a = Point(0.0, 0.0)
        val b = Point(5.0, 5.0)
        assertEquals(a.polylineAngleTo(b).round(6), 315.0, 0.0)
    }

    @Test
    fun test3() {
        val a = Point(1.0, 1.0)
        val b = Point(5.0, 5.0)
        assertEquals(a.polylineAngleTo(b).round(6), 315.0, 0.0)
    }

    @Test
    fun test4() {
        val b = Point(1.0, 1.0)
        val a = Point(5.0, 5.0)
        assertEquals(a.polylineAngleTo(b).round(6), 135.0, 0.0)
    }
}
