package com.xcape.simplemmomod.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferences(
    val userIdToUse: Int = 0,
    val userAgent: String = ""
)