package com.tibi.tiptopo.presentation.parser

class SokkiaParser(private val message: String): IDataParser {
    override fun isValid(): Boolean {
        return true
    }

    override fun parseSD(): Double {
        return 0.0
    }

    override fun parseHA(): Double {
        return 0.0
    }

    override fun parseVA(): Double {
        return 0.0
    }
}