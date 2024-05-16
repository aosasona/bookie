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
    @ColumnInfo(name = "is_dislike", defaultValue = "false") var isDislike: Boolean = false,
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

data class PostWithRelations(
    @Embedded val post: Post,

    @Relation(parentColumn = "id", entityColumn = "post_id")
    var likes: List<Like>,

    @Relation(parentColumn = "owner_id", entityColumn = "id")
    val user: User,

    @Relation(parentColumn = "id", entityColumn = "parent_id")
    val comments: List<Post>,
)