package com.xcape.simplemmomod.ui.autotravelui

import com.xcape.simplemmomod.common.SkillType

sealed class TravellingStatus {
    object Error: TravellingStatus()
    object Verify: TravellingStatus()
    object NotStarted: TravellingStatus()
    object Idle: TravellingStatus()
    object Initializing: TravellingStatus()
    object Battling: TravellingStatus()
    object Stepping: TravellingStatus()
    object Questing: TravellingStatus()
    data class UpgradeSkill(val skillType: SkillType): TravellingStatus()

    override fun toString(): String {
        return when(this) {
            is Battling -> "Battling"
            is Stepping -> "Stepping"
            is Questing -> "Questing"
            is UpgradeSkill -> "Upgrading ${skillType.toString().uppercase()}"
            is Idle -> "Idling"
            is Initializing -> "Initializing"
            is NotStarted -> "Not Started"
            is Error -> "Error!"
            is Verify -> "Verify Now!"
        }
    }
}

data class AutoTravelUiState(
    val isTravelling: Boolean = false,
    val shouldSkipNpc: Boolean = false,
    val shouldAutoEquipItem: Boolean = true,
    val skillToUpgrade: SkillType = SkillType.DEX,
    val isServiceRunning: Boolean = true,
    val header: TravellingStatus = TravellingStatus.NotStarted,
    val errors: String = ""
)
