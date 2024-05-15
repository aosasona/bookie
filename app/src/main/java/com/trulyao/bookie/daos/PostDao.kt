package com.trulyao.bookie.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.trulyao.bookie.entities.Post
import com.trulyao.bookie.entities.UserPostAndLikes

@Dao
interface LikeDao {

}

@Dao
interface PostDao {
    @Insert
    fun createPost(post: Post): Long

    @Query("SELECT p.* FROM users u LEFT JOIN posts p ON p.owner_id = u.id WHERE u.id = :userId")
    fun findPostsByUserId(userId: Int): List<Post>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun findPostById(postId: Int): Post?

    @Transaction
    @Query("SELECT * FROM posts")
    fun getAll(): List<UserPostAndLikes>

    @Update
    fun updatePost(post: Post)
}