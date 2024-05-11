package com.trulyao.bookie.views.models

import androidx.compose.runtime.MutableState
import com.trulyao.bookie.entities.Role

data class CurrentUser(
    var uid: MutableState<Int>,
    var firstName: MutableState<String>,
    var lastName: MutableState<String>,
    var email: MutableState<String>,
    var role: MutableState<Role>,
)