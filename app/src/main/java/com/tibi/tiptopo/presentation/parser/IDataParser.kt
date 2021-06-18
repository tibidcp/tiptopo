package com.tibi.geodesy.parser

interface IDataParser {
    fun isValid(): Boolean

    fun parseSD(): Float

    fun parseHA(): Float

    fun parseVA(): Float
}