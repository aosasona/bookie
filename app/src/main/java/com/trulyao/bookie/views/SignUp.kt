package com.trulyao.bookie.views

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trulyao.bookie.components.LoadingButton
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.controllers.CreateUserData
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.lib.Alert
import com.trulyao.bookie.lib.AppDatabase
import com.trulyao.bookie.lib.AppException
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.ui.theme.BookieTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(navigateToSignIn: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isLoading by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val dob = rememberDatePickerState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    suspend fun handleSignUp() {
        try {
            isLoading = true

            if (dob.selectedDateMillis == null) throw AppException("Date of birth is invalid or empty")

            val userRepo = UserController.getInstance(
                AppDatabase.getInstance(context).userDao(),
                Dispatchers.IO
            )
            val data = CreateUserData(
                firstName = firstName,
                lastName = lastName,
                email = email,
                dateOfBirth = Date(dob.selectedDateMillis!!),
                password = password,
                confirmPassword = confirmPassword
            )

            val createdUserId = userRepo.signUp(data)
            if (createdUserId <= 0) throw AppException("Failed to create account, please try again later")

            Alert.show(
                context,
                alertType = Alert.AlertType.Success,
                message = "Your account has been successfully created, you can now proceed to sign in :)",
            )
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoading = false
        }
    }


    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .verticalScroll(enabled = true, state = scrollState),
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "Sign Up",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                "Fill in the form below to create an account",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.size(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            TextInput(
                title = "First name",
                value = firstName,
                onChange = { firstName = it },
                placeholderText = "John",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            TextInput(
                title = "Last name",
                value = lastName,
                onChange = { lastName = it },
                placeholderText = "Doe",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            TextInput(
                title = "Student e-mail address",
                value = email,
                onChange = { email = it },
                placeholderText = "john@bookie.ac.uk",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            TextInput(
                title = "Password",
                value = password,
                onChange = { password = it },
                placeholderText = "******",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            TextInput(
                title = "Confirm Password",
                value = confirmPassword,
                onChange = { confirmPassword = it },
                placeholderText = "******",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            DatePicker(
                state = dob,
                title = { Text("Date of birth") }
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { navigateToSignIn() }, modifier = Modifier.padding(0.dp)) {
                Text("Already have an account?", modifier = Modifier.padding(0.dp))
            }

            LoadingButton(
                isLoading = isLoading,
                onClick = { scope.launch { handleSignUp() } },
                horizontalArrangement = Arrangement.End
            ) {
                Text("Sign up")
            }
        }

        Spacer(modifier = Modifier.size(50.dp))
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SignUpPreview() {
    BookieTheme {
        SignUp(navigateToSignIn = {})
    }
}
