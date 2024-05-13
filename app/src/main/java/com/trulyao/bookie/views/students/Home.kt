package com.trulyao.bookie.views.students

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.LoadingButton
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.controllers.PostController
import com.trulyao.bookie.entities.Post
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed interface PostMode {
    class Edit(val post: Post) : PostMode
    data object Create : PostMode
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(user: User?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val pullToRefreshState = rememberPullToRefreshState()

    var editorMode by remember { mutableStateOf<PostMode>(PostMode.Create) }

    var showNewPostModal by remember { mutableStateOf(false) }
    var isLoadingPosts by remember { mutableStateOf(true) }

    suspend fun loadPosts() {
        try {
            isLoadingPosts = true
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoadingPosts = false
            if (pullToRefreshState.isRefreshing) pullToRefreshState.endRefresh()
        }
    }

    LaunchedEffect(true) {
        loadPosts()
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            loadPosts()
        }
    }

    ProtectedView(user = user) {
        Scaffold(
            topBar = {
                Text("Posts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(DEFAULT_VIEW_PADDING))
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNewPostModal = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(72.dp)
                ) {
                    Icon(Icons.Default.Add, "Create a post", modifier = Modifier.size(32.dp))
                }
            },
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
            ) {
                item {
                    if (isLoadingPosts) FeedLoadingIndicator()
                }

                // Posts
            }

            NewPostModal(
                user = user,
                isVisible = showNewPostModal,
                mode = editorMode,
                reload = { scope.launch { loadPosts() } },
                onDismiss = { showNewPostModal = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostModal(
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    user: User?,
    isVisible: Boolean,
    mode: PostMode,
    onDismiss: () -> Unit,
    reload: () -> Unit,
) {
    var content by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    suspend fun save() {
        try {
            isSaving = true
            if (mode is PostMode.Create) {
                PostController
                    .getInstance(context.getDatabase().postDao())
                    .createPost(context, user?.id, content)
            } else {
                // Update post
            }

            Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isSaving = false
            reload()
            onDismiss()
        }
    }

    LaunchedEffect(mode) {
        if (mode is PostMode.Edit) {
            content = mode.post.content
        }
    }

    if (isVisible) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(DEFAULT_VIEW_PADDING)
                    .fillMaxSize()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        content = "";
                        onDismiss()
                    }) {
                        Text(text = "Cancel")
                    }

                    LoadingButton(
                        isLoading = isSaving,
                        enabled = content.isNotEmpty(),
                        onClick = { scope.launch { save() } }
                    ) {
                        Text(text = if (mode == PostMode.Create) "Post" else "Save")
                    }
                }

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxSize(),
                    maxLines = 24,
                )
            }
        }
    }
}

@Composable
fun FeedLoadingIndicator() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 12.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.size(10.dp))
        Text("Fetching new posts...", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
    }
}

