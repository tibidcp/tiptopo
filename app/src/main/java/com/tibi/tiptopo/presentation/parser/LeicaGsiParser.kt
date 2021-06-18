package com.tibi.geodesy.parser

class LeicaGsiParser(val message: String) : IDataParser {
    override fun isValid(): Boolean {
        return message.contains("11") && message.contains("21") && message.contains("22") &&
                message.contains("31") && message.contains("51") && message.contains("87") &&
                message.contains("88")
    }

    override fun parseSD(): Float {
        return message.substring(55, 63).toFloat() / 1000
    }


    override fun parseHA(): Float {
        val ang: Int = message.substring(23, 31).toInt() / 10
        val sec = ang % 100
        val min = ang / 100 % 100
        val deg = ang / 10000
        return deg + min / 60.0f + sec / 3600.0f
    }

    override fun parseVA(): Float {
        val ang: Int = message.substring(39, 47).toInt() / 10
        val s: Int
        val m: Int
        val d: Int
        s = ang % 100
        m = ang / 100 % 100
        d = ang / 10000
        val dms = d + m / 60.0f + s / 3600.0f
        return if (dms <= 90f) {
            90f - dms
        } else {
            360f - (dms - 90f)
        }
    }
}