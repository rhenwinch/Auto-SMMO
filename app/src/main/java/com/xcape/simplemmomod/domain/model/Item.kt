package com.xcape.simplemmomod.domain.model

import com.xcape.simplemmomod.common.RarityType

data class Item(
    val name: String,
    val level: Int,
    val rarity: RarityType,
    val type: String,
    val isEquipSlotEmpty: Boolean,
    val isItemATool: Boolean,
    val isItemEquippable: Boolean,
    val isItemCurrentlyEquipped: Boolean,
    val isFoundItemBetter: Boolean,
    val isFoundItemWorse: Boolean,
)
