package com.oncology.handbook.data.dao

import androidx.room.*
import com.oncology.handbook.data.entity.UserContent
import kotlinx.coroutines.flow.Flow

@Dao
interface UserContentDao {

    @Query("SELECT * FROM user_contents ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<UserContent>>

    @Query("SELECT * FROM user_contents WHERE category = :category ORDER BY updatedAt DESC")
    fun getByCategory(category: String): Flow<List<UserContent>>

    @Query("SELECT * FROM user_contents WHERE id = :id")
    suspend fun getById(id: Long): UserContent?

    @Query("SELECT * FROM user_contents WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY updatedAt DESC")
    fun search(keyword: String): Flow<List<UserContent>>

    @Query("SELECT DISTINCT category FROM user_contents ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: UserContent): Long

    @Update
    suspend fun update(content: UserContent)

    @Delete
    suspend fun delete(content: UserContent)

    @Query("DELETE FROM user_contents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM user_contents")
    suspend fun getCount(): Int
}
