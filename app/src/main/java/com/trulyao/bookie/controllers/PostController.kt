package com.trulyao.bookie.controllers

import android.content.Context
import com.trulyao.bookie.daos.PostDao
import com.trulyao.bookie.entities.Post
import com.trulyao.bookie.entities.UserPostAndLikes
import com.trulyao.bookie.lib.AppException
import com.trulyao.bookie.lib.getDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostController private constructor(
    private val dao: PostDao,
    private val dispatcher: CoroutineDispatcher,
) {
    companion object {
        @Volatile
        private var instance: PostController? = null
        fun getInstance(
            dao: PostDao,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): PostController {
            return if (this.instance != null) {
                this.instance!!
            } else {
                synchronized(this) {
                    instance ?: PostController(dao, dispatcher).also { repo -> instance = repo }
                }
            }
        }
    }

    suspend fun createPost(context: Context, userId: Int?, content: String): Long {
        if (userId == 0 || userId == null) throw AppException("User ID is required")

        val user = withContext(dispatcher) {
            context.getDatabase().userDao().findByID(userId) ?: throw AppException("User does not exist")
        }

        if (content.isBlank()) throw AppException("Content is required")

        val data = Post(
            content = content,
            ownerId = userId,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
        )

        val insertedPostId = withContext(dispatcher) {
            dao.createPost(data)
        }

        return insertedPostId
    }

    suspend fun updatePostContent(postId: Int?, content: String) {
        if (postId == 0 || postId == null) throw AppException("Post ID is required")
        if (content.isBlank()) throw AppException("Content is required")

        val post = withContext(dispatcher) {
            dao.findPostById(postId)
        } ?: throw AppException("Post does not exist")

        post.content = content
        post.modifiedAt = System.currentTimeMillis()

        withContext(dispatcher) {
            dao.updatePost(post)
        }
    }

    suspend fun getPosts(): List<UserPostAndLikes> {
        val posts = withContext(dispatcher) {
            dao.getAll()
        }

        // Strip sensitive fields from the data
        posts.forEach { post ->
            post.user.password = ""
            post.user.netHash = ""
        }

        return posts
    }
}