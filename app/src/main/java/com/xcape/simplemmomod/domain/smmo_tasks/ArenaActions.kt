package com.xcape.simplemmomod.domain.smmo_tasks

import com.xcape.simplemmomod.domain.model.Npc

interface ArenaActions {
    suspend fun generateNpc(): Npc
    suspend fun attackNpc(
        npcId: String,
        verifyCallback: suspend () -> Unit,
        shouldAutoEquip: Boolean = true,
        isUserTravelling: Boolean = true,
    ): Long
}