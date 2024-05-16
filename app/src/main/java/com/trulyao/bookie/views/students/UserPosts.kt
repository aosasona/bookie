package com.trulyao.bookie.views.students

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.trulyao.bookie.components.Feed
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.controllers.PostController
import com.trulyao.bookie.entities.PostWithRelations
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException

@Composable
fun UserPosts(
    user: User?,
    navigateToPostDetails: (Int) -> Unit,
) {
    val context = LocalContext.current
    val posts = remember { mutableStateListOf<PostWithRelations>() }
    var isLoadingPosts by remember { mutableStateOf(true) }

    suspend fun loadPosts(stopRefresh: () -> Unit) {
        try {
            isLoadingPosts = true
            val controller = PostController.getInstance(context.getDatabase().postDao())
            val allPosts = controller.getPostsByUserId(user?.id)
            posts.clear()
            posts.addAll(allPosts)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoadingPosts = false
            stopRefresh()
        }
    }

    ProtectedView(user = user) {
        Feed(
            user = user,
            posts = posts,
            isLoadingPosts = isLoadingPosts,
            showFab = false,
            load = { fn -> loadPosts(fn) },
            navigateToPostDetails = navigateToPostDetails
        )
    }
}