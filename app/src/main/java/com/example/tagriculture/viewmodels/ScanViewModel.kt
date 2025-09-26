package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()

    val allAnimals: LiveData<List<Animal>>

    init {
        allAnimals = animalDao.getAllAnimals()
    }
}