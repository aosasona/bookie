package com.trulyao.bookie.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.signOut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ProtectedView(
    context: Context = LocalContext.current,
    user: User?,
    allow: List<Role> = listOf(Role.Student),
    scope: CoroutineScope = rememberCoroutineScope(),
    child: @Composable () -> Unit,
) {
    LaunchedEffect(true) {
        if (user == null || !allow.contains(user.role)) {
            scope.launch {
                signOut(context)
            }
        }
    }

    return child()
}