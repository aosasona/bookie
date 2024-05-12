package com.trulyao.bookie.lib

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.views.models.CurrentUser
import java.util.Locale

fun Context.getDatabase(): AppDatabase {
    return AppDatabase.getInstance(this)
}

fun User.toCurrentUser(): CurrentUser {
    return CurrentUser(
        uid = this.id!!.toMutableState(),
        firstName = this.firstName.toMutableState(),
        lastName = this.lastName.toMutableState(),
        email = this.email.toMutableState(),
        role = mutableStateOf(this.role),
        dateOfBirth = this.dateOfBirth,
    )
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
