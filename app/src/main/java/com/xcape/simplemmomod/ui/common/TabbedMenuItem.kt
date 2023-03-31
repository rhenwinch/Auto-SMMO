package com.xcape.simplemmomod.ui.common

import com.xcape.simplemmomod.common.Endpoints.INVENTORY_URL
import com.xcape.simplemmomod.common.Endpoints.TASKS_URL

enum class TabbedMenuItem(
    private val _name: String,
    val tabs: List<String>
) {
    COLLECTION(
        _name = "Collections",
        tabs = listOf("Avatars", "Collectables", "Sprites", "Items", "Backgrounds")
    ),
    INVENTORY(
        _name = "Inventory",
        tabs = listOf("Items", "Equipped", "Storage", "Showcase", "Wishlist")
    ),
    PROFILE(
        _name = "Profile",
        tabs = listOf("Profile", "Stats", "Interaction", "Inventory", "Awards")
    ),
    TASKS(
        _name = "Tasks",
        tabs = listOf("Daily", "Weekly", "Monthly")
    );

    override fun toString(): String {
        return _name
    }

    companion object {
        fun String.toTabbedMenuItem(): TabbedMenuItem? {
            return when {
                contains("/user/view/") && endsWith("collection") -> COLLECTION
                contains("/user/view/") -> PROFILE
                this == INVENTORY_URL -> INVENTORY
                this == TASKS_URL -> TASKS
                else -> null
            }
        }
    }
}