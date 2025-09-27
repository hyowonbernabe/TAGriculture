package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.analytics.AnalyticsEngine
import com.example.tagriculture.analytics.AnalyticsReport
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase
import com.example.tagriculture.data.database.Notification
import com.example.tagriculture.data.database.Tag
import com.example.tagriculture.data.database.WeightEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnimalDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val tagDao = AppDatabase.getDatabase(application).tagDao()
    private val weightEntryDao = AppDatabase.getDatabase(application).weightEntryDao()
    private val notificationDao = AppDatabase.getDatabase(application).notificationDao()

    private val _loadedAnimalId = MutableLiveData<Long>()

    val animalDetails: LiveData<Animal?> = _loadedAnimalId.switchMap { id ->
        animalDao.getAnimalByIdAsLiveData(id)
    }

    val weightHistory: LiveData<List<WeightEntry>> = _loadedAnimalId.switchMap { id ->
        weightEntryDao.getWeightHistoryForAnimal(id)
    }

    val analyticsReport = MediatorLiveData<AnalyticsReport>()
    val healthAlert: LiveData<Boolean> = weightHistory.map { history ->
        history != null && history.size >= 2 && history.last().weight < history[history.size - 2].weight
    }

    init {
        analyticsReport.addSource(animalDetails) { animal ->
            val history = weightHistory.value
            if (animal != null && !history.isNullOrEmpty()) {
                val report = AnalyticsEngine.generateReport(animal, history)
                analyticsReport.value = report
                // Generate readiness notifications when animal details are updated
                generateReadinessNotifications(animal, report.readinessAlerts)
            }
        }
        analyticsReport.addSource(weightHistory) { history ->
            val animal = animalDetails.value
            if (animal != null && !history.isNullOrEmpty()) {
                analyticsReport.value = AnalyticsEngine.generateReport(animal, history)
            }
        }

        healthAlert.observeForever { isAlert ->
            val animal = animalDetails.value
            if (isAlert && animal != null) {
                generateHealthNotification(animal)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        healthAlert.removeObserver { }
    }

    fun loadAnimal(animalId: Long) {
        if (_loadedAnimalId.value == animalId) return
        _loadedAnimalId.value = animalId
    }

    private fun generateReadinessNotifications(animal: Animal, alerts: List<Pair<com.example.tagriculture.analytics.AlertType, String>>) {
        viewModelScope.launch(Dispatchers.IO) {
            alerts.forEach { alertPair ->
                val notification = Notification(
                    animalId = animal.id,
                    animalName = animal.name,
                    alertType = alertPair.first.name,
                    message = alertPair.second,
                    timestamp = System.currentTimeMillis()
                )
                notificationDao.insertNotification(notification)
            }
        }
    }

    private fun generateHealthNotification(animal: Animal) {
        viewModelScope.launch(Dispatchers.IO) {
            val notification = Notification(
                animalId = animal.id,
                animalName = animal.name,
                alertType = "HEALTH",
                message = "Weight has decreased since last measurement.",
                timestamp = System.currentTimeMillis()
            )
            notificationDao.insertNotification(notification)
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
        animalToUpdate: Animal,
        newType: String,
        newName: String,
        newBreed: String,
        newBirthDate: Long,
        newPictureUri: String?
    ) {

        val updatedAnimal = animalToUpdate.copy(
            animalType = newType,
            name = newName,
            breed = newBreed,
            birthDate = newBirthDate,
            pictureUri = newPictureUri
        )

        viewModelScope.launch(Dispatchers.IO) {
            animalDao.updateAnimal(updatedAnimal)
        }
    }

    fun addNewWeightEntry(animalId: Long, newWeight: Double, date: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val animalToUpdate = animalDao.getAnimalById(animalId)
            if (animalToUpdate != null) {
                val newWeightEntry = WeightEntry(
                    animalId = animalId,
                    weight = newWeight,
                    date = date
                )
                weightEntryDao.insertWeightEntry(newWeightEntry)

                val updatedAnimal = animalToUpdate.copy(currentWeight = newWeight)
                animalDao.updateAnimal(updatedAnimal)
            }
        }
    }
}