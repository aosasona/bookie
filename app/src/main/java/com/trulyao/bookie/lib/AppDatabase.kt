package com.trulyao.bookie.lib

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.hashPassword
import java.util.Date
import java.util.concurrent.Executors

private val SHARED_THREAD_EXECUTOR = Executors.newSingleThreadExecutor()

@Database(entities = [User::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        private var instance: AppDatabase? = null

        public fun getInstance(context: Context): AppDatabase {
            return if (this.instance != null) {
                this.instance!!
            } else {
                synchronized(this) { instance ?: this.build(context).also { db -> instance = db } }
            }
        }

        private fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "data")
                .build()
        }

        private fun seedDatabase(context: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    SHARED_THREAD_EXECUTOR.execute {
                        val userDao = getInstance(context).userDao()
                        userDao.createUser(
                            User(
                                firstName = "Ayodeji",
                                lastName = "Osasona",
                                email = "admin@bookie.ac.uk",
                                password = hashPassword("admin123"),
                                isAdmin = true,
                                dateOfBirth = Date(2004, 6, 5),
                            ),
                        )
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
        return value.time.toLong()
    }
}
