package com.xcape.simplemmomod.data.smmo_tasks

import android.util.Base64
import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.common.Endpoints.ITEM_EQUIP_URL
import com.xcape.simplemmomod.common.Endpoints.ITEM_STAT_URL
import com.xcape.simplemmomod.common.Endpoints.TRAVEL_URL
import com.xcape.simplemmomod.common.Endpoints.WEB_HOST
import com.xcape.simplemmomod.common.Functions.getStringInBetween
import com.xcape.simplemmomod.common.Functions.toJson
import com.xcape.simplemmomod.common.RarityType
import com.xcape.simplemmomod.data.dto.GatherResultDto
import com.xcape.simplemmomod.data.dto.ItemDto
import com.xcape.simplemmomod.data.dto.toGatherResult
import com.xcape.simplemmomod.data.dto.toItem
import com.xcape.simplemmomod.data.remote.AutoSMMORequest
import com.xcape.simplemmomod.data.remote.JSON_BODY_TYPE
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.AutoSMMOLogger
import com.xcape.simplemmomod.domain.smmo_tasks.LootActions
import kotlinx.coroutines.delay
import okhttp3.Headers
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class CannotGatherMaterials(override val message: String = "Unknown error gathering materials"): Exception(message)

class LootActionsImpl @Inject constructor(
    private val autoSMMORequest: AutoSMMORequest,
    private val autoSMMOLogger: AutoSMMOLogger,
    private val userRepository: UserRepository
) : LootActions {
    override suspend fun obtainMaterials(materialUrl: String): Long {
        var user = userRepository.getLoggedInUser()!!

        var isDoneGathering = false

        val getHeaders = Headers.Builder()
            .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .add("Host", WEB_HOST)
            .add("Referer", TRAVEL_URL)
            .add("x-requested-with", PACKAGE_NAME)
            .add("Connection", "keep-alive")
            .add("Sec-Fetch-Dest", "document")
            .add("Sec-Fetch-Mode", "navigate")
            .add("Sec-Fetch-Site", "same-origin")
            .add("Sec-Fetch-User", "?1")
            .add("User-Agent", user.userAgent)
            .build()

        val (initCookie, gatheringPage) = autoSMMORequest.get(
            url = materialUrl,
            headers = getHeaders
        )

        val materialId = getStringInBetween(
            string = materialUrl,
            delimiter1 = "/gather/",
            delimiter2 = "?"
        )
        val gatherMaterialUrl
            = getStringInBetween(
                gatheringPage,
                "\"gathering.gather_endpoint\":\"",
                "\""
            ).replace("\\u0026", "&")
            .replace("\\", "")

        user = user.copy(cookie = initCookie)
        userRepository.updateUser(user = user)

        var dailyMaterialsFound = user.dailyMaterialsFound
        var totalMaterialsFound = user.totalMaterialsFound
        while (!isDoneGathering) {
            try {
                val humanizedHeaders = Headers.Builder()
                    .add("Accept", "application/json")
                    .add("Host", WEB_HOST)
                    .add("Origin", BASE_URL)
                    .add("Referer", materialId)
                    .add("Authorization", "Bearer ${user.apiToken}")
                    .add("Connection", "keep-alive")
                    .add("Sec-Fetch-Dest", "empty")
                    .add("Sec-Fetch-Mode", "cors")
                    .add("Sec-Fetch-Site", "same-site")
                    .add("User-Agent", user.userAgent)
                    .build()


                val humanizedData = ("{\"quantity\":1,\"id\":$materialId}")
                    .toRequestBody(JSON_BODY_TYPE)

                val (newCookie, responseString) = autoSMMORequest.post(
                    url = gatherMaterialUrl,
                    body = humanizedData,
                    headers = humanizedHeaders
                )

                val result = responseString.toJson(GatherResultDto::class.java).toGatherResult()

                isDoneGathering = result.gatherEnd
                val expGained = result.playerExpGained
                val craftingExpGained = result.craftingExpGained

                val toLog = if(isDoneGathering) {
                    "> Done gathering! Rewards: $expGained EXP | $craftingExpGained Crafting EXP"
                } else {
                    "> Gathering! Rewards: $expGained EXP | $craftingExpGained Crafting EXP"
                }

                user = autoSMMOLogger.log(
                    message = toLog,
                    user = user
                )

                user = user.copy(
                    cookie = newCookie,
                    dailyMaterialsFound = ++dailyMaterialsFound,
                    totalMaterialsFound = ++totalMaterialsFound
                )
                userRepository.updateUser(user = user)

                delay((1000L..2500L).random())
            }
            catch (e: Exception) {
                throw CannotGatherMaterials(message = "Could not gather material: ${e.localizedMessage}")
            }
        }

        return (1000L..2500L).random()
    }

    override suspend fun shouldEquipItem(itemId: String): Boolean {
        var user = userRepository.getLoggedInUser()!!

        val humanizedHeaders = Headers.Builder()
            .add("Accept", "*/*")
            .add("Host", WEB_HOST)
            .add("Origin", BASE_URL)
            .add("Referer", TRAVEL_URL)
            .add("x-requested-with", PACKAGE_NAME)
            .add("Connection", "keep-alive")
            .add("Sec-Fetch-Dest", "empty")
            .add("Sec-Fetch-Mode", "cors")
            .add("Sec-Fetch-Site", "same-site")
            .add("User-Agent", user.userAgent)
            .build()

        val (newCookie, responseString) = autoSMMORequest.post(
            url = String.format(ITEM_STAT_URL, itemId),
            body = "".toRequestBody(),
            headers = humanizedHeaders
        )

        val item = responseString.toJson(ItemDto::class.java).toItem()

        val isItemCelestialOrExotic = item.rarity >= RarityType.CELESTIAL
        val isItemEpicOrAbove = item.rarity >= RarityType.EPIC
        val isItemLevelGreaterThanUserLevel = item.level > user.level

        if(isItemCelestialOrExotic) {
            user = autoSMMOLogger.log(
                message = "=====!! [FOUND A ${item.rarity} -> ${item.name} (${item.type})] !!=====",
                user = user
            )
        }

        val itemName = Base64.decode(item.name, Base64.DEFAULT)
        user = autoSMMOLogger.log(
            message = "> Item Stats: ${String(itemName)} ($itemId) [${item.type} Level ${item.level} ${item.rarity}]",
            user = user
        )

        if(!item.isItemEquippable || item.isFoundItemWorse || item.isItemCurrentlyEquipped || item.isItemATool && !isItemEpicOrAbove) {
            val newEquippableItems = user.toEquipItems.toMutableList()
            newEquippableItems.remove(itemId)

            userRepository.updateUser(
                user = user.copy(
                    toEquipItems = newEquippableItems.toList(),
                    cookie = newCookie
                )
            )
            return false
        }

        if(isItemLevelGreaterThanUserLevel)
            return false

        if(item.isItemATool && isItemEpicOrAbove || item.isFoundItemBetter || item.isEquipSlotEmpty) {
            val newEquippableItems = user.toEquipItems.toMutableList()
            newEquippableItems.remove(itemId)

            userRepository.updateUser(
                user = user.copy(
                    toEquipItems = newEquippableItems.toList(),
                    cookie = newCookie
                )
            )
            return true
        }

        return false
    }

    override suspend fun equipItem(itemId: String) {
        val user = userRepository.getLoggedInUser()!!

        val humanizedHeaders = Headers.Builder()
            .add("Accept", "*/*")
            .add("Host", WEB_HOST)
            .add("Origin", BASE_URL)
            .add("Referer", TRAVEL_URL)
            .add("x-requested-with", PACKAGE_NAME)
            .add("Connection", "keep-alive")
            .add("Sec-Fetch-Dest", "empty")
            .add("Sec-Fetch-Mode", "cors")
            .add("Sec-Fetch-Site", "same-site")
            .add("User-Agent", user.userAgent)
            .build()

        autoSMMORequest.get(
            url = String.format(ITEM_EQUIP_URL, itemId),
            headers = humanizedHeaders
        )
    }
}