package com.xcape.simplemmomod.common

import android.util.Log
import com.google.gson.Gson
import com.xcape.simplemmomod.common.Constants.APP_TAG
import java.util.*

object Functions {
    val gson = Gson()

    inline fun <reified T : Any> String.toJson(jsonObject: Class<T>): T {
        return gson.fromJson(this, jsonObject)
    }

    fun getStringInBetween(
        string: String,
        delimiter1: String,
        delimiter2: String
    ): String {
        return try {
            string.split(delimiter1)[1].split(delimiter2)[0]
        } catch (e: Exception) {
            Log.e(APP_TAG, string)
            throw e
        }
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