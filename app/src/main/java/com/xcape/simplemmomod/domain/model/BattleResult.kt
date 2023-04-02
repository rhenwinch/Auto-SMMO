package com.xcape.simplemmomod.domain.model

data class BattleResult(
    val isSuccess: Boolean,
    val result: String?,
    val opponentHp: Int,
    val opponentHpPercentage: Int,
    val playerHp: Int,
    val playerHpPercentage: Int,
)