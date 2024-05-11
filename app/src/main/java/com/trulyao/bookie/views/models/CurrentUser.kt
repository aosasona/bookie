package com.trulyao.bookie.views.models

import androidx.compose.runtime.MutableState

data class CurrentUser(
    var uid: MutableState<Int>,
    var firstName: MutableState<String>,
    var lastName: MutableState<String>,
)