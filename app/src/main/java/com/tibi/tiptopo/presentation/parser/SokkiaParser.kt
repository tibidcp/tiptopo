package com.tibi.tiptopo.presentation.parser

class SokkiaParser(private val message: String): IDataParser {
    private val strings = message.split(" ")

    override fun isValid(): Boolean {
        return message.length == 23
    }

    override fun parseSD(): Double {
        val sd = strings[0].toInt()
        return sd / 1000.0
    }

    override fun parseHA(): Double {
        val ha = strings[2].toInt()
        val sec = ha % 100
        val min = ha / 100 % 100
        val deg = ha / 10000
        return deg + min / 60.0 + sec / 3600.0
    }

    override fun parseVA(): Double {
        val va = strings[1].toInt()
        val sec = va % 100
        val min = va / 100 % 100
        val deg = va / 10000
        val sokkiaVa = deg + min / 60.0 + sec / 3600.0
        return if (sokkiaVa >= 90.0) {
            sokkiaVa - 90.0
        } else {
            sokkiaVa + 270.0
        }
    }
}