package com.xcape.simplemmomod.data.dto

data class UserEventsDto(
    val avatar: String,
    val character_badge: Int,
    val current_hp: Int,
    val dark_mode: Boolean,
    val diamond_store_sale: Boolean,
    val diamonds: Int,
    val energy: Int,
    val energy_percent: Int,
    val events: Int,
    val exp: Int,
    val exp_percent: Int,
    val global_notification: Boolean,
    val gold: Long,
    val hp_percent: Int,
    val id: Int,
    val level: Int,
    val loggedin: String,
    val max_energy: Int,
    val max_exp: Int,
    val max_hp: Int,
    val maxsteps: Int,
    val menu_lock: MenuLock,
    val messages: Int,
    val stepsleft: Int,
    val user_number: String,
    val username: String
)

data class MenuLock(
    val battle: Int,
    val collections: Int,
    val crafting: Int,
    val guilds: Int,
    val jobs: Int,
    val quests: Int,
    val tasks: Int
)

