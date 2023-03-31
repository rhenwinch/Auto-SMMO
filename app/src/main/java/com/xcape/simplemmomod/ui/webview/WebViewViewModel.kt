package com.xcape.simplemmomod.ui.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebViewViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    val user: StateFlow<User?> = userRepository
        .getFlowLoggedInUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = User()
        )

    fun onEvent(event: WebViewUiEvent) {
        when(event) {
            is WebViewUiEvent.ChangedWebsite -> {
                viewModelScope.launch {
                    setCookie(cookie = event.newCookie)
                }
            }
            WebViewUiEvent.UserVerified -> {
                viewModelScope.launch {
                    userRepository.updateUser(
                        user = user.value!!.copy(needsVerification = false)
                    )
                }
            }
        }
    }

    private suspend fun setCookie(cookie: String) {
        val user = userRepository.getLoggedInUser()!!

        userRepository.updateUser(
            user = user.copy(cookie = cookie)
        )
    }
}