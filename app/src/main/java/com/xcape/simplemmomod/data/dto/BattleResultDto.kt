package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.domain.model.BattleResult

data class BattleResultDto(
    val confirm_button_text: String,
    val damage_given_to_opponent: Int,
    val damage_given_to_player: Int,
    val opponent_hp: Int,
    val opponent_hp_percentage: Int,
    val player_hp: Int,
    val player_hp_percentage: Int,
    val result: String?,
    val show_confirm_button: Boolean,
    val title: String?,
    val type: String?
)

fun BattleResultDto.toBattleResult(): BattleResult {
    return BattleResult(
        isSuccess = type == "success",
        result = result,
        opponentHp = opponent_hp,
        opponentHpPercentage = opponent_hp_percentage,
        playerHp = player_hp,
        playerHpPercentage = player_hp_percentage,
    )
}