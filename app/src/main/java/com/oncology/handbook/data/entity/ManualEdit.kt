package com.oncology.handbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户修改后的手册章节内容
 * sectionId 对应 ManualContent 中 ManualSection 的 id
 */
@Entity(tableName = "manual_edits")
data class ManualEdit(
    @PrimaryKey val sectionId: String,
    val htmlContent: String,
    val updatedAt: Long = System.currentTimeMillis()
)
