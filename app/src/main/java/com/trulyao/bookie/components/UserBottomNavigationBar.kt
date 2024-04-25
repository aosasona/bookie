package com.trulyao.bookie.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.trulyao.bookie.views.UserRoutes
import com.trulyao.bookie.views.userRoute

sealed class UserBottomBarItem(
    override val route: String,
    override val title: String,
    override val icon: ImageVector,
) : BottomNavigationBarItem {
    data object Home : UserBottomBarItem(userRoute(UserRoutes.Home), "Home", Icons.Default.Home)
    data object Activities :
        UserBottomBarItem(
            userRoute(UserRoutes.Activities),
            "Activities",
            Icons.Default.LocalActivity
        )

    data object Profile :
        UserBottomBarItem(userRoute(UserRoutes.Profile), "Profile", Icons.Default.Person)
}

private val tabs = listOf(
    UserBottomBarItem.Home,
    UserBottomBarItem.Activities,
    UserBottomBarItem.Profile
)

@Composable
fun UserBottomNavigationBar(navController: NavController) {
    GenericBottomNavigationBar(navController = navController, tabs = tabs)
}

@Preview(showBackground = true)
@Composable
private fun NavBarPreview() {
    val navController = rememberNavController()
    UserBottomNavigationBar(navController = navController)
}