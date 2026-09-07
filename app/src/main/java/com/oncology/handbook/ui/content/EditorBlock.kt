package com.oncology.handbook.ui.content

import com.oncology.handbook.data.entity.ContentBlock

/** 编辑器中的内容块，id=0 表示尚未保存 */
data class EditorBlock(
    var type: Int,
    var text: String = "",
    var filePath: String = ""
) {
    fun toContentBlock(noteId: Long, order: Int) = ContentBlock(
        noteId = noteId,
        type = type,
        orderIndex = order,
        text = text,
        filePath = filePath
    )

    companion object {
        fun fromContentBlock(block: ContentBlock) = EditorBlock(
            type = block.type,
            text = block.text,
            filePath = block.filePath
        )
    }
}
