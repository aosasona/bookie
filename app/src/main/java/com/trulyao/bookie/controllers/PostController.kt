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

    suspend fun getPosts(): List<UserPostAndLikes> {
        return withContext(dispatcher) {
            dao.getAll()
        }
    }
}