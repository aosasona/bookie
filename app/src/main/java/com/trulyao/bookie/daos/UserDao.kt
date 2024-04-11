package com.trulyao.bookie.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trulyao.bookie.entities.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.NONE)
    fun createUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun findByID(id: Int): User?
}