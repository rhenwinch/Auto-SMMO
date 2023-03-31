package com.xcape.simplemmomod.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.input.key.Key.Companion.I
import com.xcape.simplemmomod.R

sealed class MenuItem(
    val icon: IconResource,
    val route: String,
    val action: () -> Unit
) {
    data class Home(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromImageVector(Icons.Filled.Home),
        route = "Home",
        action = _action
    )

    data class Quest(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_quest),
        route = "Quest",
        action = _action
    )

    data class Travel(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_travel),
        route = "Travel",
        action = _action
    )

    data class Battle(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_battle),
        route = "Battle",
        action = _action
    )

    data class Profile(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromImageVector(Icons.Filled.Person),
        route = "You",
        action = _action
    )

    data class Messages(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_message),
        route = "Messages",
        action = _action
    )

    data class Notifications(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_notifications),
        route = "Notifications",
        action = _action
    )

    data class Character(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromImageVector(Icons.Filled.Person),
        route = "Character",
        action = _action
    )

    data class Town(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.town),
        route = "Town",
        action = _action
    )

    data class Inventory(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.inventory),
        route = "Inventory",
        action = _action
    )

    data class Jobs(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.jobs),
        route = "Jobs",
        action = _action
    )

    data class Tasks(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.tasks),
        route = "Tasks",
        action = _action
    )

    data class Collections(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.collection),
        route = "Collections",
        action = _action
    )

    data class Crafting(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.crafting),
        route = "Crafting",
        action = _action
    )

    data class Guilds(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.guilds),
        route = "Guilds",
        action = _action
    )

    data class Leaderboards(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.leaderboards),
        route = "Leaderboards",
        action = _action
    )

    data class DiscussionBoards(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.discussionboards),
        route = "Discussion Boards",
        action = _action
    )

    data class SocialMedia(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.socialmedia),
        route = "Social Media",
        action = _action
    )

    data class Events(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.events),
        route = "Events",
        action = _action
    )

    data class DiamondStore(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.diamondstore),
        route = "Diamond Store",
        action = _action
    )

    data class Earn(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.earn),
        route = "Earn",
        action = _action
    )

    data class Settings(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.settings),
        route = "Settings",
        action = _action
    )

    data class About(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.about),
        route = "About",
        action = _action
    )

    data class Support(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.support),
        route = "Support",
        action = _action
    )

    data class Logout(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.logout),
        route = "Logout",
        action = _action
    )

    data class Login(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_login),
        route = "Login",
        action = _action
    )

    data class Register(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_lock),
        route = "Register",
        action = _action
    )

    data class AccountPicker(val _action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_people),
        route = "Account Picker",
        action = _action
    )
}