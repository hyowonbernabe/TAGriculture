package com.example.tagriculture.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.AppDatabase
import com.example.tagriculture.data.database.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReassignTagViewModel(application: Application) : AndroidViewModel(application) {
    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val tagDao = AppDatabase.getDatabase(application).tagDao()

    val allAnimals: LiveData<List<Animal>> = animalDao.getAllAnimals()

    fun reassignTag(nfcSerialNumber: String, animalId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            tagDao.unassignTagFromAnimal(animalId)
            tagDao.insertTag(Tag(nfcSerialNumber, animalId))
        }
    }
}