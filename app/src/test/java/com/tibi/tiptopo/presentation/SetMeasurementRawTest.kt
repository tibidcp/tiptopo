package com.tibi.tiptopo.presentation

import com.tibi.tiptopo.domain.Measurement
import com.tibi.tiptopo.domain.Station
import com.tibi.tiptopo.domain.TotalStation
import org.junit.Assert.assertEquals
import org.junit.Test

class SetMeasurementRawTest {
    @Test
    fun test1() {
        val station = Station(
            id = "Jqw4dANh7pWNUEeMZ3a6",
            backsightHA = 154.04194444444445,
            backsightDA = 246.7341100472902,
            x = 112.08929332086015,
            y = 417.9457976538788
        )
        val measurement = Measurement(
            latitude = 55.67707036019693,
            longitude = 37.571533160264714
        )
        val realVa = 0.0
        val realHa = 357.04583333333335
        val realSd = 43.568

        station.setMeasurementRaw(measurement, TotalStation.Nikon)


        assertEquals(measurement.ha, realHa, 0.001)
        assertEquals(measurement.va, realVa, 0.0)
        assertEquals(measurement.sd, realSd, 0.001)

    }

    @Test
    fun test2() {
        val station = Station(
            id = "Jqw4dANh7pWNUEeMZ3a6",
            backsightHA = 154.04194444444445,
            backsightDA = 246.7341100472902,
            x = 112.08929332086015,
            y = 417.9457976538788
        )
        val measurement = Measurement(
            latitude = 55.67707036019693,
            longitude = 37.571533160264714
        )
        val realVa = 90.0
        val realHa = 357.04583333333335
        val realSd = 43.568

        station.setMeasurementRaw(measurement, TotalStation.Sokkia)


        assertEquals(measurement.ha, realHa, 0.001)
        assertEquals(measurement.va, realVa, 0.0)
        assertEquals(measurement.sd, realSd, 0.001)

    }
}