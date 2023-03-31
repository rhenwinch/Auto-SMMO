package com.xcape.simplemmomod.domain.model

data class QuestResult(
    val isQuestFailed: Boolean,
    val exp: String?,
    val gold: String?,
    val questPoints: Int?,
    val resultText: String,
    val status: String
)
