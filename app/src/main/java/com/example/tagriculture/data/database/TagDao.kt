package com.example.tagriculture.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Query("SELECT * FROM tags WHERE nfcSerialNumber = :serialNumber")
    suspend fun getTagBySerial(serialNumber: String): Tag?
}