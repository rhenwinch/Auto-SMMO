package com.xcape.simplemmomod.domain.repository

import com.xcape.simplemmomod.domain.model.User

interface UserApiServiceRepository {
    suspend fun getUser(
        cookie: String,
        apiToken: String,
        userAgent: String
    ): User
    suspend fun getUserToken(cookie: String): String
    suspend fun getCsrfToken(
        cookie: String,
        apiToken: String,
        userAgent: String
    ): String
}