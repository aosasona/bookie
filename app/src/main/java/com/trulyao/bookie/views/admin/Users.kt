package com.trulyao.bookie.views.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.trulyao.bookie.components.EditUserModal
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.controllers.AdminController
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.lib.toMutableState
import com.trulyao.bookie.views.models.CurrentUser

enum class ViewMode {
    Admins,
    Students,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Users(user: User?) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()
    val sheetState = rememberModalBottomSheetState()

    val viewMode: ViewMode by remember { mutableStateOf(ViewMode.Admins) }
    val users = remember { mutableStateListOf<User>() }
    var currentUser: CurrentUser? by remember { mutableStateOf(null) }
    var showBottomSheet by remember { mutableStateOf(false) }


    suspend fun loadUsers() {
        try {
            users.clear()

            val controller = AdminController.getInstance(context.getDatabase().userDao())
            val result = if (viewMode == ViewMode.Admins) controller.getAdmins() else controller.getStudents()

            users.addAll(result)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            pullToRefreshState.endRefresh()
        }
    }

    fun enterEditState(user: User?) {
        if (user?.id == null) return

        currentUser = CurrentUser(
            uid = user.id.toMutableState(),
            firstName = user.firstName.toMutableState(),
            lastName = user.lastName.toMutableState(),
        )

        showBottomSheet = true
    }

    fun exitEditState() {
        currentUser = null
        showBottomSheet = false
    }


    LaunchedEffect(true) {
        loadUsers()
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            loadUsers()
        }
    }

    ProtectedView(user = user, allow = listOf(Role.Admin, Role.SuperAdmin)) {
        Scaffold(
            modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text("Users", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    },
                    // Provide an accessible alternative to trigger refresh.
                    actions = {
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(Icons.Filled.Add, "Add a new admin user")
                        }

                        IconButton(onClick = { pullToRefreshState.startRefresh() }) {
                            Icon(Icons.Filled.Refresh, "Trigger Refresh")
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (viewMode == ViewMode.Admins) {
                AdminsList(user = user, users = users, paddingValues = paddingValues, showEditModal = { enterEditState(it) })
            }

            EditUserModal(
                scope = scope,
                isOpen = showBottomSheet,
                sheetState = sheetState,
                currentUser = currentUser,
                exitEditState = { exitEditState() }
            )
        }
    }
}