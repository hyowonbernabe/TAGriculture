package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.analytics.AnalyticsEngine
import com.example.tagriculture.data.database.AppDatabase
import com.example.tagriculture.data.database.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val weightEntryDao = AppDatabase.getDatabase(application).weightEntryDao()
    private val notificationDao = AppDatabase.getDatabase(application).notificationDao()

    val allNotifications: LiveData<List<Notification>> = notificationDao.getAllNotifications()

    fun refreshNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            val allAnimals = animalDao.getAllAnimalsForSeeding()
            val newNotificationList = mutableListOf<Notification>()

            for (animal in allAnimals) {
                val history = weightEntryDao.getWeightHistoryForAnimalSync(animal.id)
                if (history.isEmpty()) continue

                if (history.size >= 2 && history.last().weight < history[history.size - 2].weight) {
                    newNotificationList.add(
                        Notification(
                            animalId = animal.id,
                            animalName = animal.name,
                            alertType = "HEALTH",
                            message = "Weight has decreased since last measurement.",
                            timestamp = System.currentTimeMillis() + animal.id
                        )
                    )
                }

                val report = AnalyticsEngine.generateReport(animal, history)
                report.readinessAlerts.forEach { alertPair ->
                    newNotificationList.add(
                        Notification(
                            animalId = animal.id,
                            animalName = animal.name,
                            alertType = alertPair.first.name,
                            message = alertPair.second,
                            timestamp = System.currentTimeMillis() + animal.id
                        )
                    )
                }
            }

            notificationDao.clearAll()
            notificationDao.insertAll(newNotificationList)
        }
    }
}