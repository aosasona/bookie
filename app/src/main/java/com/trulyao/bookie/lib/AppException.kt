package com.trulyao.bookie.lib

import android.content.Context

class AppException(message: String) : Exception(message) {
}

public fun handleException(context: Context, e: Exception) {
    val message = when (e) {
        is AppException -> e.message
        else -> {
            System.err.println(e.message)
            System.err.println(e.stackTrace)
            "Something went wrong"
        }
    }

    Alert.show(context, message!!)
}
