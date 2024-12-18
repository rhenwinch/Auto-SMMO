package com.xcape.simplemmomod.ui.common

import java.util.Locale

object UserAgentGenerator {
    fun generate(): String {

        val sb = StringBuilder()
        sb.append("Mozilla/5.0 ")

        sb.append(
            String.format(
                Locale.US,
                format = "(Linux; Android %d; SM-A0%dU)",
                (12..14).random(),
                (30..50).random()
            )
        )

        sb.append(
            String.format(
                Locale.US,
                format = " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%d.0.%d.%d Mobile Safari/537.36",
                (131..133).random(),
                (6000..7000).random(),
                (100..150).random()
            )
        )

        return sb.toString()
    }
}