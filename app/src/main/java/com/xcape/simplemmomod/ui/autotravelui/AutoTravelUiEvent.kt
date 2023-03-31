package com.xcape.simplemmomod.ui.autotravelui

import com.xcape.simplemmomod.common.SkillType

sealed class AutoTravelUiEvent {
    data class Play(val isPausing: Boolean = false): AutoTravelUiEvent()
    data class ChangeToUpgradeSkill(val skillType: SkillType = SkillType.DEX): AutoTravelUiEvent()
    data class IgnoreNpcClick(val isDisabling: Boolean): AutoTravelUiEvent()
    data class AutoEquipClick(val isDisabling: Boolean): AutoTravelUiEvent()
    object ConsumeError: AutoTravelUiEvent()
}