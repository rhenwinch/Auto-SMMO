package com.xcape.simplemmomod.common

interface OnTravellerStateChange {
    fun start()
    fun pause()
    fun onError(message: String)
    fun onConsumeError()
}