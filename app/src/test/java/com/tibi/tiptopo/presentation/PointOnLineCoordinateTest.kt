package com.tibi.tiptopo.presentation

import com.google.maps.android.geometry.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class PointOnLineCoordinateTest {

    @Test
    fun test1() {
        val start = Point(1.0, 4.0)
        val end = Point(5.0, 7.0)
        val distance = 2.0
        val result = pointOnLineCoordinate(start, end, distance)
        assertEquals(result.x, 2.6, 0.1)
        assertEquals(result.y, 5.2, 0.1)
    }

    @Test
    fun test2() {
        val start = Point(1.0, 4.0)
        val end = Point(5.0, 7.0)
        val distance = 4.0
        val result = pointOnLineCoordinate(start, end, distance)
        assertEquals(result.x, 4.2, 0.1)
        assertEquals(result.y, 6.4, 0.1)
    }

    @Test
    fun test3() {
        val start = Point(5.0, 7.0)
        val end = Point(1.0, 4.0)
        val distance = 3.0
        val result = pointOnLineCoordinate(start, end, distance)
        assertEquals(result.x, 2.6, 0.1)
        assertEquals(result.y, 5.2, 0.1)
    }

    @Test
    fun test4() {
        val start = Point(-2.33, -2.267)
        val end = Point(1.670, 0.733)
        val distance = 1.0
        val result = pointOnLineCoordinate(start, end, distance)
        assertEquals(result.x, -1.53, 0.01)
        assertEquals(result.y, -1.667, 0.001)
    }
}
