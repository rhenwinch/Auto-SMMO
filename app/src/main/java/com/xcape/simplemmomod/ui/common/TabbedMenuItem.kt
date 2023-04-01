package com.xcape.simplemmomod.ui.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xcape.simplemmomod.common.Endpoints.INVENTORY_URL
import com.xcape.simplemmomod.common.Endpoints.TASKS_URL

sealed class TabbedMenuItem(val tabs: Any) {
    data class Collection(
        val _tabs: List<String> = listOf("Avatars", "Collectables", "Sprites", "Items", "Backgrounds")
    ): TabbedMenuItem(_tabs)
    
    data class Inventory(
        val _tabs: List<String> = listOf("Items", "Equipped", "Storage", "Showcase", "Wishlist")
    ): TabbedMenuItem(_tabs)
    
    data class Profile(
        val _tabs: List<String> = listOf("Profile", "Stats", "Interaction", "Inventory", "Awards")
    ): TabbedMenuItem(_tabs)

    data class Tasks(
        val _tabs: List<String> = listOf("Daily", "Weekly", "Monthly")
    ): TabbedMenuItem(_tabs)

    data class Custom(
        val _tabs: Map<String, Pair<String, Int>>
    ): TabbedMenuItem(_tabs)

    companion object {
        fun String.toTabbedMenuItem(
            customTabItems: String? = null
        ): TabbedMenuItem? {
            return when {
                customTabItems?.isNotEmpty() == true -> {
                    val listType = object: TypeToken<List<String>>() {}.type
                    val json = Gson().fromJson<List<String>>(customTabItems, listType)

                    val tabItems = mutableMapOf<String, Pair<String, Int>>()
                    for(i in json.indices step 2) {
                        tabItems[json[i]] = Pair(json[i + 1], i / 2)
                    }

                    Custom(_tabs = tabItems)
                }
                contains("/user/view/") && endsWith("collection") -> Collection()
                contains("/user/view/") && split("/user/view/")[1].toIntOrNull() != null -> Profile()
                this == INVENTORY_URL -> Inventory()
                this == TASKS_URL -> Tasks()
                else -> null
            }
        }
    }
}