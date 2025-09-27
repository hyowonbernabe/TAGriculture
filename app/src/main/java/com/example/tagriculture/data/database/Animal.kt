package com.example.tagriculture.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val animalType: String,
    val name: String,
    val breed: String,

    val birthDate: Long,
    val birthWeight: Double,
    var currentWeight: Double,

    val locationCity: String,
    val locationMunicipal: String,

    var pictureUri: String? = null,
    var grade: String? = null
)