package com.oncology.handbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oncology.handbook.data.dao.CategoryDao
import com.oncology.handbook.data.dao.ContentBlockDao
import com.oncology.handbook.data.dao.ManualEditDao
import com.oncology.handbook.data.dao.UserContentDao
import com.oncology.handbook.data.dao.UserSectionDao
import com.oncology.handbook.data.dao.UserCategoryDao
import com.oncology.handbook.data.entity.Category
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.data.entity.ManualEdit
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.data.entity.UserSection
import com.oncology.handbook.data.entity.UserCategory

@Database(
    entities = [UserContent::class, Category::class, ContentBlock::class, ManualEdit::class, UserSection::class, UserCategory::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userContentDao(): UserContentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun contentBlockDao(): ContentBlockDao
    abstract fun manualEditDao(): ManualEditDao
    abstract fun userSectionDao(): UserSectionDao
    abstract fun userCategoryDao(): UserCategoryDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS manual_edits (
                        sectionId TEXT PRIMARY KEY NOT NULL,
                        htmlContent TEXT NOT NULL DEFAULT '',
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_sections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        htmlContent TEXT NOT NULL DEFAULT '',
                        orderIndex INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_sections_categoryId ON user_sections(categoryId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT 'folder',
                        orderIndex INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oncology_handbook.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
