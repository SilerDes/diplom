package com.kazbekov.invent.main.utils

import android.util.Log

object Logger {
    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
}