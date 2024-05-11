package com.trulyao.bookie.controllers

import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.AppException
import com.trulyao.bookie.lib.nameRegex
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminController private constructor(
    private val dao: UserDao,
    private val dispatcher: CoroutineDispatcher,
) {
    companion object {
        @Volatile
        private var instance: AdminController? = null
        fun getInstance(
            dao: UserDao,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): AdminController {
            return if (this.instance != null) {
                this.instance!!
            } else {
                synchronized(this) {
                    instance ?: AdminController(dao, dispatcher).also { repo ->
                        instance = repo
                    }
                }
            }
        }
    }

    suspend fun getAdmins(): List<User> {
        return withContext(dispatcher) { dao.getAdminUsers() }
    }

    suspend fun getStudents(): List<User> {
        return withContext(dispatcher) { dao.getStudentUsers() }
    }

    suspend fun updateAdminProfile(
        uid: Int?,
        firstName: String?,
        lastName: String?,
    ) {
        if (uid == null || uid == 0) {
            throw AppException("User ID is required")
        }

        if (firstName == null || firstName.length < 2 || !nameRegex.matches(firstName)) {
            throw AppException("First name must be at least 2 characters with only valid alphabets")
        }

        if (lastName == null || lastName.length < 2 || !nameRegex.matches(lastName)) {
            throw AppException("Last name must be at least 2 characters with only valid alphabets")
        }

        val user = withContext(dispatcher) {
            dao.findByID(id = uid)
        } ?: throw AppException("Invalid User ID")

        user.firstName = firstName
        user.lastName = lastName

        withContext(dispatcher) {
            dao.updateUser(user)
        }
    }
}