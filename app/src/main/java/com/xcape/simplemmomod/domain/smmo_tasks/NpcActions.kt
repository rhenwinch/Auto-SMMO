package com.xcape.simplemmomod.domain.smmo_tasks

import com.xcape.simplemmomod.domain.model.Npc

interface NpcActions {
    suspend fun generateNpc(): Npc
    suspend fun attackNpc(
        npcId: String,
        npc: Npc? = null,
        verifyCallback: suspend () -> Unit,
        healthPercentageToRetreatOn: Int = 15,
        shouldAutoEquip: Boolean = true,
        isUserTravelling: Boolean = true,
    ): Long
}