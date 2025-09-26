package com.example.tagriculture.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeightEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(weightEntry: WeightEntry)

    @Query("SELECT * FROM weight_entries WHERE animalId = :animalId ORDER BY date ASC")
    fun getWeightHistoryForAnimal(animalId: Long): LiveData<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE animalId = :animalId ORDER BY date ASC")
    fun getWeightHistoryForAnimalSync(animalId: Long): List<WeightEntry>
}