package com.oncology.handbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户新增的手册章节
 * 与内置章节（ManualContent中硬编码）区分开
 */
@Entity(tableName = "user_sections")
data class UserSection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: String,
    val title: String,
    val htmlContent: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
