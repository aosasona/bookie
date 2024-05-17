package com.trulyao.bookie.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.entities.PostWithRelations
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feed(
    user: User?,
    posts: List<PostWithRelations>,
    isLoadingPosts: Boolean,
    showFab: Boolean = true,
    load: suspend (() -> Unit) -> Unit,
    navigateToPostDetails: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    var editorMode by remember { mutableStateOf<PostMode>(PostMode.Create) }
    var showNewPostModal by remember { mutableStateOf(false) }

    fun stopRefresh() = if (pullToRefreshState.isRefreshing) pullToRefreshState.endRefresh() else Unit

    LaunchedEffect(true) {
        load { stopRefresh() }
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            load { stopRefresh() }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text("Posts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(DEFAULT_VIEW_PADDING))
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { showNewPostModal = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(72.dp)
                ) {
                    Icon(Icons.Default.Add, "Create a post", modifier = Modifier.size(32.dp))
                }
            } else Unit
        },
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
            item {
                if (isLoadingPosts) FeedLoadingIndicator()
            }

            items(posts) { item ->
                PostListItem(
                    item = item,
                    user = user!!,
                    navigateToPostDetails = navigateToPostDetails,
                    onDelete = { scope.launch { load { stopRefresh() } } },
                    enterEditMode = { editorMode = PostMode.Edit(it); showNewPostModal = true }
                )

                Spacer(modifier = Modifier.size(8.dp))
            }
        }

        NewPostModal(
            user = user,
            isVisible = showNewPostModal,
            mode = editorMode,
            reload = { scope.launch { load { stopRefresh() } } },
            onDismiss = { showNewPostModal = false }
        )
    }

}