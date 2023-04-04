package com.xcape.simplemmomod.common

@Suppress("SpellCheckingInspection")
object Endpoints {
    const val BASE_URL = "https://simple-mmo.com"
    private const val API_BASE_URL = "https://api.simple-mmo.com"

    const val WEB_HOST = "simple-mmo.com"
    const val API_HOST = "api.simple-mmo.com"

    const val REGISTER_URL = "$BASE_URL/register"
    const val LOGIN_URL = "$BASE_URL/login"
    const val LOGOUT_URL = "$BASE_URL/logout"

    const val USER_EVENTS_URL = "$API_BASE_URL/api/main"
    const val USER_INFO_URL = "$API_BASE_URL/api/popup"
    const val API_TOKEN_URL = "$BASE_URL/api/token"

    const val HOME_URL = "$BASE_URL/home"
    const val BATTLE_URL = "$BASE_URL/battle/menu"
    const val QUEST_URL = "$BASE_URL/quests/viewall"
    const val TRAVEL_URL = "$BASE_URL/travel"
    const val NOTIFICATIONS_URL = "$BASE_URL/events"
    const val MESSAGES_URL = "$BASE_URL/messages/inbox"
    const val TOWN_URL = "$BASE_URL/town"
    const val INVENTORY_URL = "$BASE_URL/inventory"
    const val JOBS_URL = "$BASE_URL/jobs/viewall"
    const val ABOUT_URL = "$BASE_URL/about"
    const val EVENTS_URL = "$BASE_URL/events/viewall"
    const val CHARACTER_URL = "$BASE_URL/character"
    const val COLLECTION_URL = "$BASE_URL/collection/menu"
    const val CRAFTING_URL = "$BASE_URL/crafting/menu"
    const val DISCUSSION_BOARD_URL = "$BASE_URL/discussionboards/menu"
    const val EARN_URL = "$BASE_URL/earn"
    const val GUILDS_URL = "$BASE_URL/guilds/menu"
    const val LEADERBOARDS_URL = "$BASE_URL/leaderboards"
    const val SETTINGS_URL = "$BASE_URL/preferences"
    const val SOCIAL_MEDIA_URL = "$BASE_URL/social-media"
    const val DIAMONDS_STORE_URL = "$BASE_URL/diamondstore"
    const val SUPPORT_URL = "$BASE_URL/support"
    const val TASKS_URL = "$BASE_URL/tasks/daily"

    const val STEP_URL = "$API_BASE_URL/api/travel/perform/f4gl4l3k"
    const val GENERATE_NPC_URL = "$API_BASE_URL/api/battlearena/generate"
    const val ATTACK_NPC_URL = "$API_BASE_URL/api/npcs/attack/%s/434g3s"
    const val GATHER_MATERIAL_URL = "$BASE_URL/api/crafting/material/gather/%s"
    const val ITEM_STAT_URL = "$BASE_URL/api/item/stats/%s"
    const val ITEM_EQUIP_URL = "$BASE_URL/inventory/equip/%s?api=true"
    const val QUEST_ACTION_URL = "$BASE_URL/api/quest/%s/gj83h"
    const val UPGRADE_SKILL_URL = "$BASE_URL/api/user/upgrade/%s"
    const val BOT_VERIFICATION_URL = "$BASE_URL/i-am-not-a-bot"

}