package com.oncology.handbook.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oncology.handbook.data.entity.UserSection

@Dao
interface UserSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userSection: UserSection): Long

    @Update
    suspend fun update(userSection: UserSection)

    @Delete
    suspend fun delete(userSection: UserSection)

    @Query("SELECT * FROM user_sections WHERE id = :id")
    suspend fun getById(id: Long): UserSection?

    @Query("SELECT * FROM user_sections WHERE categoryId = :categoryId ORDER BY orderIndex, createdAt")
    suspend fun getByCategoryId(categoryId: String): List<UserSection>

    @Query("SELECT * FROM user_sections ORDER BY orderIndex, createdAt")
    suspend fun getAll(): List<UserSection>
}
