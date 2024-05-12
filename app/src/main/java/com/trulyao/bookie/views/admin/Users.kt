package com.trulyao.bookie.views.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.DeleteUserDialog
import com.trulyao.bookie.components.EditUserModal
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.controllers.AdminController
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.lib.toCurrentUser
import com.trulyao.bookie.views.models.CurrentUser
import kotlinx.coroutines.launch

enum class ViewMode {
    Admins,
    Students,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Users(user: User?, navigateToCreateUser: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()
    val sheetState = rememberModalBottomSheetState()

    var isLoading by remember { mutableStateOf(false) }
    val users = remember { mutableStateListOf<User>() }

    var viewMode: ViewMode by remember { mutableStateOf(ViewMode.Admins) }

    var currentUser: CurrentUser? by remember { mutableStateOf(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    suspend fun loadUsers() {
        try {
            isLoading = true
            users.clear()

            val controller = AdminController.getInstance(context.getDatabase().userDao())
            val result = if (viewMode == ViewMode.Admins) controller.getAdmins() else controller.getStudents()

            users.addAll(result)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            if (pullToRefreshState.isRefreshing) {
                pullToRefreshState.endRefresh()
            }

            isLoading = false
        }
    }

    fun enterEditState(target: User?) {
        if (target?.id == null) return
        currentUser = target.toCurrentUser()
        showBottomSheet = true
    }

    suspend fun exitEditState() {
        currentUser = null
        showBottomSheet = false
        loadUsers()
    }

    fun showDeleteDialog(target: User?) {
        if (target?.id == null) return
        currentUser = target.toCurrentUser()
        showDeleteDialog = true
    }


    LaunchedEffect(viewMode) {
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
                        IconButton(onClick = { navigateToCreateUser() }) {
                            Icon(Icons.Filled.Add, "Add a new user")
                        }

                        IconButton(onClick = { pullToRefreshState.startRefresh() }) {
                            Icon(Icons.Filled.Refresh, "Trigger Refresh")
                        }
                    }
                )
            }
        ) { paddingValues ->

            if (isLoading) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.size(16.dp))
                    Text("Please wait...", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                return@Scaffold
            }

            @Composable
            fun Headline(u: User) = if (viewMode == ViewMode.Admins) {
                AdminHeadLine(user = user, admin = u)
            } else {
                StudentHeadline(user = user, student = u)
            }

            @Composable
            fun TrailingContent(u: User) = if (viewMode == ViewMode.Admins) {
                AdminTrailingContent(
                    user = user,
                    admin = u,
                    showEditModal = { enterEditState(it) },
                    showDeleteDialog = { showDeleteDialog(it) }
                )
            } else {
                StudentTrailingContent(
                    user = user,
                    student = u,
                    showEditModal = { enterEditState(it) },
                    showDeleteDialog = { showDeleteDialog(it) }
                )
            }

            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    TabRow(selectedTabIndex = viewMode.ordinal) {
                        ViewMode.entries.forEachIndexed { _, mode ->
                            Tab(text = { Text(mode.name) }, selected = viewMode == mode, onClick = { viewMode = mode })
                        }
                    }
                }

                items(users) { user ->
                    ListItem(headlineContent = { Headline(user) }, trailingContent = { TrailingContent(user) })
                }
            }

            if (showDeleteDialog) {
                DeleteUserDialog(
                    context = context,
                    scope = scope,
                    target = currentUser,
                    reload = { scope.launch { loadUsers() } },
                    onDismiss = {
                        currentUser = null
                        showDeleteDialog = false
                    }
                )
            }

            EditUserModal(
                user = user,
                scope = scope,
                isOpen = showBottomSheet,
                sheetState = sheetState,
                currentUser = currentUser,
                exitEditState = { scope.launch { exitEditState() } },
            )
        }
    }
}