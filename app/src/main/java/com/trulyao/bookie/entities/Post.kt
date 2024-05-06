package com.trulyao.bookie.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "likes", indices = [Index(value = ["user_id", "post_id"], unique = true)])
data class Like(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "post_id") val postId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo var content: String,
    @ColumnInfo(name = "is_approved", defaultValue = "false") var isApproved: Boolean? = false,
    @ColumnInfo(name = "owner_id") val ownerId: Int,
    @ColumnInfo(name = "parent_id") val parentId: Int? = null, // Represents the parent post, for a comment
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") var modifiedAt: Long,
)

data class PostWithLikes(
    @Embedded val post: Post,

    @Relation(parentColumn = "id", entityColumn = "post_id")
    val likes: List<Like>,
)