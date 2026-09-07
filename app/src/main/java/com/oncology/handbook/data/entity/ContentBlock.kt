package com.oncology.handbook.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 笔记内容块：支持文字、图片、视频混排
 * type: 0=文字, 1=图片, 2=视频
 */
@Entity(
    tableName = "content_blocks",
    indices = [Index("noteId")]
)
data class ContentBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val type: Int,
    val orderIndex: Int,
    val text: String = "",
    val filePath: String = ""
) {
    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
    }
}
