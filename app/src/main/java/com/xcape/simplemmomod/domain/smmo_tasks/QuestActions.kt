package com.xcape.simplemmomod.domain.smmo_tasks

import com.xcape.simplemmomod.domain.model.QuestResult
import com.xcape.simplemmomod.domain.model.User

interface QuestActions {
    suspend fun getHighestAvailableQuest(): String
    suspend fun performQuest(questId: String, user: User): QuestResult
}