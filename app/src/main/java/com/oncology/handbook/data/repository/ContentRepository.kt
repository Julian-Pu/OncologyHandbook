package com.oncology.handbook.data.repository

import com.oncology.handbook.data.AppDatabase
import com.oncology.handbook.data.entity.UserContent
import kotlinx.coroutines.flow.Flow

class ContentRepository(private val database: AppDatabase) {

    fun getAllContents(): Flow<List<UserContent>> = database.userContentDao().getAll()

    fun getContentsByCategory(category: String): Flow<List<UserContent>> =
        database.userContentDao().getByCategory(category)

    fun searchContents(keyword: String): Flow<List<UserContent>> =
        database.userContentDao().search(keyword)

    fun getAllCategories(): Flow<List<String>> = database.userContentDao().getAllCategories()

    suspend fun getContentById(id: Long): UserContent? =
        database.userContentDao().getById(id)

    suspend fun insertContent(content: UserContent): Long =
        database.userContentDao().insert(content)

    suspend fun updateContent(content: UserContent) =
        database.userContentDao().update(content)

    suspend fun deleteContent(content: UserContent) =
        database.userContentDao().delete(content)

    suspend fun deleteContentById(id: Long) =
        database.userContentDao().deleteById(id)

    suspend fun getCount(): Int = database.userContentDao().getCount()
}
