package com.trulyao.bookie.entities

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.trulyao.bookie.lib.Store
import com.trulyao.bookie.lib.StoreKey
import java.util.Date

enum class Role {
    Student,
    Admin,
    SuperAdmin,
}


@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,

    @ColumnInfo(name = "first_name") var firstName: String,
    @ColumnInfo(name = "last_name") var lastName: String,
    @ColumnInfo(name = "email") var email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "role") val role: Role,
    @ColumnInfo(name = "date_of_birth") var dateOfBirth: Date,
    @ColumnInfo(name = "network_name") val netHash: String, // used for network discovery and P2P instances

    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") var modifiedAt: Long,
)

data class UserWithPosts(
    @Embedded val user: User,

    @Relation(parentColumn = "id", entityColumn = "owner_id")
    val posts: List<Post>,
)


fun getRoleValue(role: Role): Int {
    return when (role) {
        Role.Student -> 1
        Role.Admin -> 2
        Role.SuperAdmin -> 3
    }
}

suspend fun signOut(context: Context) {
    Store.set(context, StoreKey.CurrentUserID, null)
}
