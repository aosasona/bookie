package com.trulyao.bookie.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.trulyao.bookie.ui.theme.BookieTheme

@Composable
fun SignIn() {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text("Enter your credentials to continue", style = MaterialTheme.typography.labelLarge)

        Spacer(modifier = Modifier.size(12.dp))

        TextField(value = username, onValueChange = { username = it }, placeholder = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        TextField(value = password, onValueChange = { password = it }, placeholder = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.size(12.dp))

        Button(onClick = { /*TODO*/ }) {
            Text("Sign In")
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