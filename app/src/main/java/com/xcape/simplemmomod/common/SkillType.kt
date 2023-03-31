package com.xcape.simplemmomod.common

enum class SkillType {
    STR,
    DEX,
    DEF;

    override fun toString(): String {
        return when(this) {
            STR -> "str"
            DEX -> "dex"
            DEF -> "def"
        }
    }

    companion object {
        fun String.toSkillType(): SkillType {
            return when(this) {
                "str" -> STR
                "def" -> DEF
                "dex" -> DEX
                else -> throw Exception("Invalid skill type given!")
            }
        }
    }
}