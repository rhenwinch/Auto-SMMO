package com.xcape.simplemmomod.ui.autotravelui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.services.TravellerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AutoTravelViewModel @Inject constructor(
    private val userRepository: UserRepository,
    io: CoroutineDispatcher
) : ViewModel() {
    val state = TravellerForegroundService.exposedState

    val user: StateFlow<User?> = userRepository
        .getFlowLoggedInUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = runBlocking(io) { userRepository.getLoggedInUser() }
        )

    fun resetUserDailies() {
        viewModelScope.launch {
            val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val timeToMatch = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            timeToMatch[Calendar.HOUR_OF_DAY] = 12
            timeToMatch[Calendar.MINUTE] = 0

            if(currentTime.timeInMillis >= timeToMatch.timeInMillis) {
                timeToMatch.add(Calendar.DATE, 1)
            }

            val timeDifference = timeToMatch.timeInMillis - currentTime.timeInMillis
            val updatedUser = user.value!!.copy(
                dailyBattles = 0,
                dailyItemsFound = 0,
                dailyMaterialsFound = 0,
                dailyQuests = 0,
                dailyResetTime = currentTime.timeInMillis + timeDifference,
                dailySteps = 0,
                travelLog = ""
            )

            userRepository.updateUser(user = updatedUser)
        }
    }
}