package com.trulyao.bookie.lib

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.daos.LikeDao
import com.trulyao.bookie.daos.PostDao
import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.Like
import com.trulyao.bookie.entities.Post
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.getRoleValue
import kotlinx.coroutines.Dispatchers
import java.util.Date
import java.util.concurrent.Executors

private val SHARED_THREAD_EXECUTOR = Executors.newSingleThreadExecutor()

@Database(
    version = 5,
    entities = [User::class, Post::class, Like::class],
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun likeDao(): LikeDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return if (this.instance != null) {
                this.instance!!
            } else {
                synchronized(this) { instance ?: this.build(context).also { db -> instance = db } }
            }
        }

        private fun build(context: Context): AppDatabase {
            return Room
                .databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "data.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(seedDatabase(context))
                .build()
        }

        private fun seedDatabase(context: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    SHARED_THREAD_EXECUTOR.execute {
                        val userDao = getInstance(context).userDao()
                        UserController.getInstance(userDao, Dispatchers.IO).createDefaultAdmin()
                    }
                }
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(value: Date): Long {
        return value.time
    }

    @TypeConverter
    fun fromInt(value: Int): Role {
        for (role in Role.entries) {
            if (getRoleValue(role) == value) return role
        }

        throw AppException("Invalid value provided for role mapping")
    }

    @TypeConverter
    fun roleToInt(role: Role): Int {
        for (r in Role.entries) {
            if (r == role) return getRoleValue(role)
        }

        throw AppException("Invalid role provided")
    }

}
