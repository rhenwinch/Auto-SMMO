package com.xcape.simplemmomod.data.smmo_tasks

import android.util.Log
import com.xcape.simplemmomod.common.Constants.APP_TAG
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.AutoSMMOLogger
import javax.inject.Inject

class AutoSMMOLoggerImpl @Inject constructor(
    private val userRepository: UserRepository
) : AutoSMMOLogger {
    override suspend fun log(message: String, tag: String, user: User?): User {
        var newUser = when(user) {
            null -> userRepository.getLoggedInUser()!!
            else -> user
        }

        Log.d(APP_TAG, message)
        newUser = newUser.copy(travelLog = "${newUser.travelLog}\n$message")
        userRepository.updateUser(user = newUser)

        return newUser
    }
}