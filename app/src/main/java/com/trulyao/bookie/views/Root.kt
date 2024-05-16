package com.trulyao.bookie.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trulyao.bookie.components.AdminBottomNavigationBar
import com.trulyao.bookie.components.UserBottomNavigationBar
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.Store
import com.trulyao.bookie.lib.StoreKey
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.ui.theme.BookieTheme
import com.trulyao.bookie.views.admin.CreateUser
import com.trulyao.bookie.views.admin.Moderation
import com.trulyao.bookie.views.admin.Users
import com.trulyao.bookie.views.students.ChangePassword
import com.trulyao.bookie.views.students.Home
import com.trulyao.bookie.views.students.PostDetails
import com.trulyao.bookie.views.students.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Root(
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current

    var user: User? by remember { mutableStateOf(null) }

    val currentUserID: Int? by Store
        .getOrDefault(context, StoreKey.CurrentUserID, null)
        .collectAsState(initial = 0)

    val isSignedIn by remember {
        derivedStateOf {
            currentUserID != null && currentUserID != 0 && user != null
        }
    }

    LaunchedEffect(currentUserID) {
        withContext(Dispatchers.IO) {
            user = context.getDatabase().userDao().findByID(currentUserID ?: 0)
        }
    }

    BookieTheme {
        Scaffold(
            bottomBar = {
                if (currentUserID != 0) BottomNavBar(
                    navController,
                    user = user,
                    isSignedIn = isSignedIn
                )
                else Unit
            }
        ) { paddingValues ->
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = getStartDestination(user = user, currentUserID = currentUserID),
                ) {

                    composable(SharedRoutes.LoadingScreen.name) {
                        LoadingScreen()
                    }

                    composable(SharedRoutes.SignIn.name) {
                        SignIn(
                            navigateToSignUp = {
                                navController.navigate(SharedRoutes.SignUp.name)
                            },
                        )
                    }

                    composable(SharedRoutes.SignUp.name) {
                        SignUp(navigateToSignIn = {
                            navController.navigate(SharedRoutes.SignIn.name)
                        })
                    }

                    if (user == null) return@NavHost

                    // Student routes
                    if (user?.role == Role.Student) {
                        composable(userRoute(UserRoutes.Home)) {
                            Home(
                                user = user,
                                navigateToPostDetails = { postId ->
                                    navController.navigate(userRoute(UserRoutes.PostDetails) + "/$postId")
                                }
                            )
                        }

                        composable(userRoute(UserRoutes.Profile)) {
                            Profile(
                                user = user,
                                navigateToSignIn = {
                                    navController.navigate(SharedRoutes.SignIn.name)
                                },
                                navigateToPasswordChange = {
                                    navController.toUserView(UserRoutes.ChangePassword)
                                }
                            )
                        }

                        composable(userRoute(UserRoutes.ChangePassword)) {
                            ChangePassword(user = user)
                        }

                        composable(userRoute(UserRoutes.PostDetails) + "/{postId}", arguments = listOf(navArgument("postId") { type = NavType.StringType })) {
                            val postId = it.arguments?.getString("postId")?.toIntOrNull()
                            PostDetails(postId = postId)
                        }
                    }

                    // Admin routes
                    if (user?.role != Role.Student) {
                        composable(adminRoute(AdminRoutes.Users)) {
                            Users(user = user, navigateToCreateUser = { navController.toAdminView(AdminRoutes.CreateUser) })
                        }

                        composable(adminRoute(AdminRoutes.Moderation)) {
                            Moderation(user = user)
                        }

                        composable(adminRoute(AdminRoutes.Profile)) {
                            Profile(
                                user = user,
                                navigateToSignIn = { navController.navigate(SharedRoutes.SignIn.name) },
                                navigateToPasswordChange = { navController.toAdminView(AdminRoutes.ChangePassword) }
                            )
                        }

                        composable(adminRoute(AdminRoutes.ChangePassword)) {
                            ChangePassword(user = user)
                        }

                        composable(adminRoute(AdminRoutes.CreateUser)) {
                            CreateUser(user = user, navigateToUsersScreen = { navController.toAdminView(AdminRoutes.Users) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, user: User?, isSignedIn: Boolean) {
    if (isSignedIn) {
        BottomAppBar {
            when (user?.role) {
                Role.Student -> UserBottomNavigationBar(navController = navController)
                Role.Admin, Role.SuperAdmin -> AdminBottomNavigationBar(navController = navController)
                null -> Unit
            }
        }
    } else Unit
}

fun getStartDestination(user: User?, currentUserID: Int?): String {
    return if (currentUserID == 0) {
        SharedRoutes.LoadingScreen.name
    } else if (currentUserID == null && user == null) {
        SharedRoutes.SignIn.name
    } else {
        when (user?.role) {
            Role.Student -> userRoute(UserRoutes.Home)
            Role.Admin -> adminRoute(AdminRoutes.Moderation)
            Role.SuperAdmin -> adminRoute(AdminRoutes.Moderation)
            null -> SharedRoutes.LoadingScreen.name
        }
    }
}