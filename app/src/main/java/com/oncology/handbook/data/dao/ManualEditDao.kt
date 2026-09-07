package com.oncology.handbook.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oncology.handbook.data.entity.ManualEdit

@Dao
interface ManualEditDao {

    @Query("SELECT * FROM manual_edits WHERE sectionId = :sectionId")
    suspend fun getBySectionId(sectionId: String): ManualEdit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(edit: ManualEdit)

    @Query("DELETE FROM manual_edits WHERE sectionId = :sectionId")
    suspend fun deleteBySectionId(sectionId: String)
}
