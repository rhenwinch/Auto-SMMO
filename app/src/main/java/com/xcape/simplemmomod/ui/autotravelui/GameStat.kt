package com.xcape.simplemmomod.ui.autotravelui

sealed class GameStat(
    val name: String,
    val count: Int
) {
    data class Steps(val _count: Int): GameStat(
        name = "Steps",
        count = _count
    )

    data class Quests(val _count: Int): GameStat(
        name = "Quests",
        count = _count
    )

    data class Battles(val _count: Int): GameStat(
        name = "Battles",
        count = _count
    )

    data class ItemsFound(val _count: Int): GameStat(
        name = "Items",
        count = _count
    )

    data class MaterialsFound(val _count: Int): GameStat(
        name = "Materials",
        count = _count
    )
}
