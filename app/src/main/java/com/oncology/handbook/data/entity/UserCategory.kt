package com.oncology.handbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户新增的手册大类（分类）
 */
@Entity(tableName = "user_categories")
data class UserCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val icon: String = "folder",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
