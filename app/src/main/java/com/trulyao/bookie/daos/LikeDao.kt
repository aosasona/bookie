package com.trulyao.bookie.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.trulyao.bookie.entities.Like

@Dao
interface LikeDao {

    @Upsert
    fun upsertLike(like: Like): Long

    @Delete
    fun deleteLike(like: Like)

    @Query("SELECT * FROM likes WHERE user_id = :userId AND post_id = :postId")
    fun findLikeByUserIdAndPostId(userId: Int, postId: Int): Like?

}
