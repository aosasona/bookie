package com.trulyao.bookie.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

interface BottomNavigationBarItem {
    val route: String;
    val title: String;
    val icon: ImageVector;
}

@Composable
fun GenericBottomNavigationBar(navController: NavController, tabs: List<BottomNavigationBarItem>) {
    var currentTabRoute by remember { mutableStateOf(tabs.get(0).route) }
    val selectedIdx by remember {
        derivedStateOf { tabs.indexOfFirst { it.route == currentTabRoute } }
    }

    NavigationBar {
        tabs.forEachIndexed { idx, tab ->
            NavigationBarItem(
                alwaysShowLabel = true,
                selected = idx == selectedIdx,
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                label = { Text(tab.title) },
                onClick = {
                    currentTabRoute = tab.route;

                    navController.navigate(tab.route) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
                                saveState = true
                            } // this makes sure we don't just end up on the first screen by default always with no state or history present
                        }

                        launchSingleTop = true; // keep it unique
                        restoreState = true; // restore tab state in between switches
                    }
                },
            )
        }
    }
}