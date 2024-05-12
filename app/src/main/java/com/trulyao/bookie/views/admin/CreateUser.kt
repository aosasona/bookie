package com.trulyao.bookie.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trulyao.bookie.components.LoadingButton
import com.trulyao.bookie.components.PasswordInput
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.controllers.CreateUserData
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.controllers.mockUser
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.Alert
import com.trulyao.bookie.lib.AppException
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUser(user: User?, navigateToUsersScreen: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val generatedEmail by remember {
        derivedStateOf {
            if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                "${firstName.lowercase()}.${lastName.lowercase()}@bookie.ac.uk"
            } else {
                ""
            }
        }
    }
    var password by remember { mutableStateOf("") }
    val dob = rememberDatePickerState()
    var role by remember { mutableStateOf(Role.Student) }

    var isSaving by remember { mutableStateOf(false) }

    suspend fun createUser() {
        try {
            isSaving = true

            if (dob.selectedDateMillis == null) throw AppException("Date of birth is invalid or empty")

            val data = CreateUserData(
                firstName = firstName,
                lastName = lastName,
                email = email.ifEmpty { generatedEmail },
                dateOfBirth = Date(dob.selectedDateMillis!!),
                password = password,
            )

            val createdUserId = UserController
                .getInstance(context.getDatabase().userDao())
                .signUp(data, role = role)

            if (createdUserId <= 0) throw AppException("Failed to create account, please try again later")

            Alert.show(
                context,
                alertType = Alert.AlertType.Success,
                message = "User account has been successfully created",
            )

            navigateToUsersScreen()
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isSaving = false
        }
    }

    ProtectedView(user = user, allow = listOf(Role.Admin, Role.SuperAdmin)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(horizontal = DEFAULT_VIEW_PADDING)
                .fillMaxWidth()
                .verticalScroll(enabled = true, state = scrollState),
        ) {
            Text(text = "New user", fontSize = 42.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp))

            Spacer(modifier = Modifier.size(2.dp))

            TextInput(
                title = "First name",
                value = firstName,
                onChange = { firstName = it },
                placeholderText = "John",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )

            TextInput(
                title = "Last name",
                value = lastName,
                onChange = { lastName = it },
                placeholderText = "Doe",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )

            TextInput(
                title = "E-mail address",
                value = email.ifEmpty { generatedEmail },
                onChange = { email = it },
                placeholderText = "john@bookie.ac.uk",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            PasswordInput(title = "Password", value = password, onChange = { password = it })

            if (user?.role == Role.SuperAdmin) {
                Text("Role", style = MaterialTheme.typography.labelLarge)

                Row(modifier = Modifier.fillMaxWidth()) {
                    Role.entries.forEach { item ->
                        Surface(
                            onClick = { role = item },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(if (item == role) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (item == role) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background),
                            ) {
                                Text(
                                    text = item.name,
                                    color = if (item == role) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .background(if (item == role) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(4.dp))

            DatePicker(state = dob, title = { Text("Date of birth") })

            LoadingButton(isLoading = isSaving, horizontalArrangement = Arrangement.End, onClick = { scope.launch { createUser() } }) {
                Text(text = "Save")
            }

            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CreateUserPreview() {
    CreateUser(user = mockUser(Role.SuperAdmin), navigateToUsersScreen = {})
}