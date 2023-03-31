package com.xcape.simplemmomod.ui.common

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

fun Context.showToast(message: String, length: Int = LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}