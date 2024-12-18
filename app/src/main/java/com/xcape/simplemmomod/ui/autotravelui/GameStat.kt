package com.xcape.simplemmomod.ui.autotravelui

sealed class GameStat(
    val name: String,
    val count: Int
) {
    class Steps(count: Int): GameStat(
        name = "Steps",
        count = count
    )

    class Quests(count: Int): GameStat(
        name = "Quests",
        count = count
    )

    class Battles(count: Int): GameStat(
        name = "Battles",
        count = count
    )

    class ItemsFound(count: Int): GameStat(
        name = "Items",
        count = count
    )

    class MaterialsFound(count: Int): GameStat(
        name = "Materials",
        count = count
    )
}
