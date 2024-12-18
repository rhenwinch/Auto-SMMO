package com.xcape.simplemmomod.data.dto

import com.google.gson.annotations.SerializedName
import com.xcape.simplemmomod.domain.model.GatherResult

data class GatherResultDto(
    val type: String,
    @SerializedName("skill_experience_gained") val craftingExpGained: Int,
    @SerializedName("is_end") val gatherEnd: Boolean,
    @SerializedName("player_experience_gained") val playerExpGained: Int,
)

fun GatherResultDto.toGatherResult(): GatherResult {
    return GatherResult(
        gatherEnd = gatherEnd,
        playerExpGained = playerExpGained,
        craftingExpGained = craftingExpGained
    )
}