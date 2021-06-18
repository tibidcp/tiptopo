package com.tibi.tiptopo.presentation.parser

class NikonRawParser(private val message: String) : IDataParser {
    override fun isValid(): Boolean {
        return message.contains("HA") && message.contains("VA") && message.contains("SD") && message.contains(
            "HT"
        )
    }

    override fun parseSD(): Double {
        val start = message.lastIndexOf("SD:") + 3
        val end = message.substring(start).indexOf(" ")
        return message.substring(start).substring(0, end).toDouble() / 10000
    }

    override fun parseHA(): Double {
        val start = message.lastIndexOf("HA:") + 3
        val end = message.substring(start).indexOf(" ")
        val ang = message.substring(start).substring(0, end).toInt() / 10
        val sec = ang % 100
        val min = ang / 100 % 100
        val deg = ang / 10000
        return deg + min / 60.0 + sec / 3600.0
    }

    override fun parseVA(): Double {
        val start = message.lastIndexOf("VA:") + 3
        val end = message.substring(start).indexOf(" ")
        val ang = message.substring(start).substring(0, end).toInt() / 10
        val sec = ang % 100
        val min = ang / 100 % 100
        val deg = ang / 10000
        return deg + min / 60.0 + sec / 3600.0
    }
}