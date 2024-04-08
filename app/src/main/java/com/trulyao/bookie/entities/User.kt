package com.trulyao.bookie.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "email", index = true) val email: String,
    @ColumnInfo(name = "date_of_birth") val dateOfBirth: Date,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: Long?,
    @ColumnInfo(name = "modified_at", defaultValue = "CURRENT_TIMESTAMP") val modifiedAt: Long?,
)
