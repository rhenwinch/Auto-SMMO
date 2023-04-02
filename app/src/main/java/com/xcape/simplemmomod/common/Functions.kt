package com.xcape.simplemmomod.common

import com.google.gson.Gson
import java.util.*

object Functions {
    inline fun <reified T : Any> String.toJson(jsonObject: Class<T>): T {
        return Gson().fromJson(this, jsonObject)
    }

    fun getStringInBetween(string: String, delimiter1: String, delimiter2: String): String {
        return string.split(delimiter1)[1].split(delimiter2)[0]
    }

    fun removeHtmlTags(rawHtml: String): String {
        val regexHtml = Regex("<.*?>")
        return rawHtml.replace(regexHtml, "")
    }

    fun getTimeInMilliseconds(): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
    }

    fun isUserWayPastDailyResetTime(resetTime: Long): Boolean {
        val currentTime = getTimeInMilliseconds()

        return when {
            currentTime >= resetTime -> true
            else -> false
        }
    }
}