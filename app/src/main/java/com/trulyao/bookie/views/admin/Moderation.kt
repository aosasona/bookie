package com.trulyao.bookie.views.admin

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User

@Composable
fun Moderation(user: User?) {
    ProtectedView(user = user, allow = listOf(Role.Admin, Role.SuperAdmin)) {
        Text("Admin Moderation")
    }
}