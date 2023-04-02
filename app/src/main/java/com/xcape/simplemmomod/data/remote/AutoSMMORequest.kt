package com.xcape.simplemmomod.data.remote

import com.xcape.simplemmomod.domain.model.AutoSMMOResponse
import com.xcape.simplemmomod.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import javax.inject.Inject

val JSON_BODY_TYPE: MediaType = "application/json; charset=utf-8".toMediaType()

class AutoSMMORequest @Inject constructor(
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val client = OkHttpClient()

    suspend fun get(url: String, headers: Headers): AutoSMMOResponse {
        return withContext(ioDispatcher) {
            val user = userRepository.getLoggedInUser()!!

            var request = when(headers.size > 0) {
                true -> Request.Builder().url(url).headers(headers)
                false -> Request.Builder().url(url)
            }

            user.cookie.split("; ").forEach { cookie ->
                request = request.addHeader("Cookie", cookie)
            }

            val response = client.newCall(request.build()).execute()

            if (!response.isSuccessful)
                throw IOException("Unexpected code $response")

            val newCookies = response.headers.values("Set-Cookie")

            AutoSMMOResponse(
                newCookies.joinToString("; ") { it.split(";")[0] },
                response.body.byteStream().bufferedReader().use { it.readText() }
            )
        }
    }

    suspend fun post(url: String, body: RequestBody, headers: Headers, ): AutoSMMOResponse {
        return withContext(ioDispatcher) {
            val user = userRepository.getLoggedInUser()!!

            var request = when (headers.size > 0) {
                true -> Request.Builder().url(url).headers(headers)
                false -> Request.Builder().url(url)
            }.post(body)

            user.cookie.split("; ").forEach { cookie ->
                request = request.addHeader("Cookie", cookie)
            }

            val response = client.newCall(request.build()).execute()

            if (!response.isSuccessful)
                throw IOException("Unexpected code $response")

            val newCookies = response.headers.values("Set-Cookie")
            userRepository.updateUser(
                user = user.copy(
                    cookie = newCookies.joinToString("; ") { it.split(";")[0] }
                )
            )

            AutoSMMOResponse(
                newCookies.joinToString("; ") { it.split(";")[0] },
                response.body.byteStream().bufferedReader().use { it.readText() }
            )
        }
    }
}