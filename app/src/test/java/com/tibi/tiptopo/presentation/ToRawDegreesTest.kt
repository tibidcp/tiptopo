package com.tibi.tiptopo.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class ToRawDegreesTest {

    @Test
    fun test1() {
        val angle = 22.9868
        val result = "22.5912"
        assertEquals(angle.toRawDegrees(), result)
    }

    @Test
    fun test2() {
        val angle = 0.0
        val result = "0.0000"
        assertEquals(angle.toRawDegrees(), result)
    }

    @Test
    fun test3() {
        val angle = 59.9999
        val result = "59.5959"
        assertEquals(angle.toRawDegrees(), result)
    }
}
