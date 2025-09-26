package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()

    private val allAnimals: LiveData<List<Animal>> = animalDao.getAllAnimals()

    private val searchQuery = MutableLiveData("")
    private val filterType = MutableLiveData("All")
    private val sortOrder = MutableLiveData("Name (A-Z)")

    val filteredAndSortedAnimals = MediatorLiveData<List<Animal>>()

    init {
        filteredAndSortedAnimals.addSource(allAnimals) { applyFiltersAndSort() }
        filteredAndSortedAnimals.addSource(searchQuery) { applyFiltersAndSort() }
        filteredAndSortedAnimals.addSource(filterType) { applyFiltersAndSort() }
        filteredAndSortedAnimals.addSource(sortOrder) { applyFiltersAndSort() }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFilterType(type: String) {
        filterType.value = type
    }

    fun setSortOrder(order: String) {
        sortOrder.value = order
    }

    private fun applyFiltersAndSort() {
        val animals = allAnimals.value ?: return
        val query = searchQuery.value ?: ""
        val filter = filterType.value ?: "All"
        val sort = sortOrder.value ?: "Name (A-Z)"

        var result = animals

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        if (filter != "All") {
            result = result.filter {
                it.animalType == filter
            }
        }

        result = when (sort) {
            "Name (A-Z)" -> result.sortedBy { it.name }
            "Name (Z-A)" -> result.sortedByDescending { it.name }
            "Weight (High-Low)" -> result.sortedByDescending { it.currentWeight }
            "Weight (Low-High)" -> result.sortedBy { it.currentWeight }
            else -> result
        }

        filteredAndSortedAnimals.value = result
    }
}