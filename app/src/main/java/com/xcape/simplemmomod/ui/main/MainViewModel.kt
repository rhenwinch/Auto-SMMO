package com.xcape.simplemmomod.ui.main

import android.webkit.CookieManager
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.simplemmomod.domain.model.AppPreferences
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserApiServiceRepository
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.ui.common.UserAgentGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userApiService: UserApiServiceRepository,
    private val dataStore: DataStore<AppPreferences>
) : ViewModel() {
    val appState = dataStore.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = AppPreferences()
    )

    private val isLoggingInState = MutableStateFlow(false)

    val user = userRepository
        .getFlowLoggedInUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    init {
        viewModelScope.launch {
            launch {
                appState.collect {
                    isLoggingInState.first { !isLoggingInState.value }
                    if(it.userAgent.isEmpty()) {
                        val userAgent = UserAgentGenerator.generate()
                        updateUserAgentPreference(userAgent = userAgent)
                    }
                }
            }

            launch {
                fetchUser()
            }
        }
    }

    fun onEvent(event: MainUiEvent) {
        when(event) {
            is MainUiEvent.ChangedWebsite -> {
                viewModelScope.launch {
                    setCookie(cookie = event.newCookie)
                }
            }
            is MainUiEvent.ClickedLogoutButton -> {
                viewModelScope.launch {
                    logout()

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.removeAllCookies(null)
                    cookieManager.flush()
                }
            }
            is MainUiEvent.ClickedNavigationDrawer -> {
                viewModelScope.launch {
                    if(event.isOpen) {
                        fetchUser(isForced = true)
                    }
                }
            }
        }
    }

    private suspend fun getUserCredentials(
        cookie: String,
        ignoreTokenUpdate: Boolean = false
    ): User? {
        return try {
            var userToken = user.value?.apiToken
            if(!ignoreTokenUpdate || userToken == null || userToken.isEmpty()) {
                userToken = userApiService.getUserToken(cookie = cookie)
            }

            var userCsrfToken = user.value?.csrfToken
            if(userCsrfToken == null || userCsrfToken.isEmpty()) {
                userCsrfToken = userApiService.getCsrfToken(
                    cookie = cookie,
                    apiToken = userToken,
                    userAgent = appState.value.userAgent
                )
            }

            val updatedUser = userApiService.getUser(
                cookie = cookie,
                apiToken = userToken,
                userAgent = appState.value.userAgent
            )

            var toReturn = user.value
            if(toReturn == null) {
                toReturn = User()
            }

            toReturn.copy(
                characterUpgrades = updatedUser.characterUpgrades,
                notifications = updatedUser.notifications,
                messages = updatedUser.messages,
                userAgent = appState.value.userAgent,
                cookie = cookie,
                apiToken = userToken,
                csrfToken = userCsrfToken,
                username = updatedUser.username,
                id = updatedUser.id,
                gold = updatedUser.gold,
                level = updatedUser.level,
                avatar = updatedUser.avatar,
                totalSteps = updatedUser.totalSteps,
                maxBattleEnergy = updatedUser.maxBattleEnergy,
                maxQuestEnergy = updatedUser.maxQuestEnergy,
                battleEnergy = updatedUser.battleEnergy,
                questEnergy = updatedUser.questEnergy,
            )
        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun setCookie(cookie: String) {
        user.value?.copy(cookie = cookie)?.let {
            userRepository.updateUser(user = it)
        }
    }

    fun login(cookie: String) {
        viewModelScope.launch {
            isLoggingInState.update { true }

            var user = getUserCredentials(cookie)

            user?.let {
                user = it.copy(loggedIn = true)

                val cachedUser = userRepository.getUserById(user!!.id)
                if(cachedUser != null) {
                    val newLoggedInUser = cachedUser.copy(loggedIn = true)
                    userRepository.updateUser(user = newLoggedInUser)

                    updatePreferences(
                        userId = newLoggedInUser.id,
                        userAgent = newLoggedInUser.userAgent
                    )
                    isLoggingInState.update { false }
                } else {
                    updatePreferences(
                        userId = user!!.id,
                        userAgent = appState.value.userAgent
                    )

                    userRepository.addUser(user = user!!)
                }
            }

            isLoggingInState.update { false }
        }
    }

    private suspend fun logout() {
        user.value?.let { 
            userRepository.updateUser(user = it.copy(loggedIn = false))
        }
        updatePreferences(userAgent = appState.value.userAgent)
    }

    private suspend fun fetchUser(isForced: Boolean = false) {
        if(appState.value.userIdToUse == 0)
            return

        val user = userRepository.getUserById(appState.value.userIdToUse)

        user?.let {
            val isUserRecentlySignedIn = it.loggedIn && it.apiToken.isEmpty()

            if(isUserRecentlySignedIn || isForced) {
                var newUser = getUserCredentials(
                    cookie = it.cookie,
                    ignoreTokenUpdate = isForced
                )!!

                newUser = newUser.copy(loggedIn = appState.value.userIdToUse == newUser.id)
                userRepository.updateUser(newUser)
            }
        }
    }

    private suspend fun updatePreferences(
        userId: Int = 0,
        userAgent: String = ""
    ) {
        dataStore.updateData {
            it.copy(
                userIdToUse = userId,
                userAgent = userAgent
            )
        }
    }

    suspend fun updateUserAgentPreference(userAgent: String = "") {
        dataStore.updateData {
            it.copy(userAgent = userAgent)
        }
    }
}
