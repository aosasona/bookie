package com.trulyao.bookie.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trulyao.bookie.entities.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.NONE)
    fun createUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun findByID(id: Int): User?

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    fun updatePassword(userId: Int, newPassword: String)

    //    @Query("UPDATE users SET first_name = :firstName, last_name = :lastName, email = :email, date_of_birth = :dateOfBirth WHERE id = :userId")
//    fun updateUser(
//        userId: Int,
//        firstName: String,
//        lastName: String,
//        email: String,
//        dateOfBirth: Date,
//    )
    @Update
    fun updateUser(user: User)
}