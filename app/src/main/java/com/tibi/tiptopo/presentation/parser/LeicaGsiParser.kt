package com.tibi.tiptopo.presentation.parser

class LeicaGsiParser(private val message: String) : IDataParser {
    override fun isValid(): Boolean {
        return message.contains("11") && message.contains("21") && message.contains("22") &&
                message.contains("31") && message.contains("51") && message.contains("87") &&
                message.contains("88")
    }

    override fun parseSD(): Double {
        return message.substring(55, 63).toDouble() / 1000
    }


    override fun parseHA(): Double {
        val ang: Int = message.substring(23, 31).toInt() / 10
        val sec = ang % 100
        val min = ang / 100 % 100
        val deg = ang / 10000
        return deg + min / 60.0 + sec / 3600.0
    }

    override fun parseVA(): Double {
        val ang: Int = message.substring(39, 47).toInt() / 10
        val s: Int = ang % 100
        val m: Int = ang / 100 % 100
        val d: Int = ang / 10000
        val dms = d + m / 60.0 + s / 3600.0
        return if (dms <= 90.0) {
            90.0 - dms
        } else {
            360.0 - (dms - 90.0)
        }
    }
}