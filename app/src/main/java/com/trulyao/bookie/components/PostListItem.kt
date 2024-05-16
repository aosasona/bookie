package com.trulyao.bookie.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.controllers.PostController
import com.trulyao.bookie.controllers.mockUser
import com.trulyao.bookie.entities.Like
import com.trulyao.bookie.entities.Post
import com.trulyao.bookie.entities.PostWithRelations
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.lib.ucFirst
import kotlinx.coroutines.launch

val PADDING = 16.dp

enum class PostEvent {
    Like, Dislike
}

@Composable
fun PostListItem(
    item: PostWithRelations,
    user: User,
    navigateToPostDetails: (Int) -> Unit,
    enterEditMode: (Post) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var menuIsExpanded by remember { mutableStateOf(false) }

    val likes = remember { mutableStateListOf<Like>() }
    LaunchedEffect(item) {
        likes.clear()
        likes.addAll(item.likes)
    }

    val userLike by remember { derivedStateOf { likes.find { i -> i.userId == user.id } } }

    // These are mostly unnecessary, but they make readability better
    val isLikedByUser: Boolean by remember { derivedStateOf { userLike != null && userLike!!.isDislike.not() } }
    val isDislikedByUser: Boolean by remember { derivedStateOf { userLike != null && userLike!!.isDislike } }


    // Handle optimistic UI thingies
    suspend fun removeEngagement() {
        // Sanity checks
        if (user.id == null) throw Exception("User ID is required")
        if (item.post.id == null) throw Exception("Post ID is required")

        // Optimistically update the UI
        likes.removeIf { it.userId == user.id }

        // Update the database
        PostController
            .getInstance(context.getDatabase().postDao())
            .removeEngagement(context, user.id, item.post.id)
    }

    suspend fun likePost() {
        // Sanity checks
        if (user.id == null) throw Exception("User ID is required")
        if (item.post.id == null) throw Exception("Post ID is required")

        // Optimistically update the UI
        likes.add(Like(userId = user.id, postId = item.post.id, createdAt = System.currentTimeMillis()))

        PostController
            .getInstance(context.getDatabase().postDao())
            .createEngagement(context = context, userId = user.id, postId = item.post.id, isDislike = false)
    }

    suspend fun dislikePost() {
        if (user.id == null) throw Exception("User ID is required")
        if (item.post.id == null) throw Exception("Post ID is required")
        // Optimistically update the UI
        likes.add(Like(userId = user.id, postId = item.post.id, isDislike = true, createdAt = System.currentTimeMillis()))

        PostController
            .getInstance(context.getDatabase().postDao())
            .createEngagement(context = context, userId = user.id, postId = item.post.id, isDislike = true)
    }

    suspend fun handleEvent(event: PostEvent) {
        try {
            if (user.id == null) throw Exception("User ID is required")

            // If we have liked or disliked the post, remove the engagement
            if (userLike != null) {
                val userEngagement = if (userLike!!.isDislike) PostEvent.Dislike else PostEvent.Like
                if (userEngagement == event) {
                    removeEngagement()
                    return
                }
            }

            when (event) {
                PostEvent.Like -> {
                    if (isDislikedByUser) removeEngagement()
                    likePost()
                }

                PostEvent.Dislike -> {
                    if (isLikedByUser) removeEngagement()
                    dislikePost()
                }
            }
        } catch (e: Exception) {
            removeEngagement() // Rollback the optimistic UI update
            handleException(context, e)
        }
    }

    Surface(modifier = Modifier.fillMaxWidth(), onClick = { navigateToPostDetails(item.post.id!!) }) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PADDING), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.user.firstName.ucFirst()} ${item.user.lastName.ucFirst()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )

                if (item.user.id == user.id) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(onClick = { menuIsExpanded = true }) {
                            Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
                        }

                        DropdownMenu(expanded = menuIsExpanded, onDismissRequest = { menuIsExpanded = false }) {
                            DropdownMenuItem(
                                text = { TextIconButton(text = "Edit", icon = Icons.Default.Edit) },
                                onClick = { enterEditMode(item.post) }
                            )

                            DropdownMenuItem(
                                text = { TextIconButton(text = "Delete", icon = Icons.Default.Delete, color = Color.Red) },
                                onClick = { /*TODO*/ }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = PADDING)
                    .padding(vertical = 6.dp)
            ) {
                Text(text = item.post.content, color = MaterialTheme.colorScheme.onSurface, overflow = TextOverflow.Ellipsis, maxLines = 4)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PADDING)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ActionButton(
                        count = likes.count { it.isDislike.not() },
                        contentDescription = "Like",
                        icon = if (isLikedByUser) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        color = if (isLikedByUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        onClick = { scope.launch { handleEvent(PostEvent.Like) } }
                    )

                    ActionButton(
                        count = likes.count { it.isDislike },
                        contentDescription = "Dislike",
                        icon = if (isDislikedByUser) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        color = if (isDislikedByUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        onClick = { scope.launch { handleEvent(PostEvent.Dislike) } }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ModeComment, contentDescription = "Comments")
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "${item.comments.size.toString()} comment" + if (item.comments.size != 1) "s" else "",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.size(4.dp))
            HorizontalDivider()

        }
    }
}

@Composable
fun ActionButton(count: Int, color: Color, contentDescription: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.clip(shape = RoundedCornerShape(20.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(20.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Icon(icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = count.toString(), color = color)
        }
    }
}

@Composable
fun TextIconButton(
    text: String, icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = text, tint = color)
        Text(text = text, color = color)
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PostListItemPreview() {
    val post = PostWithRelations(
        post = Post(
            content = "Hello, world!",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            ownerId = 1,
        ),
        user = mockUser(Role.Student),
        likes = mutableListOf(),
        comments = mutableListOf()
    )

    PostListItem(post, mockUser(Role.Student), navigateToPostDetails = {}, enterEditMode = {})
}