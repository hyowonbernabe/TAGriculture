package com.example.tagriculture.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val animalId: Long,
    val animalName: String,
    val alertType: String,
    val message: String,
    val timestamp: Long
)