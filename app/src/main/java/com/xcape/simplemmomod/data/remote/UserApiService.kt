package com.xcape.simplemmomod.data.remote

import com.xcape.simplemmomod.common.Endpoints.API_TOKEN_URL
import com.xcape.simplemmomod.common.Endpoints.HOME_URL
import com.xcape.simplemmomod.common.Endpoints.USER_INFO_URL
import com.xcape.simplemmomod.common.Endpoints.USER_EVENTS_URL
import com.xcape.simplemmomod.data.dto.UserEventsDto
import com.xcape.simplemmomod.data.dto.UserInfoBody
import com.xcape.simplemmomod.data.dto.UserInfoDto
import com.xcape.simplemmomod.data.dto.UserTokenDto
import com.xcape.simplemmomod.data.smmo_tasks.PACKAGE_NAME
import retrofit2.http.*

interface UserApiService {
    @POST
    suspend fun getUserInfo(
        @Url url: String = USER_INFO_URL,
        @Body body: UserInfoBody,
        @Header("x-requested-with") packageName: String = PACKAGE_NAME,
        @Header("Cookie") cookie: String,
        @Header("User-Agent") userAgent: String,
    ): UserInfoDto

    @FormUrlEncoded
    @POST
    suspend fun getUserEvents(
        @Url url: String = USER_EVENTS_URL,
        @Header("Cookie") cookie: String,
        @Field("api_token") apiToken: String
    ): UserEventsDto

    @GET
    suspend fun getUserToken(
        @Url url: String = API_TOKEN_URL,
        @Header("Cookie") cookie: String
    ): UserTokenDto

    @GET
    suspend fun getCsrfToken(
        @Url url: String = HOME_URL,
        @Header("Cookie") cookie: String,
        @Header("x-simplemmo-token") apiToken: String,
        @Header("User-Agent") userAgent: String,
        @Header("x-requested-with") packageName: String = PACKAGE_NAME,
    ): String
}