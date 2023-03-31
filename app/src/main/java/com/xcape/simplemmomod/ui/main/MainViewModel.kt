package com.xcape.simplemmomod.ui.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.simplemmomod.common.Constants.APP_TAG
import com.xcape.simplemmomod.common.Endpoints
import com.xcape.simplemmomod.data.dto.toUser
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

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dataStore.data.collect {
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
                    setLoginState(
                        isLoggingIn = event.isLoggedIn,
                        cookie = event.cookie
                    )
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
    ): User {
        var userToken = _user.value.apiToken
        if(!ignoreTokenUpdate) {
            userToken = userApiService.getUserToken(cookie = cookie)
        }

        var userCsrfToken = _user.value.csrfToken
        if(userCsrfToken.isEmpty()) {
            userCsrfToken = userApiService.getCsrfToken(
                cookie = cookie,
                apiToken = userToken,
                userAgent = _user.value.userAgent
            )
        }

        val user = userApiService.getUser(
            cookie = cookie,
            apiToken = userToken,
            userAgent = appState.value.userAgent
        )

        return _user.value.copy(
            characterUpgrades = user.characterUpgrades,
            notifications = user.notifications,
            messages = user.messages,
            userAgent = appState.value.userAgent,
            cookie = cookie,
            apiToken = userToken,
            csrfToken = userCsrfToken,
            username = user.username,
            id = user.id,
            gold = user.gold,
            level = user.level,
            avatar = user.avatar,
            totalSteps = user.totalSteps,
            maxBattleEnergy = user.maxBattleEnergy,
            maxQuestEnergy = user.maxQuestEnergy,
            battleEnergy = user.battleEnergy,
            questEnergy = user.questEnergy,
        )
    }

    private suspend fun setCookie(cookie: String) {
        userRepository.updateUser(
            user = _user.value.copy(cookie = cookie)
        )
    }

    private suspend fun setLoginState(
        isLoggingIn: Boolean,
        cookie: String
    ) {
        if(isLoggingIn) {
            isLoggingInState.update { true }

            val user = getUserCredentials(cookie).copy(loggedIn = true)

            userRepository.getUserById(user.id)?.let { cachedUser ->
                val newLoggedInUser = cachedUser.copy(loggedIn = true)
                userRepository.updateUser(user = newLoggedInUser)

                _user.update { newLoggedInUser }

                updatePreferences(
                    userId = newLoggedInUser.id,
                    userAgent = newLoggedInUser.userAgent
                )
                isLoggingInState.update { false }
                return
            }

            _user.update { user }

            updatePreferences(
                userId = user.id,
                userAgent = appState.value.userAgent
            )

            userRepository.addUser(user = user)

            isLoggingInState.update { false }
            return
        }

        if(_user.value.username.isNotEmpty()) {
            userRepository.updateUser(
                user = _user.value.copy(loggedIn = false)
            )

            _user.update { User() }

            updatePreferences(userAgent = appState.value.userAgent)
        }
    }

    private suspend fun fetchUser(isForced: Boolean = false) {
        if(appState.value.userIdToUse == 0)
            return

        val user = userRepository.getUserById(appState.value.userIdToUse)

        user?.let {
            val isUserRecentlySignedIn = it.loggedIn && it.apiToken.isEmpty()

            if(isUserRecentlySignedIn || isForced) {
                val newUser = getUserCredentials(
                    cookie = it.cookie,
                    ignoreTokenUpdate = isForced
                ).copy(loggedIn = _user.value.loggedIn)
                _user.update { newUser }
                userRepository.updateUser(newUser)
            } else {
                _user.update { _ -> it }
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
