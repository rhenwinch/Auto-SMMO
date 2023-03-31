package com.xcape.simplemmomod.domain.smmo_tasks

import com.xcape.simplemmomod.common.Constants.APP_TAG
import com.xcape.simplemmomod.domain.model.User

interface AutoSMMOLogger {
    /* returns updated user */
    suspend fun log(
        message: String,
        tag: String = APP_TAG,
        user: User? = null
    ): User
}