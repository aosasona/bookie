package com.trulyao.bookie.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.ui.theme.BookieTheme

@Composable
fun SignIn(navigateToSignUp: () -> Unit) {
    val context = LocalContext.current

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    fun handleSignIn() {
        try {
            isLoading = true
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text(
                "Enter your credentials to continue",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TextInput(
                title = "Email-address",
                value = email,
                onChange = { email = it },
                placeholderText = "john.doe@bookie.ac.uk",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Next
                )
            )

            TextInput(
                title = "Password",
                value = password,
                onChange = { password = it },
                placeholderText = "******",
                visualTransformation = PasswordVisualTransformation(),
            )
        }



        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { navigateToSignUp() }) {
                Text("Create an account")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { handleSignIn() }, enabled = isLoading.not()) {
                    Text("Sign In")
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.size(12.dp))

                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(28.dp)
                            .height(28.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInPreview() {
    BookieTheme {
        SignIn(navigateToSignUp = {})
    }
}