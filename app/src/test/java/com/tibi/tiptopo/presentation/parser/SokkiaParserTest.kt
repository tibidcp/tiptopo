package com.tibi.tiptopo.presentation.parser

import com.tibi.tiptopo.presentation.round
import org.junit.Assert.*
import org.junit.Test

class SokkiaParserTest {
    @Test
    fun testIsValidTrue() {
        val message = "0003860 0890446 2421636"
        val parser = SokkiaParser(message)
        assertTrue(parser.isValid())
    }

    @Test
    fun testIsValidFalse() {
        val message = "0003860  2421636"
        val parser = SokkiaParser(message)
        assertFalse(parser.isValid())
    }

    @Test
    fun testSdParse() {
        val message = "0003860 0890446 2421636"
        val parser = SokkiaParser(message)
        assertEquals(parser.parseSD(), 3.86, 0.0)
    }

    @Test
    fun testHaParse() {
        val message = "0003860 0890446 2421636"
        val parser = SokkiaParser(message)
        assertEquals(parser.parseHA().round(2), 242.28, 0.01)
    }

    @Test
    fun testVaParse() {
        val message = "0003860 0890446 2421636"
        val parser = SokkiaParser(message)
        assertEquals(parser.parseVA(), 359.08, 0.01)
    }
}