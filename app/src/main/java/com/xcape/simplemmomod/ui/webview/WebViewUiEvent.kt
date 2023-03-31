package com.xcape.simplemmomod.ui.webview

sealed class WebViewUiEvent {
    data class ChangedWebsite(val newCookie: String = ""): WebViewUiEvent()
    object UserVerified: WebViewUiEvent()
}