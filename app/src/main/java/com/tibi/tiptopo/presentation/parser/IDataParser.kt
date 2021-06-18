package com.tibi.tiptopo.presentation.parser

interface IDataParser {
    fun isValid(): Boolean

    fun parseSD(): Double

    fun parseHA(): Double

    fun parseVA(): Double
}