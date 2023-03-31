package com.xcape.simplemmomod.ui.common

object UserAgentGenerator {
    fun generate(): String {
        val sb = StringBuilder()
        sb.append("Mozilla/5.0 ")

        sb.append(
            String.format(
                format = "(Linux; Android %d; SM-M%dF)",
                (9..12).random(),
                (100..500).random()
            )
        )

        sb.append(
            String.format(
                format = " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%d.0.%d.%d Mobile Safari/537.36",
                (100..110).random(),
                (5000..5481).random(),
                (10..65).random()
            )
        )

        return sb.toString()
    }
}