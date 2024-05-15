package com.trulyao.bookie.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.entities.Like
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.UserPostAndLikes
import com.trulyao.bookie.lib.ucFirst

val PADDING = 16.dp

@Composable
fun PostListItem(item: UserPostAndLikes, user: User) {
    var menuIsExpanded by remember { mutableStateOf(false) }

    val dislikes by remember {
        derivedStateOf { item.likes.filter { it.isDislike } }
    }

    // These are mostly unnecessary, but they make readability better
    val isLikedByUser: Boolean by remember {
        derivedStateOf { item.likes.any { it.userId == user.id && !it.isDislike } }
    }

    val isDislikedByUser: Boolean by remember {
        derivedStateOf { item.likes.any { it.userId == user.id && it.isDislike } }
    }

    // Handle optimistic UI thingies

    fun removeEngagement() {
        try {
            // Sanity checks
            if (user.id == null) throw Exception("User ID is required")
            if (item.post.id == null) throw Exception("Post ID is required")

            // Optimistically update the UI
            item.likes.removeIf { it.userId == user.id }

            // Update the database
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun toggleLike() {
        try {
            // Sanity checks
            if (user.id == null) throw Exception("User ID is required")
            if (item.post.id == null) throw Exception("Post ID is required")

            // Optimistically update the UI
            item.likes.add(Like(userId = user.id, postId = item.post.id, createdAt = System.currentTimeMillis()))

            // TODO: Update the database
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun toggleDislike() {
        try {
            if (user.id == null) throw Exception("User ID is required")
            if (item.post.id == null) throw Exception("Post ID is required")
            // Optimistically update the UI
            item.likes.add(Like(userId = user.id, postId = item.post.id, isDislike = true, createdAt = System.currentTimeMillis()))

            // TODO: Update the database
        } catch (e: Exception) {
            // Handle error
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${item.user.firstName.ucFirst()} ${item.user.lastName.ucFirst()}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)

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
                            text = {
                                TextIconButton(text = "Edit", icon = Icons.Default.Edit, onClick = { })
                            },
                            onClick = { /*TODO*/ }
                        )

                        DropdownMenuItem(
                            text = {
                                TextIconButton(text = "Delete", icon = Icons.Default.Delete, color = Color.Red, onClick = { })
                            },
                            onClick = { /*TODO*/ }
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.padding(horizontal = PADDING)) {
            Text(text = item.post.content, color = MaterialTheme.colorScheme.onSurface, overflow = TextOverflow.Ellipsis, maxLines = 4)
        }

        Row {
            ActionButton(
                count = item.likes.size - dislikes.size,
                contentDescription = "Like",
                icon = if (isLikedByUser) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                color = if (isLikedByUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                onClick = { }
            )

            ActionButton(
                count = dislikes.size,
                contentDescription = "Dislike",
                icon = if (isDislikedByUser) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                color = if (isDislikedByUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                onClick = { }
            )
        }
    }
}

// TODO: remove count
@Composable
fun ActionButton(count: Int, color: Color, contentDescription: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.clip(RoundedCornerShape(4.dp))) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp),
        ) {
            Icon(icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size(16.dp))
            Text(text = count.toString())
        }
    }
}

@Composable
fun TextIconButton(
    text: String, icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, tint = color)
            Text(text = text, color = color)
        }
    }
}