package com.trulyao.bookie.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
fun NewPostModal(
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    user: User?,
    isVisible: Boolean,
    mode: PostMode,
    onDismiss: () -> Unit,
    reload: () -> Unit,
) {
    var postContent by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val canSave by remember { derivedStateOf { postContent.isNotBlank() && !isSaving } }

    fun dismissModal() {
        postContent = ""
        onDismiss()
    }

    suspend fun save() {
        try {
            isSaving = true
            val controller = PostController.getInstance(context.getDatabase().postDao())

            if (mode is PostMode.Create) {
                controller.createPost(context, user?.id, postContent)
            } else {
                val m = mode as PostMode.Edit
                controller.updatePostContent(m.post.id, postContent)
            }

            Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isSaving = false
            reload()
            dismissModal()
        }
    }

    LaunchedEffect(mode) {
        if (mode is PostMode.Edit) {
            postContent = mode.post.content
        }
    }


    if (isVisible) {
        ModalBottomSheet(onDismissRequest = { dismissModal() }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DEFAULT_VIEW_PADDING)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { postContent = ""; onDismiss(); }) { Text(text = "Cancel") }
                    LoadingButton(
                        isLoading = isSaving,
                        enabled = canSave,
                        onClick = { scope.launch { save() } }
                    ) {
                        Text(text = if (mode == PostMode.Create) "Post" else "Save")
                    }
                }

                TextField(
                    value = postContent,
                    onValueChange = { postContent = it },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxSize(),
                    maxLines = 24,
                )
            }
        }
    }
}
