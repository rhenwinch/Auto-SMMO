package com.xcape.simplemmomod.domain

import androidx.datastore.core.Serializer
import com.xcape.simplemmomod.domain.model.AppPreferences
import com.xcape.simplemmomod.domain.model.User
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppPreferencesSerializer : Serializer<AppPreferences> {
    override val defaultValue: AppPreferences
        get() = AppPreferences()

    override suspend fun readFrom(input: InputStream): AppPreferences {
        return try {
            Json.decodeFromString(
                deserializer = AppPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: AppPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = AppPreferences.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}