package com.trulyao.bookie.lib

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

fun Context.getDatabase(): AppDatabase {
    return AppDatabase.getInstance(this)
}

fun String.ucFirst(): String {
    return this.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
    }
}

fun String.toMutableState(): MutableState<String> {
    return mutableStateOf(this)
}

fun Int.toMutableState(): MutableState<Int> {
    return mutableIntStateOf(this)
}
