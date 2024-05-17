package com.trulyao.bookie.views.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.LoadingButton
import com.trulyao.bookie.components.NewPostModal
import com.trulyao.bookie.components.PostListItem
import com.trulyao.bookie.components.PostMode
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.controllers.PostController
import com.trulyao.bookie.entities.PostWithRelations
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.lib.ucFirst
import com.trulyao.bookie.views.LoadingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetails(user: User?, postId: Int?, goBack: () -> Unit) {
    if (postId == null) {
        goBack()
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var post by remember { mutableStateOf<PostWithRelations?>(null) }
    var showNewPostModal by remember { mutableStateOf(false) }

    suspend fun loadPost() {
        try {
            isLoading = true
            post = PostController
                .getInstance(context.getDatabase().postDao())
                .getPostWithRelationsById(postId)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(true) {
        loadPost()
    }

    if (isLoading || post == null) {
        return LoadingScreen()
    }

    var isSaving by remember { mutableStateOf(false) }
    var editorMode by remember { mutableStateOf(PostMode.Edit(post!!.post)) }
    var commentContent by remember { mutableStateOf("") }

    suspend fun saveComment() {
        try {
            isSaving = true
            val controller = PostController.getInstance(context.getDatabase().postDao())
            controller.createComment(context, userId = user?.id, parentId = postId, content = commentContent)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isSaving = false
            commentContent = ""
            loadPost()
        }
    }

    ProtectedView(user = user) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("${post?.user?.firstName?.ucFirst() ?: ""}'s post") })
            }
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                item {
                    Column {
                        PostListItem(
                            item = post!!,
                            user = user,
                            showFullDetails = true,
                            onDelete = { goBack() },
                            enterEditMode = {
                                editorMode = PostMode.Edit(post!!.post)
                                showNewPostModal = true
                            }
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DEFAULT_VIEW_PADDING),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = commentContent,
                                onValueChange = {
                                    if (commentContent.length < 600) commentContent = it
                                },
                                label = { Text("What do you think?") },
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                            )

                            LoadingButton(
                                isLoading = isSaving,
                                enabled = commentContent.isNotBlank(),
                                horizontalArrangement = Arrangement.End,
                                onClick = { scope.launch { saveComment() } }) {
                                Text("Comment")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Comments",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = DEFAULT_VIEW_PADDING)
                    )
                }

                if (post?.comments.isNullOrEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Spacer(modifier = Modifier.size(36.dp))

                            Text(
                                text = "No comments yet",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = DEFAULT_VIEW_PADDING),
                            )
                        }
                    }
                }

                items(post?.comments ?: listOf()) { item ->
                    PostListItem(
                        item = PostWithRelations(
                            post = item.post,
                            likes = item.likes,
                            user = item.user,
                            comments = emptyList()
                        ),
                        user = user,
                        showFullDetails = true,
                        isComment = true,
                        onDelete = { scope.launch { loadPost() } },
                        enterEditMode = {
                            editorMode = PostMode.Edit(item.post)
                            showNewPostModal = true
                        }
                    )
                }
            }

            NewPostModal(
                user = user,
                isVisible = showNewPostModal,
                mode = editorMode,
                reload = { scope.launch { loadPost() } },
                onDismiss = { showNewPostModal = false }
            )
        }
    }
}