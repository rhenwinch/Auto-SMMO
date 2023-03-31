package com.xcape.simplemmomod.domain.model

data class StepResult(
    val action: String,
    val buttons: Boolean,
    val currentEXP: Long,
    val currentGold: Long,
    val exp_amount: Int,
    val exp_percentage: Int,
    val gold_amount: Int,
    val guild_raid_exp: Any,
    val heading: String,
    val level: Int,
    val modifiers: Modifiers?,
    val resultText: String,
    val rewardAmount: Int,
    val rewardType: String,
    val sprint_expiry: Int,
    val step_type: String,
    val text: String,
    val travel_background: String,
    val userAmount: String,
    val wait_length: Long = 0
)

data class SteppingModifier(
    val amount: Double,
    val reason: List<Any>
)

data class Modifiers(
    val droprate_modifiers: List<DroprateModifier>,
    val exp_modifiers: List<ExpModifier>,
    val gold_modifiers: List<GoldModifier>,
    val material_modifiers: List<MaterialModifier>,
    val stepping_modifiers: List<SteppingModifier>
)

data class MaterialModifier(
    val amount: Double,
    val reason: List<Any>
)

data class GoldModifier(
    val amount: Double,
    val reason: List<Any>
)

data class DroprateModifier(
    val amount: Double,
    val reason: List<Any>
)

data class ExpModifier(
    val amount: Double,
    val reason: List<Any>
)