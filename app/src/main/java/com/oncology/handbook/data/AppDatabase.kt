package com.oncology.handbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oncology.handbook.data.dao.CategoryDao
import com.oncology.handbook.data.dao.UserContentDao
import com.oncology.handbook.data.entity.Category
import com.oncology.handbook.data.entity.UserContent

@Database(
    entities = [UserContent::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userContentDao(): UserContentDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oncology_handbook.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
