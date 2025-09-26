package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.analytics.AnalyticsEngine
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MarketReadyAnimal(
    val animal: Animal,
    val readyDate: Long,
    val formattedDate: String
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val weightEntryDao = AppDatabase.getDatabase(application).weightEntryDao()

    private val _marketReadyList = MutableLiveData<List<MarketReadyAnimal>>()
    val marketReadyList: LiveData<List<MarketReadyAnimal>> = _marketReadyList

    init {
        loadMarketReadyAnimals()
    }

    private fun loadMarketReadyAnimals() {
        viewModelScope.launch(Dispatchers.IO) {
            val allAnimals = animalDao.getAllAnimalsForSeeding()
            val marketList = mutableListOf<MarketReadyAnimal>()
            val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)

            for (animal in allAnimals) {
                val history = weightEntryDao.getWeightHistoryForAnimalSync(animal.id)
                val projectedDate = AnalyticsEngine.getProjectedMarketDate(animal, history)

                projectedDate?.let {
                    marketList.add(
                        MarketReadyAnimal(
                            animal = animal,
                            readyDate = it,
                            formattedDate = "Market Ready by: ${dateFormat.format(Date(it))}"
                        )
                    )
                }
            }

            _marketReadyList.postValue(marketList.sortedBy { it.readyDate })
        }
    }
}