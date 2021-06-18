package com.tibi.tiptopo.presentation.parser

class TrimbleM5Parser(private val message: String) : IDataParser {
    private val strings = message.split("\\s+".toRegex())
    override fun isValid(): Boolean {
        return message.contains("M5|Adr") && message.contains("|SD") &&
                message.contains("m") && message.contains("|Hz") &&
                message.contains("DMS") && message.contains("|V1")
    }

    override fun parseSD(): Double {
        return strings[5].toDouble()
    }

    override fun parseHA(): Double {
        return strings[8].toDouble()
    }

    override fun parseVA(): Double {
        return strings[11].toDouble()
    }

}