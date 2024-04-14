package com.trulyao.bookie.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

enum class Role {
    Student,
    Admin,
}


@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,

    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "role") val role: Role,
    @ColumnInfo(name = "date_of_birth") val dateOfBirth: Date,
    @ColumnInfo(name = "network_name") val netHash: String, // used for network discovery and P2P instances

    @ColumnInfo(
        name = "created_at",
        defaultValue = "CURRENT_TIMESTAMP"
    ) val createdAt: Long? = null,

    @ColumnInfo(
        name = "modified_at",
        defaultValue = "CURRENT_TIMESTAMP"
    ) val modifiedAt: Long? = null,
)


fun getRoleMapping(): HashMap<Role, Int> {
    val roleMapping = HashMap<Role, Int>();
    roleMapping[Role.Student] = 1
    roleMapping[Role.Admin] = 2

    return roleMapping
}