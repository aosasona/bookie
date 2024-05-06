package com.trulyao.bookie.views.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.controllers.AdminController
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.signOut
import com.trulyao.bookie.lib.AppDatabase
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.handleException
import kotlinx.coroutines.launch
import java.util.Locale

data class CurrentUser(
    var firstName: MutableState<String>,
    var lastName: MutableState<String>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Users(
    user: User,
    navigateToSignIn: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()
    val sheetState = rememberModalBottomSheetState()

    val admins = remember { mutableStateListOf<User>() }
    var currentUser: CurrentUser? by remember { mutableStateOf(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    suspend fun handleSignOut() {
        signOut(context)
        navigateToSignIn()
    }

    suspend fun loadAdmins() {
        try {
            admins.clear()

            admins.addAll(
                AdminController
                    .getInstance(AppDatabase.getInstance(context).userDao())
                    .getAdminUsers()
            )
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            pullToRefreshState.endRefresh()
        }
    }


    LaunchedEffect(true) {
        loadAdmins()
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            loadAdmins()
        }
    }

    ProtectedView(user = user, allow = listOf(Role.Admin)) {
        Scaffold(
            modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Users",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    // Provide an accessible alternative to trigger refresh.
                    actions = {
                        IconButton(onClick = { pullToRefreshState.startRefresh() }) {
                            Icon(Icons.Filled.Refresh, "Trigger Refresh")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn {
                    items(admins) { user ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = "${
                                        user.firstName.replaceFirstChar { fname ->
                                            if (fname.isLowerCase()) fname.titlecase(
                                                Locale.ROOT
                                            ) else fname.toString()
                                        }
                                    } ${
                                        user.lastName.replaceFirstChar { lname ->
                                            if (lname.isLowerCase()) lname.titlecase(
                                                Locale.US
                                            ) else lname.toString()
                                        }
                                    }",
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            },
                            trailingContent = {
                                TextButton(onClick = {
                                    currentUser = CurrentUser(
                                        firstName = mutableStateOf(user.firstName),
                                        lastName = mutableStateOf(user.lastName)
                                    )
                                    showBottomSheet = true
                                }) {
                                    Text(text = "edit")
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.size(32.dp))

                        Button(
                            onClick = { scope.launch { handleSignOut() } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DEFAULT_VIEW_PADDING)
                        ) {
                            Text("Sign out", color = Color.White)
                        }

                        Spacer(modifier = Modifier.size(32.dp))
                    }
                }

                if (showBottomSheet && currentUser != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                    ) {
                        Box(
                            modifier = Modifier.padding(DEFAULT_VIEW_PADDING)
                        ) {
                            Text(
                                "Edit",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = DEFAULT_VIEW_PADDING)
                            )

                            Spacer(modifier = Modifier.size(32.dp))

                            TextInput(
                                title = "First name",
                                value = currentUser?.firstName?.value ?: "",
                                onChange = { currentUser?.let { u -> u.firstName.value = it } }
                            )
                        }
                    }
                }
            }
        }
    }
}
