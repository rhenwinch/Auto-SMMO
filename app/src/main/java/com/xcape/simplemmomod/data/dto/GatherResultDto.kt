package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.domain.model.GatherResult

data class GatherResultDto(
    val craftingExpGained: Int,
    val expRemaining: Int,
    val gatherEnd: Boolean,
    val newLevel: Int,
    val newPercent: Int,
    val playerExpGained: Int,
    val result: String,
    val type: String
)

fun GatherResultDto.toGatherResult(): GatherResult {
    return GatherResult(
        gatherEnd = gatherEnd,
        playerExpGained = playerExpGained,
        craftingExpGained = craftingExpGained
    )
}