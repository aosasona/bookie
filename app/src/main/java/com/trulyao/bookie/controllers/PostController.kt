package com.trulyao.bookie.controllers

import android.content.Context
import com.trulyao.bookie.daos.PostDao
import com.trulyao.bookie.entities.Like
import com.trulyao.bookie.entities.Post
import com.trulyao.bookie.entities.PostWithRelations
import com.trulyao.bookie.entities.PostWithUser
import com.trulyao.bookie.lib.AppDatabase
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

    suspend fun approvePost(postId: Int?) {
        if (postId == 0 || postId == null) throw AppException("Post ID is required")

        val post = withContext(dispatcher) {
            dao.findPostById(postId)
        } ?: throw AppException("Post does not exist")

        post.isApproved = true

        withContext(dispatcher) {
            dao.updatePost(post)
        }
    }

    suspend fun createPost(context: Context, userId: Int?, content: String): Long {
        if (userId == 0 || userId == null) throw AppException("User ID is required")
        if (content.isBlank()) throw AppException("Content is required")

        val user = withContext(dispatcher) {
            context.getDatabase().userDao().findByID(userId)
        } ?: throw AppException("User does not exist")

        if (content.isBlank()) throw AppException("Content is required")

        val data = Post(
            content = content.trim(),
            ownerId = userId,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
        )

        val insertedPostId = withContext(dispatcher) {
            dao.createPost(data)
        }

        return insertedPostId
    }

    suspend fun createComment(context: Context, userId: Int?, parentId: Int?, content: String) {
        if (userId == 0 || userId == null) throw AppException("User ID is required")
        if (parentId == 0 || parentId == null) throw AppException("Parent ID is required")
        if (content.isBlank()) throw AppException("Content is required")

        val user = withContext(dispatcher) {
            context.getDatabase().userDao().findByID(userId)
        } ?: throw AppException("User does not exist")

        if (content.isBlank()) throw AppException("Content is required")

        val data = Post(
            content = content.trim(),
            ownerId = userId,
            parentId = parentId,
            isApproved = true, // Pre-approve comments
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
        )

        withContext(dispatcher) {
            dao.createPost(data)
        }
    }

    suspend fun deletePost(postId: Int?) {
        if (postId == 0 || postId == null) throw AppException("Post ID is required")

        val post = withContext(dispatcher) {
            dao.findPostById(postId)
        } ?: throw AppException("Post does not exist")

        withContext(dispatcher) { dao.deletePost(post) }
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

    suspend fun getPosts(): List<PostWithRelations> {
        val posts = withContext(dispatcher) {
            dao.getAllPostsWithRelations()
        }

        // Strip sensitive fields from the data
        posts.forEach { post ->
            post.user.password = ""
            post.user.netHash = ""
        }

        return posts
    }

    suspend fun getPostWithRelationsById(id: Int?): PostWithRelations {
        if (id == 0 || id == null) throw AppException("Post ID is required")

        val post = withContext(dispatcher) {
            dao.findPostWithRelationsById(id)
        } ?: throw AppException("Post does not exist")

        // Strip sensitive fields from the data
        post.user.password = ""
        post.user.netHash = ""

        return post
    }

    suspend fun getPostsByUserId(userId: Int?): List<PostWithRelations> {
        if (userId == 0 || userId == null) throw AppException("User ID is required")

        val posts = withContext(dispatcher) {
            dao.findPostsWithRelationsByUserId(userId)
        }

        // Strip sensitive fields from the data
        posts.forEach { post ->
            post.user.password = ""
            post.user.netHash = ""
        }

        return posts
    }

    suspend fun getUnapprovedPosts(): List<PostWithUser> {
        return withContext(dispatcher) {
            dao.getUnapprovedPosts()
        }
    }

    suspend fun createEngagement(context: Context, userId: Int?, postId: Int?, isDislike: Boolean = false) {
        if (userId == 0 || userId == null) throw AppException("User ID is required")
        if (postId == 0 || postId == null) throw AppException("Post ID is required")

        val likeDao = AppDatabase.getInstance(context).likeDao()

        // Find an existing like, if it exists, update it
        val like = withContext(dispatcher) {
            likeDao.findLikeByUserIdAndPostId(userId, postId)
        } ?: Like(userId = userId, postId = postId, isDislike = isDislike, createdAt = System.currentTimeMillis())

        withContext(dispatcher) {
            likeDao.upsertLike(like)
        }
    }

    suspend fun removeEngagement(context: Context, userId: Int?, postId: Int?) {
        if (userId == 0 || userId == null) throw AppException("User ID is required")
        if (postId == 0 || postId == null) throw AppException("Post ID is required")

        val likeDao = AppDatabase.getInstance(context).likeDao()

        // Silently return if the like does not exist, we do not need to throw an error since it does not exist
        val engagement = withContext(dispatcher) {
            likeDao.findLikeByUserIdAndPostId(userId, postId)
        } ?: return;

        withContext(dispatcher) {
            likeDao.deleteLike(engagement)
        }
    }
}