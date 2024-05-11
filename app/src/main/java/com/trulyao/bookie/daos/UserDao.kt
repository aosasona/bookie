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

    @Query("DELETE FROM users WHERE id = :id")
    fun deleteByID(id: Int)

    @Query("SELECT * FROM users WHERE role IN (2, 3)")
    fun getAdminUsers(): List<User>

    @Query("SELECT * FROM users WHERE role = 1")
    fun getStudentUsers(): List<User>

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    fun updatePassword(userId: Int, newPassword: String)

    @Update
    fun updateUser(user: User)
}