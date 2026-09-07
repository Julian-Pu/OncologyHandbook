package com.oncology.handbook.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oncology.handbook.data.entity.UserCategory

@Dao
interface UserCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userCategory: UserCategory): Long

    @Update
    suspend fun update(userCategory: UserCategory)

    @Delete
    suspend fun delete(userCategory: UserCategory)

    @Query("SELECT * FROM user_categories WHERE id = :id")
    suspend fun getById(id: Long): UserCategory?

    @Query("SELECT * FROM user_categories ORDER BY orderIndex, createdAt")
    suspend fun getAll(): List<UserCategory>
}
