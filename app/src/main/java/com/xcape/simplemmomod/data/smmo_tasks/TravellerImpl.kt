package com.xcape.simplemmomod.data.smmo_tasks

import com.xcape.simplemmomod.common.Endpoints.API_HOST
import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.common.Endpoints.STEP_URL
import com.xcape.simplemmomod.common.Endpoints.TRAVEL_URL
import com.xcape.simplemmomod.common.Endpoints.UPGRADE_SKILL_URL
import com.xcape.simplemmomod.common.Functions.getTimeInMilliseconds
import com.xcape.simplemmomod.common.Functions.toJson
import com.xcape.simplemmomod.common.Parser.isUserDead
import com.xcape.simplemmomod.common.Parser.isUserNotVerified
import com.xcape.simplemmomod.common.Parser.isUserOnAJob
import com.xcape.simplemmomod.common.Parser.isUserOnANewLevel
import com.xcape.simplemmomod.common.Parser.parseItemLoot
import com.xcape.simplemmomod.common.Parser.parseMaterialLoot
import com.xcape.simplemmomod.common.Parser.parseNpcFound
import com.xcape.simplemmomod.common.Parser.shouldWaitMore
import com.xcape.simplemmomod.common.SkillType
import com.xcape.simplemmomod.common.StepType
import com.xcape.simplemmomod.common.StepType.Companion.toStepType
import com.xcape.simplemmomod.data.dto.UpgradeSkillDto
import com.xcape.simplemmomod.data.dto.toUpgradeResponse
import com.xcape.simplemmomod.data.remote.AutoSMMORequest
import com.xcape.simplemmomod.data.remote.JSON_BODY_TYPE
import com.xcape.simplemmomod.domain.model.StepResult
import com.xcape.simplemmomod.domain.repository.UserApiServiceRepository
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.random.Random

const val PACKAGE_NAME = "dawsn.simplemmo"
const val BOT_RESPONSE = "i-am-not-a-bot"

class TravellerOnJob(override val message: String): Exception()
class TravellerDead(override val message: String): Exception()

class TravellerImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userApiServiceRepository: UserApiServiceRepository,
    private val autoSMMORequest: AutoSMMORequest,
    private val autoSMMOLogger: AutoSMMOLogger,
    private val lootActions: LootActions,
    private val npcActions: NpcActions,
    private val questActions: QuestActions
) : Traveller {
    override var energyTimer: Long = getTimeInMilliseconds()

    override suspend fun verify() {
        val user = userRepository.getLoggedInUser()!!

        userRepository.updateUser(
            user = user.copy(needsVerification = true)
        )

        userRepository.getFlowLoggedInUser().first { it?.needsVerification == false }
    }

    override suspend fun takeStep(
        shouldAutoEquip: Boolean,
        shouldSkipNPCs: Boolean,
    ): Long {
        var user = userRepository.getLoggedInUser()!!

        val humanizedX = Random.nextInt(50, 188)
        val humanizedY = Random.nextInt(350, 389)

        val humanizedHeaders = Headers.Builder()
            .add("Accept", "*/*")
            .add("Host", API_HOST)
            .add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            .add("Origin", BASE_URL)
            .add("Referer", TRAVEL_URL)
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
            .add("d_1", humanizedX.toString())
            .add("d_2", humanizedY.toString())
            .add("s", "false")
            .add("travel_id", "0")
            .build()

        val (newCookie, responseString) = autoSMMORequest.post(
            url = STEP_URL,
            body = humanizedData,
            headers = humanizedHeaders
        )

        val stepResult  = responseString.toJson(StepResult::class.java)
        val travelText = stepResult.text

        if(isUserNotVerified(travelText = travelText)) {
            verify()
            return stepResult.wait_length
        }

        if(shouldWaitMore(travelText = travelText)) {
            return stepResult.wait_length
        }

        if(isUserOnAJob(travelText = travelText)) {
            autoSMMOLogger.log(
                message = "======!! User On Job !!======",
                user = user
            )
            throw TravellerOnJob("Cannot travel while on a job!")
        }

        if(isUserDead(travelText = travelText)) {
            autoSMMOLogger.log(
                message = "======!! No health !!======",
                user = user
            )
            throw TravellerDead("Cannot travel without health!")
        }

        val travelHeadline = stepResult.heading
        val stepType = stepResult.step_type.toStepType()
        val currentLevel = stepResult.level
        val currentExp = stepResult.currentEXP
        val currentGold = stepResult.currentGold

        if(isUserOnANewLevel(oldLevel = user.level, newLevel = currentLevel)) {
            user = autoSMMOLogger.log(
                message = "> New level! -> ${user.level} > $currentLevel",
                user = user
            )
        }

        user = user.copy(
            cookie = newCookie,
            level = currentLevel,
            gold = currentGold,
            dailySteps = user.dailySteps + 1,
            totalSteps = user.totalSteps + 1
        )
        userRepository.updateUser(user = user)

        val goldEarned = stepResult.gold_amount
        val expEarned = stepResult.exp_amount

        var waitTime = stepResult.wait_length
        when(stepType) {
            StepType.MATERIAL -> {
                val (materialLevelAndRarity, materialId) = parseMaterialLoot(toParse = travelText)

                autoSMMOLogger.log(
                    message = "[STEP #${user.dailySteps}] You've found (Material): $travelHeadline [$materialLevelAndRarity]",
                    user = user
                )

                val canBeGathered = !travelText.contains("Your skill level isn't high enough")
                if(canBeGathered) {
                    waitTime += lootActions.obtainMaterials(materialId = materialId)
                }
            }
            StepType.TEXT -> {
                autoSMMOLogger.log(
                    message = "[STEP #${user.dailySteps}] $travelHeadline",
                    user = user
                )
            }
            StepType.ITEM -> {
                val (itemName, itemId) = parseItemLoot(toParse = travelText)

                val newEquippableItems = user.toEquipItems.toMutableList()
                newEquippableItems.add(itemId)

                user = user.copy(
                    toEquipItems = newEquippableItems,
                    dailyItemsFound = user.dailyItemsFound + 1,
                    totalItemsFound = user.totalItemsFound + 1
                )
                userRepository.updateUser(user = user)

                user = autoSMMOLogger.log(
                    message = "[STEP #${user.dailySteps}] You've found (Item): $itemName",
                    user = user
                )

                if(shouldAutoEquip) {
                    user = autoSMMOLogger.log(
                        message = "> Checking if there's previous items found...",
                        user = user
                    )

                    user.toEquipItems.reversed().forEach {
                        if (lootActions.shouldEquipItem(itemId = it)) {
                            lootActions.equipItem(itemId = it)
                        }
                    }
                }
            }
            StepType.NPC -> {
                val (npcLevel, npcId) = parseNpcFound(toParse = travelText)

                autoSMMOLogger.log(
                    message = "[STEP #${user.dailySteps}] You've found (NPC): $travelHeadline ($npcLevel)",
                    user = user
                )

                if(!shouldSkipNPCs)
                    waitTime += npcActions.attackNpc(
                        npcId = npcId,
                        verifyCallback = { verify() },
                        shouldAutoEquip = shouldAutoEquip
                    )
            }
            StepType.PLAYER -> {
                autoSMMOLogger.log(
                    message = "[STEP #${user.dailySteps}] You've encountered (Player): $travelHeadline",
                    user = user
                )
            }
        }


        autoSMMOLogger.log(message = "> Step Rewards [gold/exp]: $goldEarned/$expEarned | Current [gold/exp/level]: $currentGold/$currentExp/$currentLevel")

        val shouldHumanizeStepping = user.dailySteps % (8..16).random() == 0
        if(shouldHumanizeStepping)
            waitTime += (1000L..3000L).random()

        return waitTime
    }

    override suspend fun doQuest(): Long {
        val questId = questActions.getHighestAvailableQuest()

        var user = userRepository.getLoggedInUser()!!
        val userWithEnergy = userApiServiceRepository.getUser(
            cookie = user.cookie,
            apiToken = user.apiToken,
            userAgent = user.userAgent
        )
        user = user.copy(
            gold = userWithEnergy.gold,
            level = userWithEnergy.level,
            maxQuestEnergy = userWithEnergy.maxQuestEnergy,
            maxBattleEnergy = userWithEnergy.maxBattleEnergy,
            battleEnergy = userWithEnergy.battleEnergy,
            questEnergy = userWithEnergy.questEnergy
        )
        userRepository.updateUser(user = user)

        var isUserOutOfEnergy = userWithEnergy.questEnergy == 0
        if(isUserOutOfEnergy) {
            return (800L..1500L).random()
        }

        user = autoSMMOLogger.log(
            message = "[QUEST] Energy: ${userWithEnergy.questEnergy}/${userWithEnergy.maxQuestEnergy}",
            user = user
        )

        var questEnergy = userWithEnergy.questEnergy
        while (questEnergy > 0) {
            val questResponse = questActions.performQuest(
                questId = questId,
                user = user
            )

            val shouldVerify = questResponse.resultText.contains(BOT_RESPONSE) || questResponse.resultText.contains("Press here to verify")

            if(shouldVerify) {
                verify()
                continue
            }

            isUserOutOfEnergy = questResponse.resultText.contains("You have no more quest points")
            if(isUserOutOfEnergy) {
                break
            }

            val toLog = if(questResponse.isQuestFailed) {
                "> Failed Quest Point #$questEnergy/${userWithEnergy.maxQuestEnergy}: ${questResponse.resultText}"
            } else {
                "> Quest Point #${questEnergy}/${userWithEnergy.maxQuestEnergy}: ${questResponse.status} -> ${questResponse.gold} gold and ${questResponse.exp} exp"
            }

            user = autoSMMOLogger.log(
                message = toLog,
                user = user
            )

            user = user.copy(
                dailyQuests = user.dailyQuests + 1,
                totalQuests = user.totalQuests + 1
            )
            userRepository.updateUser(user = user)

            questEnergy = questResponse.questPoints!!
            delay((1500L..2500L).random())
        }

        userRepository.updateUser(user = user)

        return (800L..1500L).random()
    }

    override suspend fun doArena(
        shouldAutoEquip: Boolean,
        shouldSkipNPCs: Boolean
    ): Long {
        if(shouldSkipNPCs)
            return (800L..1500L).random()

        var user = userRepository.getLoggedInUser()!!
        val userWithEnergy = userApiServiceRepository.getUser(
            cookie = user.cookie,
            apiToken = user.apiToken,
            userAgent = user.userAgent
        )

        user = user.copy(
            gold = userWithEnergy.gold,
            level = userWithEnergy.level,
            maxQuestEnergy = userWithEnergy.maxQuestEnergy,
            maxBattleEnergy = userWithEnergy.maxBattleEnergy,
            battleEnergy = userWithEnergy.battleEnergy,
            questEnergy = userWithEnergy.questEnergy,
            currentHealthPercentage = userWithEnergy.currentHealthPercentage
        )
        userRepository.updateUser(user = user)

        val isUserOutOfEnergy = user.battleEnergy == 0
        if(isUserOutOfEnergy) {
            return (800L..1500L).random()
        }

        user = autoSMMOLogger.log(
            message = "[ARENA] Energy: ${user.battleEnergy}/${userWithEnergy.maxBattleEnergy}",
            user = user
        )

        var battleEnergy = user.battleEnergy
        while (battleEnergy > 0) {
            val npc = npcActions.generateNpc()
            if(npc.result == "You do not have enough energy to do this.") {
                return (800L..1500L).random()
            }

            val userWithHealth = userApiServiceRepository.getUser(
                cookie = user.cookie,
                apiToken = user.apiToken,
                userAgent = user.userAgent
            )
            user = userRepository.getLoggedInUser()!!.copy(currentHealthPercentage = userWithHealth.currentHealthPercentage)
            userRepository.updateUser(user = user)

            val shouldRetreat = npcActions.attackNpc(
                npcId = npc.id.toString(),
                npc = npc,
                verifyCallback = { verify() },
                shouldAutoEquip = shouldAutoEquip,
                isUserTravelling = false
            ) == -1L

            if(shouldRetreat) {
                return -1L
            }

            battleEnergy -= 1
            delay((1500L..3000L).random())
        }

        return (800L..1500L).random()
    }

    override suspend fun upgradeSkill(skillToUpgrade: SkillType): Long {
        var user = userRepository.getLoggedInUser()!!
        val userWithEnergy = userApiServiceRepository.getUser(
            cookie = user.cookie,
            apiToken = user.apiToken,
            userAgent = user.userAgent
        )

        user = user.copy(
            gold = userWithEnergy.gold,
            level = userWithEnergy.level,
            maxQuestEnergy = userWithEnergy.maxQuestEnergy,
            maxBattleEnergy = userWithEnergy.maxBattleEnergy,
            battleEnergy = userWithEnergy.battleEnergy,
            questEnergy = userWithEnergy.questEnergy,
            characterUpgrades = userWithEnergy.characterUpgrades
        )
        userRepository.updateUser(user = user)

        val remainingSkillPoints = userWithEnergy.characterUpgrades
        val noUpgradesAvailable = remainingSkillPoints == 0
        if(noUpgradesAvailable) {
            return (800L..1500L).random()
        }

        user = autoSMMOLogger.log(
            message = "[SKILL UPGRADE] $remainingSkillPoints SP remaining",
            user = user
        )

        val humanizedData = ("{\"_token\": \"${user.csrfToken}\", \"amount\": \"${remainingSkillPoints}\"}").toRequestBody(JSON_BODY_TYPE)
        val (cookie, skillUpgradeResponse) = autoSMMORequest.post(
            url = String.format(UPGRADE_SKILL_URL, skillToUpgrade.toString()),
            body = humanizedData,
            headers = Headers.Builder()
                .add("User-Agent", user.userAgent)
                .add("x-requested-with", PACKAGE_NAME)
                .build()
        )

        val skillUpgradeResponseJson = skillUpgradeResponse.toJson(UpgradeSkillDto::class.java).toUpgradeResponse()

        if(skillUpgradeResponseJson.isSuccess) {
             autoSMMOLogger.log(
                message = "> Skill $skillToUpgrade is upgraded by $remainingSkillPoints!",
                user = user.copy(cookie = cookie)
            )

            return (800L..1500L).random()
        }

        throw Exception("Cannot upgrade skill ${skillToUpgrade.toString()
            .uppercase()}")
    }

    override fun resetEnergyTimer() {
        energyTimer = getTimeInMilliseconds() + (300000L..1050000L).random()
    }
}