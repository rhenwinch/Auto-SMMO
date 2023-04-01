package com.xcape.simplemmomod.data.smmo_tasks

import com.xcape.simplemmomod.common.Endpoints.API_HOST
import com.xcape.simplemmomod.common.Endpoints.ATTACK_NPC_URL
import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.common.Endpoints.BATTLE_URL
import com.xcape.simplemmomod.common.Endpoints.GENERATE_NPC_URL
import com.xcape.simplemmomod.common.Functions.getStringInBetween
import com.xcape.simplemmomod.common.Functions.toJson
import com.xcape.simplemmomod.common.Parser.parseItemLoot
import com.xcape.simplemmomod.common.Parser.parseNpcRewards
import com.xcape.simplemmomod.data.dto.BattleResultDto
import com.xcape.simplemmomod.data.dto.NpcDto
import com.xcape.simplemmomod.data.dto.toBattleResult
import com.xcape.simplemmomod.data.dto.toNpc
import com.xcape.simplemmomod.data.remote.AutoSMMORequest
import com.xcape.simplemmomod.domain.model.Npc
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.ArenaActions
import com.xcape.simplemmomod.domain.smmo_tasks.AutoSMMOLogger
import com.xcape.simplemmomod.domain.smmo_tasks.LootActions
import kotlinx.coroutines.delay
import okhttp3.FormBody
import okhttp3.Headers
import javax.inject.Inject

class CannotGenerateNpc(override val message: String = "Unknown error generating npc"): Exception(message)
class CannotAttackNpc(override val message: String = "Unknown error attacking npc"): Exception(message)

class ArenaActionsImpl @Inject constructor(
    private val autoSMMORequest: AutoSMMORequest,
    private val userRepository: UserRepository,
    private val lootActions: LootActions,
    private val autoSMMOLogger: AutoSMMOLogger
) : ArenaActions {
    override suspend fun generateNpc(): Npc {
        try {
            val user = userRepository.getLoggedInUser()!!

            val humanizedHeaders = Headers.Builder()
                .add("Accept", "*/*")
                .add("Host", API_HOST)
                .add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .add("Origin", BASE_URL)
                .add("Referer", BATTLE_URL)
                .add("x-requested-with", PACKAGE_NAME)
                .add("Connection", "keep-alive")
                .add("Sec-Fetch-Dest", "empty")
                .add("Sec-Fetch-Mode", "cors")
                .add("Sec-Fetch-Site", "same-site")
                .add("User-Agent", user.userAgent)
                .build()

            val humanizedData = FormBody.Builder()
                .add("api_token", user.apiToken)
                .build()

            val (_, responseString) = autoSMMORequest.post(
                url = GENERATE_NPC_URL,
                body = humanizedData,
                headers = humanizedHeaders
            )

            return responseString.toJson(NpcDto::class.java).toNpc()
        } catch (e: Exception) {
            throw CannotGenerateNpc(message = "Could not generate NPC: ${e.localizedMessage}")
        }
    }

    override suspend fun attackNpc(
        npcId: String,
        verifyCallback: suspend () -> Unit,
        shouldAutoEquip: Boolean,
        isUserTravelling: Boolean,
    ): Long {
        var user = userRepository.getLoggedInUser()!!
        val attackNpcUrl = String.format(ATTACK_NPC_URL,
            getStringInBetween(
                string = npcId,
                delimiter1 = "/attack/",
                delimiter2 = "?"
            )
        )

        var isOpponentDefeated = false
        while (!isOpponentDefeated) {
            try {
                val humanizedHeaders = Headers.Builder()
                    .add("Accept", "*/*")
                    .add("Host", API_HOST)
                    .add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .add("Origin", BASE_URL)
                    .add("Referer", npcId)
                    .add("x-requested-with", PACKAGE_NAME)
                    .add("Connection", "keep-alive")
                    .add("Sec-Fetch-Dest", "empty")
                    .add("Sec-Fetch-Mode", "cors")
                    .add("Sec-Fetch-Site", "same-site")
                    .add("User-Agent", user.userAgent)
                    .build()

                val humanizedData = FormBody.Builder()
                    .add("_token", user.csrfToken)
                    .add("api_token", user.apiToken)
                    .add("special_attack", "false")
                    .build()

                val (newCookie, responseString) = autoSMMORequest.post(
                    url = attackNpcUrl,
                    body = humanizedData,
                    headers = humanizedHeaders
                )

                val battleResult = responseString.toJson(BattleResultDto::class.java).toBattleResult()

                val isUserDefeated = battleResult.playerHp == 0
                if(isUserDefeated)
                    throw TravellerDead("User is dead!")

                isOpponentDefeated = battleResult.opponentHp == 0
                if(isOpponentDefeated) {
                    val battleMessage = battleResult.result!!
                    val battleRewards = parseNpcRewards(toParse = battleMessage).trim()

                    val needsVerification = battleMessage.contains(BOT_RESPONSE)
                    if(needsVerification) {
                        isOpponentDefeated = false
                        verifyCallback()
                        continue
                    }

                    user = autoSMMOLogger.log(
                        message = "> You've won! Rewards: $battleRewards",
                        user = user
                    )

                    val isItemAReward = battleMessage.contains("retrieveItem")
                    if (isItemAReward) {
                        val (_, itemId) = parseItemLoot(battleMessage, isFromNpc = true)

                        val newEquippableItems = user.toEquipItems + itemId
                        user = user.copy(
                            cookie = newCookie,
                            toEquipItems = newEquippableItems,
                            dailyItemsFound = user.dailyItemsFound + 1,
                            totalItemsFound = user.totalItemsFound + 1
                        )
                        userRepository.updateUser(user = user)

                        if(shouldAutoEquip) {
                            autoSMMOLogger.log(
                                message = "> Checking if there's previous items found...",
                                user = user
                            )

                            newEquippableItems.reversed().forEach {
                                if (lootActions.shouldEquipItem(itemId = it)) {
                                    lootActions.equipItem(itemId = it)
                                }
                            }
                        }
                    } else {
                        userRepository.updateUser(
                            user = user.copy(
                                cookie = newCookie,
                                dailyBattles = user.dailyBattles + 1,
                                totalBattles = user.totalBattles + 1,
                            )
                        )
                    }
                }

                delay((1000L..2500L).random())
            }
            catch (e: Exception) {
                throw CannotAttackNpc(message = "Could not attack NPC: ${e.localizedMessage}")
            }
        }

        return (1000L..2500L).random()
    }
}