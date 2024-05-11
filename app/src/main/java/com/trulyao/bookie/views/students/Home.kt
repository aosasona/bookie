package com.trulyao.bookie.views.students

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.entities.User

@Composable
fun Home(user: User?) {
    ProtectedView(user = user) {
        Text("Home")
    }
}