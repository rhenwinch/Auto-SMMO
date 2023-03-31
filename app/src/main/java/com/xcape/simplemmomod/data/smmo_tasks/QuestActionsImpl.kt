package com.xcape.simplemmomod.data.smmo_tasks

import com.xcape.simplemmomod.common.Endpoints
import com.xcape.simplemmomod.common.Functions.toJson
import com.xcape.simplemmomod.common.Parser
import com.xcape.simplemmomod.data.dto.QuestResponseDto
import com.xcape.simplemmomod.data.dto.toQuestResponse
import com.xcape.simplemmomod.data.remote.AutoSMMORequest
import com.xcape.simplemmomod.domain.model.QuestResult
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.QuestActions
import okhttp3.FormBody
import okhttp3.Headers
import javax.inject.Inject
import kotlin.random.Random

class QuestActionsImpl @Inject constructor(
    private val autoSMMORequest: AutoSMMORequest,
    private val userRepository: UserRepository
) : QuestActions {
    override suspend fun getHighestAvailableQuest(): String {
        val user = userRepository.getLoggedInUser()!!

        val humanizedHeaders = Headers.Builder()
            .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .add("Host", Endpoints.WEB_HOST)
            .add("x-requested-with", PACKAGE_NAME)
            .add("x-simplemmo-token", user.apiToken)
            .add("Connection", "keep-alive")
            .add("Sec-Fetch-Dest", "document")
            .add("Sec-Fetch-Mode", "navigate")
            .add("Sec-Fetch-Site", "none")
            .add("Upgrade-Insecure-Requests", "1")
            .add("Sec-Fetch-User", "?1")
            .add("User-Agent", user.userAgent)
            .build()

        val (_, responseString) = autoSMMORequest.get(
            url = Endpoints.QUEST_URL,
            headers = humanizedHeaders
        )

        return Parser.parseHighestAvailableQuest(toParse = responseString)
    }

    override suspend fun performQuest(questId: String, user: User): QuestResult {
        val humanizedHeaders = Headers.Builder()
            .add("Accept", "*/*")
            .add("Host", Endpoints.API_HOST)
            .add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            .add("Origin", Endpoints.BASE_URL)
            .add("Referer", Endpoints.TRAVEL_URL)
            .add("x-requested-with", PACKAGE_NAME)
            .add("Connection", "keep-alive")
            .add("Sec-Fetch-Dest", "empty")
            .add("Sec-Fetch-Mode", "cors")
            .add("Sec-Fetch-Site", "same-site")
            .add("User-Agent", user.userAgent)
            .build()

        val humanizedX = Random.nextInt(50, 188)
        val humanizedY = Random.nextInt(350, 389)

        val humanizedData = FormBody.Builder()
            .add("api_token", user.apiToken)
            .add("x", humanizedX.toString())
            .add("y", humanizedY.toString())
            .add("s", "0")
            .build()


        val (_, questResponseString) = autoSMMORequest.post(
            url = String.format(Endpoints.QUEST_ACTION_URL, questId),
            body = humanizedData,
            headers = humanizedHeaders
        )

        return questResponseString.toJson(QuestResponseDto::class.java).toQuestResponse()
    }
}