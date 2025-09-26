package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase
import com.example.tagriculture.data.database.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnimalDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val tagDao = AppDatabase.getDatabase(application).tagDao()

    fun saveNewAnimal(
        nfcTagId: String,
        animalType: String,
        name: String,
        breed: String,
        birthDate: Long,
        birthWeight: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAnimal = Animal(
                animalType = animalType,
                name = name,
                breed = breed,
                birthDate = birthDate,
                birthWeight = birthWeight,
                currentWeight = birthWeight,
                locationCity = "Demo City",
                locationMunicipal = "Demo Area"
            )

            val newAnimalId = animalDao.insertAnimal(newAnimal)

            val newTag = Tag(
                nfcSerialNumber = nfcTagId,
                animalId = newAnimalId
            )

            tagDao.insertTag(newTag)
        }
    }
}