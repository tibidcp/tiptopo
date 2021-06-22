package com.tibi.tiptopo.presentation

import com.google.maps.android.geometry.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectAngTest {

    @Test
    fun testDirectAng4Quarter() {
        val a = Point(247.32, 870.54)
        val b = Point(705.65, -567.83)
        assertEquals(a.directAngTo(b).round(6), 287.674241, 0.0)
    }

    @Test
    fun testDirectAng3Quarter() {
        val a = Point(705.65, 1380.96)
        val b = Point(247.32, 870.54)
        assertEquals(a.directAngTo(b).round(6), 228.077849, 0.0)
    }

    @Test
    fun testDirectAng2Quarter() {
        val a = Point(705.65, -567.83)
        val b = Point(247.32, 870.54)
        assertEquals(a.directAngTo(b).round(6), 107.674241, 0.0)
    }

    @Test
    fun testDirectAng1Quarter() {
        val a = Point(247.32, 870.54)
        val b = Point(705.65, 1380.96)
        assertEquals(a.directAngTo(b).round(6), 48.077849, 0.0)
    }

    @Test
    fun testDirectAng0Degrees() {
        val a = Point(247.32, 870.54)
        val b = Point(705.65, 870.54)
        assertEquals(a.directAngTo(b), 0.0, 0.0)
    }

    @Test
    fun testDirectAng180Degrees() {
        val a = Point(705.65, 870.54)
        val b = Point(247.32, 870.54)
        assertEquals(a.directAngTo(b), 180.0, 0.0)
    }

    @Test
    fun testDirectAng90Degrees() {
        val a = Point(705.65, 870.54)
        val b = Point(705.65, 1370.54)
        assertEquals(a.directAngTo(b), 90.0, 0.0)
    }

    @Test
    fun testDirectAng270Degrees() {
        val a = Point(705.65, 1370.54)
        val b = Point(705.65, 870.54)
        assertEquals(a.directAngTo(b), 270.0, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDirectAngSamePoint() {
        val a = Point(705.65, 1370.54)
        val b = Point(705.65, 1370.54)
        a.directAngTo(b)
    }
}