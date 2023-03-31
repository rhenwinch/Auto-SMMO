package com.xcape.simplemmomod.common

enum class RarityType {
    COMMON,
    UNCOMMON,
    RARE,
    ELITE,
    EPIC,
    LEGENDARY,
    CELESTIAL,
    EXOTIC
}

fun String.toRarityType(): RarityType {
    return when(this) {
        "Common" -> RarityType.COMMON
        "Uncommon" -> RarityType.UNCOMMON
        "Rare" -> RarityType.RARE
        "Elite" -> RarityType.ELITE
        "Epic" -> RarityType.EPIC
        "Legendary" -> RarityType.LEGENDARY
        "Celestial" -> RarityType.CELESTIAL
        "Exotic" -> RarityType.EXOTIC
        else -> throw Exception("Invalid rarity given!")
    }
}