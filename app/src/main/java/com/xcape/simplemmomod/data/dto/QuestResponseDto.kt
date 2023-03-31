package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.domain.model.QuestResult

data class QuestResponseDto(
    val exp: String,
    val fail: Boolean,
    val gold: String,
    val item: Boolean,
    val quest_points: Int,
    val quest_points_percentage: Int,
    val resultText: String,
    val status: String
)

fun QuestResponseDto.toQuestResponse(): QuestResult {
    return QuestResult(
        isQuestFailed = fail,
        exp = exp,
        gold = gold,
        questPoints = quest_points,
        resultText = resultText,
        status = status
    )
}

