package com.trulyao.bookie.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trulyao.bookie.lib.AppDatabase
import com.trulyao.bookie.lib.Store
import com.trulyao.bookie.lib.StoreKey
import com.trulyao.bookie.ui.theme.BookieTheme

@Composable
fun Root() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val currentUserID: Int? by Store.getOrDefault(context, StoreKey.CurrentUserID, null)
        .collectAsState(initial = 0)

    val user by remember {
        derivedStateOf { AppDatabase.getInstance(context).userDao().findByID(currentUserID ?: 0) }
    }

    BookieTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = SharedRoutes.SignIn.name) {
                composable(SharedRoutes.SignIn.name) {
                    SignIn(navigateToSignUp = {
                        navController.navigate(SharedRoutes.SignUp.name)
                    })
                }

                composable(SharedRoutes.SignUp.name) {
                    SignUp(navigateToSignIn = {
                        navController.navigate(SharedRoutes.SignIn.name)
                    })
                }
            }
        }
    }
}

fun userRoute(route: UserRoutes): String {
    return "user_${route.name}"
}


fun adminRoute(route: AdminRoutes): String {
    return "admin_${route.name}"
}

fun NavController.toUserView(route: UserRoutes) {
    navigate("user_${route.name}")
}

fun NavController.toAdminView(route: UserRoutes) {
    navigate("admin_${route.name}")
}
