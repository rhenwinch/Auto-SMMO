package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.domain.model.Npc

data class NpcDto(
    val def: Long,
    val description: String,
    val hp: Long,
    val id: Long,
    val image: String,
    val level: Int,
    val name: String,
    val str: Long,
    val result: String? = null
)

fun NpcDto.toNpc(): Npc {
    return Npc(
        id = id,
        name = name,
        level = level,
        avatar = BASE_URL + image,
        result = result
    )
}

