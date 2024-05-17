package com.trulyao.bookie.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.trulyao.bookie.entities.Post
import com.trulyao.bookie.entities.PostWithRelations


@Dao
interface PostDao {
    @Insert
    fun createPost(post: Post): Long

    @Query("SELECT * FROM posts WHERE owner_id = :userId AND parent_id IS NULL ORDER BY created_at DESC")
    fun findPostsWithRelationsByUserId(userId: Int): List<PostWithRelations>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun findPostById(postId: Int): Post?

    @Query("SELECT * FROM posts WHERE id = :postId AND parent_id IS NULL")
    fun findPostWithRelationsById(postId: Int): PostWithRelations?

    @Transaction
    @Query("SELECT * FROM posts WHERE is_approved = 1 AND parent_id IS NULL ORDER BY created_at DESC")
    fun getAllPostsWithRelations(): List<PostWithRelations>

    @Update
    fun updatePost(post: Post)

    @Delete
    fun deletePost(post: Post)
}