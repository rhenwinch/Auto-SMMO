package com.xcape.simplemmomod.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Stable
import androidx.compose.ui.input.key.Key.Companion.I
import com.xcape.simplemmomod.R

@Stable
sealed class MenuItem(
    val icon: IconResource,
    val route: String,
    val action: () -> Unit
) {
    class Home(action: () -> Unit): MenuItem(
        icon = IconResource.fromImageVector(Icons.Filled.Home),
        route = "Home",
        action = action
    )

    class Quest(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_quest),
        route = "Quest",
        action = action
    )

    class Travel(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_travel),
        route = "Travel",
        action = action
    )

    class Battle(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_battle),
        route = "Battle",
        action = action
    )

    class Profile(action: () -> Unit): MenuItem(
        icon = IconResource.fromImageVector(Icons.Filled.Person),
        route = "You",
        action = action
    )

    class Messages(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_message),
        route = "Messages",
        action = action
    )

    class Notifications(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_notifications),
        route = "Notifications",
        action = action
    )

    class Character(action: () -> Unit): MenuItem(
        icon = IconResource.fromImageVector(Icons.Filled.Person),
        route = "Character",
        action = action
    )

    class Town(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.town),
        route = "Town",
        action = action
    )

    class Inventory(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.inventory),
        route = "Inventory",
        action = action
    )

    class Jobs(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.jobs),
        route = "Jobs",
        action = action
    )

    class Tasks(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.tasks),
        route = "Tasks",
        action = action
    )

    class Collections(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.collection),
        route = "Collections",
        action = action
    )

    class Crafting(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.crafting),
        route = "Crafting",
        action = action
    )

    class Guilds(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.guilds),
        route = "Guilds",
        action = action
    )

    class Leaderboards(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.leaderboards),
        route = "Leaderboards",
        action = action
    )

    class DiscussionBoards(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.discussionboards),
        route = "Discussion Boards",
        action = action
    )

    class SocialMedia(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.socialmedia),
        route = "Social Media",
        action = action
    )

    class Events(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.events),
        route = "Events",
        action = action
    )

    class DiamondStore(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.diamondstore),
        route = "Diamond Store",
        action = action
    )

    class Earn(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.earn),
        route = "Earn",
        action = action
    )

    class Settings(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.settings),
        route = "Settings",
        action = action
    )

    class About(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.about),
        route = "About",
        action = action
    )

    class Support(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.support),
        route = "Support",
        action = action
    )

    class Logout(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.logout),
        route = "Logout",
        action = action
    )

    class Login(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_login),
        route = "Login",
        action = action
    )

    class Register(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_lock),
        route = "Register",
        action = action
    )

    class AccountPicker(action: () -> Unit): MenuItem(
        icon = IconResource.fromDrawableResource(R.drawable.ic_people),
        route = "Account Picker",
        action = action
    )
}