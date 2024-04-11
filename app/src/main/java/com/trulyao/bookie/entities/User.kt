package com.trulyao.bookie.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.util.Date


@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,

    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "is_admin") val isAdmin: Boolean,
    @ColumnInfo(name = "date_of_birth") val dateOfBirth: Date,
    @ColumnInfo(
        name = "created_at",
        defaultValue = "CURRENT_TIMESTAMP"
    ) val createdAt: Long? = null,
    @ColumnInfo(
        name = "modified_at",
        defaultValue = "CURRENT_TIMESTAMP"
    ) val modifiedAt: Long? = null,
)

public fun hashPassword(password: String): String {
    val argon2 = Argon2Kt()
    val hashedPassword = argon2.hash(
        password = password.toByteArray(),
        mode = Argon2Mode.ARGON2_I,
        salt = "definitelyasecurepasswordhash".toByteArray(),
        tCostInIterations = 5,
        mCostInKibibyte = 65536
    )

    return hashedPassword.encodedOutputAsString()
}
