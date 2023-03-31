package com.xcape.simplemmomod.ui.account_picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountPickerViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _users = MutableStateFlow(listOf<User>())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    init {
        getAllPreviousLoggedInUsers()
    }

    private fun getAllPreviousLoggedInUsers() {
        viewModelScope.launch {
            _users.update {
                userRepository.getAll()
            }
        }
    }
}