package com.xcape.simplemmomod.domain.smmo_tasks

import com.xcape.simplemmomod.common.Resource
import com.xcape.simplemmomod.common.SkillType

interface Traveller {
    var energyTimer: Long

    suspend fun takeStep(
        shouldAutoEquip: Boolean = true,
        shouldSkipNPCs: Boolean = false
    ): Long
    suspend fun verify()
    suspend fun doQuest(): Long
    suspend fun doArena(
        shouldAutoEquip: Boolean = true,
        shouldSkipNPCs: Boolean = false
    ): Long
    suspend fun upgradeSkill(skillToUpgrade: SkillType = SkillType.DEX): Long

    fun resetEnergyTimer()
}