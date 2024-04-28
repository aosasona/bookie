package com.trulyao.bookie.lib

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.trulyao.bookie.controllers.UserController
import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.entities.getRoleMapping
import kotlinx.coroutines.Dispatchers
import java.util.Date
import java.util.concurrent.Executors

private val SHARED_THREAD_EXECUTOR = Executors.newSingleThreadExecutor()

@Database(
    version = 1,
    entities = [User::class],
    exportSchema = true
)
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
            return Room
                .databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "data.db"
                )
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
        return value.time.toLong()
    }

    @TypeConverter
    fun fromInt(value: Int): Role {
        for (item in getRoleMapping()) {
            if (item.value == value) return item.key
        }

        throw AppException("Invalid value provided for role mapping")
    }

    @TypeConverter
    fun roleToInt(role: Role): Int {
        return getRoleMapping()[role] ?: throw AppException("Invalid role provided")
    }

}
