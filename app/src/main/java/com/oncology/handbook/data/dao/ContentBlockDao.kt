package com.oncology.handbook.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.oncology.handbook.data.entity.ContentBlock

@Dao
interface ContentBlockDao {

    @Query("SELECT * FROM content_blocks WHERE noteId = :noteId ORDER BY orderIndex ASC")
    suspend fun getByNoteId(noteId: Long): List<ContentBlock>

    @Insert
    suspend fun insert(block: ContentBlock): Long

    @Insert
    suspend fun insertAll(blocks: List<ContentBlock>)

    @Query("DELETE FROM content_blocks WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Long)
}
