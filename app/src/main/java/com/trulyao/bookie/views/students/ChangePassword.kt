package com.trulyao.bookie.views.students

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.LoadingButton
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.controllers.mockUser
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import kotlinx.coroutines.launch

@Composable
fun ChangePassword(user: User, scrollState: ScrollState = rememberScrollState()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    suspend fun handleChangePassword() {
        try {
            isLoading = true
            UserController
                .getInstance(context.getDatabase().userDao())
                .changePassword(
                    userId = user.id ?: 0,
                    oldPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmNewPassword
                )
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoading = false
        }
    }

    ProtectedView(user = user) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DEFAULT_VIEW_PADDING)
                .verticalScroll(scrollState)
        ) {

            Text(
                "Change password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(30.dp))

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TextInput(
                    title = "Current Password",
                    value = currentPassword,
                    onChange = { currentPassword = it },
                    visualTransformation = PasswordVisualTransformation()
                )

                TextInput(
                    title = "New Password",
                    value = newPassword,
                    onChange = { newPassword = it },
                    visualTransformation = PasswordVisualTransformation()
                )

                TextInput(
                    title = "Confirm New Password",
                    value = confirmNewPassword,
                    onChange = { confirmNewPassword = it },
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            Spacer(modifier = Modifier.size(30.dp))

            LoadingButton(
                isLoading = isLoading,
                onClick = { scope.launch { handleChangePassword() } },
                horizontalArrangement = Arrangement.End
            ) {
                Text("Continue")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChangePasswordPreview() {
    ChangePassword(user = mockUser(Role.Student))
}