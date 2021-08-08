package com.tibi.tiptopo.presentation

import com.tibi.tiptopo.domain.Station
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCoordinatesTest {

    @Test
    fun testFirstQuarter() {
        val station = Station()
        val point = station.getCoordinate(45.0, 0.0, 100.0)
        assertEquals(point.x, 70.71, 0.01)
        assertEquals(point.y, 70.71, 0.01)
    }

    @Test
    fun testSecondQuarter() {
        val station = Station()
        val point = station.getCoordinate(135.0, 0.0, 100.0)
        assertEquals(point.x, - 70.71, 0.01)
        assertEquals(point.y, 70.71, 0.01)
    }

    @Test
    fun testThirdQuarter() {
        val station = Station()
        val point = station.getCoordinate(225.0, 0.0, 100.0)
        assertEquals(point.x, - 70.71, 0.01)
        assertEquals(point.y, - 70.71, 0.01)
    }

    @Test
    fun testFourthQuarter() {
        val station = Station()
        val point = station.getCoordinate(315.0, 0.0, 100.0)
        assertEquals(point.x, 70.71, 0.01)
        assertEquals(point.y, - 70.71, 0.01)
    }

    @Test
    fun testBacksight3599() {
        val station = Station(backsightDA = 359.9)
        val point = station.getCoordinate(315.1, 0.0, 100.0)
        assertEquals(point.x, 70.71, 0.01)
        assertEquals(point.y, - 70.71, 0.01)
    }

    @Test
    fun testBacksight90() {
        val station = Station(backsightDA = 90.0)
        val point = station.getCoordinate(45.0, 0.0, 100.0)
        assertEquals(point.x, - 70.71, 0.01)
        assertEquals(point.y, 70.71, 0.01)
    }

    @Test
    fun testHa0() {
        val station = Station()
        val point = station.getCoordinate(0.0, 0.0, 100.0)
        assertEquals(point.x, 100.0, 0.01)
        assertEquals(point.y, 0.0, 0.01)
    }

    @Test
    fun testHa90Va3() {
        val station = Station()
        val point = station.getCoordinate(90.0, 3.0, 100.0)
        assertEquals(point.x, 0.0, 0.01)
        assertEquals(point.y, 99.86, 0.01)
    }

    @Test
    fun testHa180Va3() {
        val station = Station()
        val point = station.getCoordinate(180.0, 3.0, 100.0)
        assertEquals(point.x, - 99.86, 0.01)
        assertEquals(point.y, 0.0, 0.01)
    }

    @Test
    fun testHa270Va3() {
        val station = Station()
        val point = station.getCoordinate(270.0, 3.0, 100.0)
        assertEquals(point.x, 0.0, 0.01)
        assertEquals(point.y, - 99.86, 0.01)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSamePoint() {
        val station = Station()
        station.getCoordinate(0.0, 0.0, 0.0)
    }
}
