package com.xcape.simplemmomod.data.repository

import android.util.Log
import com.xcape.simplemmomod.common.Constants.APP_TAG
import com.xcape.simplemmomod.common.Endpoints.API_HOST
import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.common.Endpoints.HOME_URL
import com.xcape.simplemmomod.common.Functions.getStringInBetween
import com.xcape.simplemmomod.data.dto.toUser
import com.xcape.simplemmomod.data.remote.AutoSMMORequest
import com.xcape.simplemmomod.data.remote.JSON_BODY_TYPE
import com.xcape.simplemmomod.data.remote.UserApiService
import com.xcape.simplemmomod.data.remote.UserInfoBody
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserApiServiceRepository
import okhttp3.Headers
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserApiServiceRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : UserApiServiceRepository {
    override suspend fun getUserToken(cookie: String): String {
        return try {
            userApiService.getUserToken(
                cookie = cookie
            ).api_token!!
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override suspend fun getUser(
        cookie: String,
        apiToken: String,
        userAgent: String
    ): User {
        return try {
            val userEvents = userApiService.getUserEvents(
                cookie = cookie,
                apiToken = apiToken
            )

            userApiService.getUserInfo(
                cookie = cookie,
                body = UserInfoBody(api_token = apiToken),
                userAgent = userAgent
            )
                .toUser()
                .copy(
                    characterUpgrades = userEvents.character_badge,
                    notifications = userEvents.events,
                    messages = userEvents.messages,
                )
        } catch (e: Exception) {
            e.printStackTrace()
            return User()
        }
    }

    override suspend fun getCsrfToken(
        cookie: String,
        apiToken: String,
        userAgent: String,
    ): String {
        return try {
            val result = userApiService.getCsrfToken(
                cookie = cookie,
                apiToken = apiToken,
                userAgent = userAgent
            )

            getStringInBetween(
                string = result,
                delimiter1 = "<meta name=\"csrf-token\" content=\"",
                delimiter2 = "\""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}