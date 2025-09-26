package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map // NEW IMPORT
import androidx.lifecycle.switchMap // NEW IMPORT
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.analytics.AnalyticsEngine
import com.example.tagriculture.analytics.AnalyticsReport
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase
import com.example.tagriculture.data.database.Tag
import com.example.tagriculture.data.database.WeightEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// The 'import androidx.lifecycle.Transformations' line has been removed.

class AnimalDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val tagDao = AppDatabase.getDatabase(application).tagDao()
    private val weightEntryDao = AppDatabase.getDatabase(application).weightEntryDao()

    private val _loadedAnimalId = MutableLiveData<Long>()

    // --- REFACTORED using .switchMap extension function ---
    val animalDetails: LiveData<Animal?> = _loadedAnimalId.switchMap { id ->
        animalDao.getAnimalByIdAsLiveData(id)
    }

    // --- REFACTORED using .switchMap extension function ---
    val weightHistory: LiveData<List<WeightEntry>> = _loadedAnimalId.switchMap { id ->
        weightEntryDao.getWeightHistoryForAnimal(id)
    }

    val analyticsReport = MediatorLiveData<AnalyticsReport>()

    // --- REFACTORED using .map extension function ---
    val healthAlert: LiveData<Boolean> = weightHistory.map { history ->
        history != null && history.size >= 2 && history.last().weight < history[history.size - 2].weight
    }

    init {
        analyticsReport.addSource(animalDetails) { animal ->
            val history = weightHistory.value
            if (animal != null && !history.isNullOrEmpty()) {
                analyticsReport.value = AnalyticsEngine.generateReport(animal, history)
            }
        }
        analyticsReport.addSource(weightHistory) { history ->
            val animal = animalDetails.value
            if (animal != null && !history.isNullOrEmpty()) {
                analyticsReport.value = AnalyticsEngine.generateReport(animal, history)
            }
        }
    }

    fun loadAnimal(animalId: Long) {
        if (_loadedAnimalId.value == animalId) return
        _loadedAnimalId.value = animalId
    }

    fun saveNewAnimal(
        nfcTagId: String, animalType: String, name: String, breed: String,
        birthDate: Long, birthWeight: Double, pictureUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAnimal = Animal(
                animalType = animalType, name = name, breed = breed, birthDate = birthDate,
                birthWeight = birthWeight, currentWeight = birthWeight,
                locationCity = "Demo City", locationMunicipal = "Demo Area", pictureUri = pictureUri
            )
            val newAnimalId = animalDao.insertAnimal(newAnimal)
            val initialWeightEntry = WeightEntry(
                animalId = newAnimalId, weight = birthWeight, date = System.currentTimeMillis()
            )
            weightEntryDao.insertWeightEntry(initialWeightEntry)
            val newTag = Tag(nfcSerialNumber = nfcTagId, animalId = newAnimalId)
            tagDao.insertTag(newTag)
        }
    }

    fun updateAnimal(
        animalToUpdate: Animal, newType: String, newName: String, newBreed: String,
        newBirthDate: Long, newCurrentWeight: Double, newPictureUri: String?
    ) {
        val weightHasChanged = animalToUpdate.currentWeight != newCurrentWeight
        val updatedAnimal = animalToUpdate.copy(
            animalType = newType, name = newName, breed = newBreed, birthDate = newBirthDate,
            currentWeight = newCurrentWeight, pictureUri = newPictureUri
        )
        viewModelScope.launch(Dispatchers.IO) {
            animalDao.updateAnimal(updatedAnimal)
            if (weightHasChanged) {
                val newWeightEntry = WeightEntry(
                    animalId = updatedAnimal.id, weight = newCurrentWeight, date = System.currentTimeMillis()
                )
                weightEntryDao.insertWeightEntry(newWeightEntry)
            }
        }
    }
}