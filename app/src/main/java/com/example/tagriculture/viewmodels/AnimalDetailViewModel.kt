package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase
import com.example.tagriculture.data.database.Tag
import com.example.tagriculture.data.database.WeightEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnimalDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val tagDao = AppDatabase.getDatabase(application).tagDao()
    private val weightEntryDao = AppDatabase.getDatabase(application).weightEntryDao()
    private val _animalDetails = MutableLiveData<Animal?>()
    val animalDetails: LiveData<Animal?> = _animalDetails
    private val _healthAlert = MutableLiveData<Boolean>(false)
    val healthAlert: LiveData<Boolean> = _healthAlert

    fun getWeightHistory(animalId: Long): LiveData<List<WeightEntry>> {
        val historyLiveData = weightEntryDao.getWeightHistoryForAnimal(animalId)

        historyLiveData.observeForever { history ->
            if (history.size >= 2) {
                val lastWeight = history.last().weight
                val previousWeight = history[history.size - 2].weight
                _healthAlert.postValue(lastWeight < previousWeight)
            } else {
                _healthAlert.postValue(false)
            }
        }

        return historyLiveData
    }

    override fun onCleared() {
        super.onCleared()
        weightEntryDao.getWeightHistoryForAnimal(0).removeObserver {}
    }

    fun loadAnimalDetails(animalId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val animal = animalDao.getAnimalById(animalId)
            _animalDetails.postValue(animal)
        }
    }

    fun saveNewAnimal(
        nfcTagId: String,
        animalType: String,
        name: String,
        breed: String,
        birthDate: Long,
        birthWeight: Double,
        pictureUri: String?
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
                locationMunicipal = "Demo Area",
                pictureUri = pictureUri
            )

            val newAnimalId = animalDao.insertAnimal(newAnimal)

            val initialWeightEntry = WeightEntry(
                animalId = newAnimalId,
                weight = birthWeight,
                date = System.currentTimeMillis()
            )
            weightEntryDao.insertWeightEntry(initialWeightEntry)

            val newTag = Tag(
                nfcSerialNumber = nfcTagId,
                animalId = newAnimalId
            )

            tagDao.insertTag(newTag)
        }
    }

    fun updateAnimal(
        animalToUpdate: Animal,
        newType: String,
        newName: String,
        newBreed: String,
        newBirthDate: Long,
        newCurrentWeight: Double,
        newPictureUri: String?
    ) {
        val weightHasChanged = animalToUpdate.currentWeight != newCurrentWeight

        val updatedAnimal = animalToUpdate.copy(
            animalType = newType,
            name = newName,
            breed = newBreed,
            birthDate = newBirthDate,
            currentWeight = newCurrentWeight,
            pictureUri = newPictureUri
        )

        viewModelScope.launch(Dispatchers.IO) {
            animalDao.updateAnimal(updatedAnimal)

            if (weightHasChanged) {
                val newWeightEntry = WeightEntry(
                    animalId = updatedAnimal.id,
                    weight = newCurrentWeight,
                    date = System.currentTimeMillis()
                )
                weightEntryDao.insertWeightEntry(newWeightEntry)
            }
        }
    }
}