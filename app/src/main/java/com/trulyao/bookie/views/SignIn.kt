package com.trulyao.bookie.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.ui.theme.BookieTheme

@Composable
fun SignIn() {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    fun handleSignIn() {
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(text = "Sign In", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.size(6.dp))
            Text("Enter your credentials to continue", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
        }

        Column {
            TextInput(
                title = "Email-address",
                value = email,
                onChange = { email = it },
                placeholderText = "john.doe@bookie.ac.uk"
            )

            Spacer(modifier = Modifier.size(8.dp))

            TextInput(
                title = "Password",
                value = password,
                onChange = { password = it },
                placeholderText = "******",
                visualTransformation = PasswordVisualTransformation(),
            )
        }

        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { handleSignIn() }) {
                Text("Sign In")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInPreview() {
    BookieTheme {
        SignIn()
    }
}