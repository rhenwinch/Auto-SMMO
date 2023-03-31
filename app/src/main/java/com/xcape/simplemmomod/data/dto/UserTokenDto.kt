package com.xcape.simplemmomod.data.dto

data class UserTokenDto(
    val loggedin: Boolean?,
    val current_time: Long,
    val api_token: String?,
    val warning: String?
)