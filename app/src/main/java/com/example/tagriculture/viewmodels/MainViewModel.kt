package com.example.tagriculture.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tagriculture.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class ScanResult {
    data class KnownAnimal(val animalId: Long) : ScanResult()
    data class NewTag(val tagId: String) : ScanResult()
    object UnassignedTag : ScanResult()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val tagDao = AppDatabase.getDatabase(application).tagDao()

    private val _scanResult = MutableLiveData<ScanResult?>()
    val scanResult: LiveData<ScanResult?> = _scanResult

    fun onNfcTagScanned(tagId: String) {
        Log.d("DEBUG_NFC", "3. MainViewModel - Coroutine launched for tag: $tagId")
        viewModelScope.launch(Dispatchers.IO) {
            val existingTag = tagDao.getTagBySerial(tagId)

            val result = if (existingTag != null) {
                val localAnimalId = existingTag.animalId
                if (localAnimalId != null) {
                    ScanResult.KnownAnimal(localAnimalId)
                } else {
                    ScanResult.UnassignedTag
                }
            } else {
                ScanResult.NewTag(tagId)
            }
            Log.d("DEBUG_NFC", "4. MainViewModel - Posting result: $result")
            _scanResult.postValue(result)
        }
    }

    fun onScanResultProcessed() {
        _scanResult.value = null
    }
}