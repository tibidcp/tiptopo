package com.tibi.tiptopo.presentation

import com.google.maps.android.geometry.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceToTest {

    @Test
    fun test1() {
        val start = Point(2.0, 4.0)
        val end = Point(4.0, 1.0)
        val result = start.distanceTo(end)
        assertEquals(result, 3.61, 0.01)
    }
    @Test
    fun test2() {
        val start = Point(-24.253, -8.139)
        val end = Point(15.800, 13.350)
        val result = start.distanceTo(end)
        assertEquals(result, 45.45, 0.01)
    }
}
