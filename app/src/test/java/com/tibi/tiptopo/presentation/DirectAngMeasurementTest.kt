package com.tibi.tiptopo.presentation

import com.tibi.tiptopo.domain.Measurement
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectAngMeasurementTest {

    @Test
    fun testDirectAng4Quarter() {
        val a = Measurement(latitude = 55.669226032, longitude = 37.512009252)
        val b = Measurement(latitude = 55.673342674, longitude = 37.489149272)
        assertEquals(a.directAngTo(b).round(6), 287.674241, 0.0)
    }

    @Test
    fun testDirectAng3Quarter() {
        val a = Measurement(latitude = 55.673341687, longitude = 37.520122950)
        val b = Measurement(latitude = 55.669226032, longitude = 37.512009252)
        assertEquals(a.directAngTo(b).round(5), 228.07785, 0.0)
    }

    @Test
    fun testDirectAng2Quarter() {
        val a = Measurement(latitude = 55.673342674, longitude = 37.489149272)
        val b = Measurement(latitude = 55.669226032, longitude = 37.512009252)
        assertEquals(a.directAngTo(b).round(6), 107.674241, 0.0)
    }

    @Test
    fun testDirectAng1Quarter() {
        val a = Measurement(latitude = 55.669226032, longitude = 37.512009252)
        val b = Measurement(latitude = 55.673341687, longitude = 37.520122950)
        assertEquals(a.directAngTo(b).round(6), 48.077854, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDirectAngSamePoint() {
        val a = Measurement(latitude = 55.673341713, longitude = 37.519957337)
        val b = Measurement(latitude = 55.673341713, longitude = 37.519957337)
        a.directAngTo(b)
    }
}
