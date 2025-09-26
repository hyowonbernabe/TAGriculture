package com.example.tagriculture.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    foreignKeys = [ForeignKey(
        entity = Animal::class,
        parentColumns = ["id"],
        childColumns = ["animalId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val animalId: Long,
    val weight: Double,
    val date: Long
)