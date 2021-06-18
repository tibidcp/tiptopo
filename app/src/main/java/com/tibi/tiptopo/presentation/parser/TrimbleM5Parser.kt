package com.tibi.geodesy.parser

class TrimbleM5Parser(val message: String) : IDataParser {
    private val strings = message.split("\\s+".toRegex())
    override fun isValid(): Boolean {
        return message.contains("M5|Adr") && message.contains("|SD") &&
                message.contains("m") && message.contains("|Hz") &&
                message.contains("DMS") && message.contains("|V1")
    }

    override fun parseSD(): Float {
        return strings[5].toFloat()
    }

    override fun parseHA(): Float {
        return strings[8].toFloat()
    }

    override fun parseVA(): Float {
        return strings[11].toFloat()
    }

}