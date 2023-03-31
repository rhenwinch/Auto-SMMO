package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.domain.model.UpgradeSkillResult

data class UpgradeSkillDto(
    val available_stats: Int,
    val new_stat_amount: Int,
    val result: String,
    val title: String,
    val type: String
)

fun UpgradeSkillDto.toUpgradeResponse(): UpgradeSkillResult {
    return UpgradeSkillResult(
        isSuccess = type == "success",
        newStatAmount = new_stat_amount
    )
}