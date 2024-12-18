package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.common.toRarityType
import com.xcape.simplemmomod.domain.model.Item

data class ItemDto(
    val circulation: String,
    val currently_equipped: Boolean,
    val currently_equipped_string: String,
    val equipable: Int,
    val image: String,
    val level: Int,
    val name: String,
    val rarity: String,
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