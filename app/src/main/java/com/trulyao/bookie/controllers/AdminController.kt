package com.trulyao.bookie.controllers

import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.User
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

    suspend fun getAdminUsers(): List<User> {
        return withContext(dispatcher) { dao.getAdminUsers() }
    }
}