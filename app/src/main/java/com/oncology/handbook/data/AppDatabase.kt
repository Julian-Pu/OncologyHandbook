package com.oncology.handbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oncology.handbook.data.dao.CategoryDao
import com.oncology.handbook.data.dao.ContentBlockDao
import com.oncology.handbook.data.dao.UserContentDao
import com.oncology.handbook.data.entity.Category
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.data.entity.UserContent

@Database(
    entities = [UserContent::class, Category::class, ContentBlock::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userContentDao(): UserContentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun contentBlockDao(): ContentBlockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS content_blocks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        noteId INTEGER NOT NULL,
                        type INTEGER NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        text TEXT NOT NULL DEFAULT '',
                        filePath TEXT NOT NULL DEFAULT ''
                    )
                    """
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_content_blocks_noteId ON content_blocks(noteId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oncology_handbook.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
