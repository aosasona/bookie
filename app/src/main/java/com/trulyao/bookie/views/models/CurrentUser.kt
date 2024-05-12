package com.trulyao.bookie.views.models

import androidx.compose.runtime.MutableState
import com.trulyao.bookie.entities.Role
import java.util.Date

data class CurrentUser(
    var uid: MutableState<Int>,
    var firstName: MutableState<String>,
    var lastName: MutableState<String>,
    var email: MutableState<String>,
    var role: MutableState<Role>,
    var dateOfBirth: Date,
)