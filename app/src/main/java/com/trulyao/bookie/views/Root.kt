package com.trulyao.bookie.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trulyao.bookie.ui.theme.BookieTheme

@Composable
fun Root() {
    val navController = rememberNavController()

    BookieTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = Views.SignIn.name) {
                composable(Views.SignIn.name) {
                    SignIn(navigateToSignUp = {
                        navController.navigate(
                            Views.SignUp.name
                        )
                    })
                }

                composable(Views.SignUp.name) {
                    SignUp(navigateToSignIn = {
                        navController.navigate(
                            Views.SignIn.name
                        )
                    })
                }
            }
        }
    }
}