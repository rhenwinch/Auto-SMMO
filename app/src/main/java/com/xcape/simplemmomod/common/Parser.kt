package com.xcape.simplemmomod.common

import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.common.Functions.getStringInBetween
import com.xcape.simplemmomod.common.Functions.removeHtmlTags
import com.xcape.simplemmomod.data.smmo_tasks.BOT_RESPONSE

object Parser {
    fun parseHighestAvailableQuest(toParse: String): String {
        return getStringInBetween(
            string = toParse,
            delimiter1 = "if (!window.__cfRLUnblockHandlers) return false; window.location='/quests/view/",
            delimiter2 = "?"
        )
    }

    fun parseMaterialLoot(toParse: String): Pair<String, String> {
        val materialLevelAndRarity = removeHtmlTags(
            rawHtml = getStringInBetween(
                string = toParse,
                delimiter1 = "<br/>",
                delimiter2 = "</span>"
            )
        ).trim()

        val materialId = BASE_URL + getStringInBetween(
            string = toParse,
            delimiter1 = "document.location='",
            delimiter2 = "'"
        )

        return Pair(materialLevelAndRarity, materialId)
    }

    fun parseItemLoot(toParse: String, isFromNpc: Boolean = false): Pair<String, String> {
        if(isFromNpc) {
            val itemId = getStringInBetween(
                string = toParse,
                delimiter1 = "retrieveItem(",
                delimiter2 = ")"
            )
            return Pair("", itemId)
        }

        val itemName = removeHtmlTags(toParse).trim()
        val itemId = getStringInBetween(
            string = toParse,
            delimiter1 = "retrieveItem(",
            delimiter2 = ","
        )
        return Pair(itemName, itemId)
    }

    fun parseNpcFound(toParse: String): Pair<String, String> {
        val npcLevel = "Level " + removeHtmlTags(
            rawHtml = getStringInBetween(
                string = toParse,
                delimiter1 = "Level ",
                delimiter2 = "<"
            )
        )

        val actionLink = BASE_URL + getStringInBetween(
            string = toParse,
            delimiter1 = "href='",
            delimiter2 = "'"
        )

        return Pair(npcLevel, actionLink)
    }

    fun parseNpcRewards(toParse: String): String {
        return removeHtmlTags(toParse).replace("You have won: ", "")
    }

    fun shouldWaitMore(travelText: String): Boolean {
        return travelText.contains("You have reached")
                || travelText.contains("Hold your horses!")
                || travelText.contains("gPlayReview();")
    }

    fun isUserNotVerified(travelText: String): Boolean {
        return travelText.contains(BOT_RESPONSE)
    }

    fun isUserOnAJob(travelText: String): Boolean {
        return travelText.contains("while you are working")
    }

    fun isUserDead(travelText: String): Boolean {
        return travelText.contains("You're dead")
    }

    fun isUserOnANewLevel(oldLevel: Int, newLevel: Int): Boolean {
        return oldLevel < newLevel
    }
}