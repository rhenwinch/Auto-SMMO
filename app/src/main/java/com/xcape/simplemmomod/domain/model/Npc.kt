package com.xcape.simplemmomod.domain.model

data class Npc(
    val id: Long,
    val name: String,
    val level: Int,
    val avatar: String,
    val result: String?
)