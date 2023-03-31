package com.xcape.simplemmomod.common

enum class StepType {
    MATERIAL,
    TEXT,
    ITEM,
    NPC,
    PLAYER;

    companion object {
        fun String.toStepType(): StepType {
            return when(this) {
                "material" -> MATERIAL
                "text" -> TEXT
                "item" -> ITEM
                "npc" -> NPC
                "player" -> PLAYER
                else -> throw Exception("Invalid step given!")
            }
        }
    }
}

