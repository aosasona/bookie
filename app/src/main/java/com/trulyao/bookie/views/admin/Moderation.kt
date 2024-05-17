package com.trulyao.bookie.views.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trulyao.bookie.components.ProtectedView
import com.trulyao.bookie.components.TextInput
import com.trulyao.bookie.controllers.PostController
import com.trulyao.bookie.entities.PostWithUser
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.DEFAULT_VIEW_PADDING
import com.trulyao.bookie.lib.getDatabase
import com.trulyao.bookie.lib.handleException
import com.trulyao.bookie.lib.ucFirst
import com.trulyao.bookie.views.LoadingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Moderation(user: User?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoadingPosts by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    val posts = remember { mutableStateListOf<PostWithUser>() }
    val searchResults = remember { mutableStateListOf<PostWithUser>() }

    fun searchPosts() {
        searchResults.clear()
        searchResults.addAll(posts.filter { it.post.content.contains(query, ignoreCase = true) })
    }

    suspend fun loadPosts() {
        try {
            isLoadingPosts = true
            val controller = PostController.getInstance(context.getDatabase().postDao())
            val allPosts = controller.getUnapprovedPosts()
            posts.clear()
            posts.addAll(allPosts)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            isLoadingPosts = false
        }
    }

    suspend fun approvePost(postId: Int) {
        try {
            val controller = PostController.getInstance(context.getDatabase().postDao())
            controller.approvePost(postId)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            loadPosts()
        }
    }

    suspend fun deletePost(postId: Int) {
        try {
            val controller = PostController.getInstance(context.getDatabase().postDao())
            controller.deletePost(postId)
        } catch (e: Exception) {
            handleException(context, e)
        } finally {
            loadPosts()
        }
    }

    LaunchedEffect(true) {
        loadPosts()
    }

    if (isLoadingPosts) {
        LoadingScreen()
        return
    }

    ProtectedView(user = user, allow = listOf(Role.Admin, Role.SuperAdmin)) {
        Scaffold(
            topBar = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Moderation", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { scope.launch { loadPosts() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            },
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                item {
                    Box(modifier = Modifier.padding(DEFAULT_VIEW_PADDING)) {
                        TextInput(title = "Search", value = query, onChange = { query = it; searchPosts() })
                    }
                }

                if (query.isNotEmpty() && searchResults.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No results found", modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                items(
                    if (query.isEmpty()) posts else searchResults
                ) { item ->
                    PostItem(
                        item = item,
                        approve = { id -> scope.launch { approvePost(id) } },
                        delete = { id -> scope.launch { deletePost(id) } },
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(
    item: PostWithUser,
    approve: (Int) -> Unit,
    delete: (Int) -> Unit,
) {
    val postedOn by remember {
        derivedStateOf {
            // Format milliseconds to a readable date
            val date = item.post.createdAt
            val formattedDate = android.text.format.DateFormat.format("dd MMM yyyy", date)
            formattedDate.toString()
        }
    }


    Column(modifier = Modifier.padding(horizontal = DEFAULT_VIEW_PADDING), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Posted by ${item.user.firstName.ucFirst()} ${item.user.lastName.ucFirst()}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(item.post.content)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = postedOn, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = { delete(item.post.id!!) },
                ) {
                    Text("Delete")
                }

                Button(
                    onClick = { approve(item.post.id!!) }
                ) {
                    Text("Approve")
                }

            }
        }

        Spacer(modifier = Modifier.size(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.size(8.dp))
    }
}
