package com.xcape.simplemmomod.ui.main

sealed class MainUiEvent {
    data class ChangedWebsite(val newCookie: String = ""): MainUiEvent()
    data class ClickedNavigationDrawer(val isOpen: Boolean = false): MainUiEvent()
    data class ClickedLogoutButton(
        val isLoggedIn: Boolean = false,
        val cookie: String = "",
    ): MainUiEvent()
}