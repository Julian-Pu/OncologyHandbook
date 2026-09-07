package com.oncology.handbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_contents")
data class UserContent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val category: String = "未分类",
    val imagePaths: String = "",   // 多张图片路径用 | 分隔
    val videoPath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
