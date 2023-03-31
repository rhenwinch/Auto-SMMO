package com.xcape.simplemmomod.common

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@ProvidedTypeConverter
class UserTypeConverter {
    @TypeConverter
    fun equippableItemsStackToJsonString(stack: List<String>): String {
        return Gson().toJson(stack)
    }

    @TypeConverter
    fun stringToEquippableItemsStack(json: String): List<String> {
        val listType = object: TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, listType)
    }
}