package com.example.tagriculture.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnimalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal): Long

    @Update
    suspend fun updateAnimal(animal: Animal)

    @Query("SELECT * FROM animals WHERE id = :animalId")
    suspend fun getAnimalById(animalId: Long): Animal?

    @Query("SELECT * FROM animals ORDER BY name ASC")
    fun getAllAnimals(): LiveData<List<Animal>>

    @Query("SELECT * FROM animals")
    fun getAllAnimalsForSeeding(): List<Animal>

    @Query("SELECT * FROM animals WHERE id = :animalId")
    fun getAnimalByIdAsLiveData(animalId: Long): LiveData<Animal?>
}