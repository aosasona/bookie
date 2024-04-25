package com.trulyao.bookie.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.trulyao.bookie.views.AdminRoutes
import com.trulyao.bookie.views.adminRoute

sealed class AdminBottomBarItem(
    override val route: String,
    override val title: String,
    override val icon: ImageVector,
) : BottomNavigationBarItem {
    data object Students :
        UserBottomBarItem(adminRoute(AdminRoutes.Students), "Manage Students", Icons.Default.School)

    data object Moderation :
        UserBottomBarItem(adminRoute(AdminRoutes.Moderation), "Moderation", Icons.Default.Shield)

    data object Users : UserBottomBarItem(
        adminRoute(AdminRoutes.Users),
        "Users",
        Icons.Default.SupervisedUserCircle
    )
}

private val tabs = listOf(
    AdminBottomBarItem.Students,
    AdminBottomBarItem.Moderation,
    AdminBottomBarItem.Users,
)

@Composable
fun AdminBottomNavigationBar(navController: NavController) {
    GenericBottomNavigationBar(navController = navController, tabs = tabs)
}

@Preview(showBackground = true)
@Composable
private fun NavBarPreview() {
    val navController = rememberNavController()
    AdminBottomNavigationBar(navController = navController)
}
