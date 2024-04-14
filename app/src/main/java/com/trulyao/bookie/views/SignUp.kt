package com.trulyao.bookie.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.ui.theme.BookieTheme

@Composable
fun SignUp(navigateToSignIn: () -> Unit) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    fun handleSignUp() {
        try {
            isLoading = true
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoading = false
        }
    }


    Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            TextInput(
                title = "Password",
                value = password,
                onChange = { password = it },
                placeholderText = "******",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            TextInput(
                title = "Confirm Password",
                value = confirmPassword,
                onChange = { confirmPassword = it },
                placeholderText = "******",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
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
                onClick = { handleSignUp() },
                horizontalArrangement = Arrangement.End
            ) {
                Text("Sign up")
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SignUpPreview() {
    BookieTheme {
        SignUp(navigateToSignIn = {})
    }
}
