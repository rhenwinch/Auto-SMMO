package com.xcape.simplemmomod.common

import androidx.core.text.HtmlCompat
import com.google.gson.Gson

object Functions {
    inline fun <reified T : Any> String.toJson(jsonObject: Class<T>): T {
        return Gson().fromJson(this, jsonObject)
    }

    fun getStringInBetween(string: String, delimiter1: String, delimiter2: String): String {
        return string.split(delimiter1)[1].split(delimiter2)[0]
    }

    fun removeHtmlTags(rawHtml: String): String {
        return HtmlCompat.fromHtml(rawHtml, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }

    fun getTimeInMilliseconds(): Long {
        return System.currentTimeMillis()
    }

    fun isUserWayPastDailyResetTime(resetTime: Long): Boolean {
        val currentTime = getTimeInMilliseconds()

        return when {
            currentTime >= resetTime -> true
            else -> false
        }
    }
}