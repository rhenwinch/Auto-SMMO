package com.xcape.simplemmomod.data.dto

import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.domain.model.User

data class UserInfoDto(
    val avatar: String,
    val bank: String,
    val crafting_level: Int,
    val current_hp: Int,
    val dark_mode: Boolean,
    val diamonds: String,
    val energy: String,
    val energy_percent: Int,
    val exp: String,
    val exp_percent: Int,
    val exp_remaining: String,
    val fishing_level: Int,
    val global_notification: Boolean,
    val gold: String,
    val hp_percent: Int,
    val id: Int,
    val level: String,
    val loggedin: String,
    val max_energy: String,
    val max_exp: String,
    val max_hp: String,
    val max_quest_points: String,
    val maxsteps: Int,
    val membership: String,
    val mining_level: Int,
    val quest_points: String,
    val quest_points_percent: Int,
    val server_time: String,
    val to_next_level: String,
    val total_steps: String,
    val treasure_level: Int,
    val username: String,
    val woodcutting_level: Int
)

fun UserInfoDto.toUser(): User {
    return User(
        username = username,
        id = id,
        gold = gold.replace(",", "").toLong(),
        level = level.replace(",", "").toInt(),
        avatar = BASE_URL + avatar,
        totalSteps = total_steps.replace(",", "").toInt(),
        currentHealthPercentage = hp_percent,
        maxBattleEnergy = max_energy.toInt(),
        maxQuestEnergy = max_quest_points.toInt(),
        battleEnergy = energy.toInt(),
        questEnergy = quest_points.toInt(),
    )
}