package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.common.toRarityType
import com.xcape.simplemmomod.domain.model.Item

data class ItemDto(
    val additional_data: String,
    val avatar_collection: Int,
    val background_collection: Int,
    val circulation: String,
    val collectable_collection: Int,
    val currently_equipped: Boolean,
    val currently_equipped_string: String,
    val custom_item: Int,
    val description: String,
    val equipable: Int,
    val existing_stat1: String,
    val existing_stat1_modifier: Any,
    val existing_stat2: String,
    val existing_stat2_modifier: Any,
    val id: Int,
    val image: String,
    val item_collection: Int,
    val itemcolour: String,
    val level: Int,
    val market: String,
    val name: String,
    val quantity: Int,
    val rarity: String,
    val sprite_collection: Int,
    val stat1: String,
    val stat1modifier: Int,
    val stat2: String,
    val stat2modifier: Int,
    val stat3: String,
    val stat3modifier: Int,
    val stats_string: String,
    val type: String,
    val value: String
)

fun ItemDto.toItem(): Item {
    return Item(
        name = name,
        level = level,
        rarity = rarity.toRarityType(),
        type = type,
        isEquipSlotEmpty = currently_equipped_string.isEmpty(),
        isItemATool = isItemATool(type),
        isItemEquippable = equipable == 1,
        isItemCurrentlyEquipped = currently_equipped,
        isFoundItemBetter = stats_string.contains("caret-up"),
        isFoundItemWorse = stats_string.contains("caret-down")
    )
}

private fun isItemATool(itemType: String): Boolean {
    val validToolType = listOf(
        "Fishing Rod",
        "Wood Axe",
        "Pickaxe",
        "Shovel"
    )

    return validToolType.contains(itemType)
}