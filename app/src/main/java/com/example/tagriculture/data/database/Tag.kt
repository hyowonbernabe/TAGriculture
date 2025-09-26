package com.example.tagriculture.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    foreignKeys = [ForeignKey(
        entity = Animal::class,
        parentColumns = ["id"],
        childColumns = ["animalId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Tag(
    @PrimaryKey val nfcSerialNumber: String,
    var animalId: Long?
)