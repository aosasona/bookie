package com.trulyao.bookie.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.ui.theme.BookieTheme

@Composable
fun SignUp(navigateToSignIn: () -> Unit) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    fun handleSignUp() {
        try {
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
        }
    }


    Column(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                "Fill in the form below to create an account",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
