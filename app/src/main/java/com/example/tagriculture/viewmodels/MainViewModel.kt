package com.example.tagriculture.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _nfcTagId = MutableLiveData<String?>()

    val nfcTagId: LiveData<String?> = _nfcTagId

    fun onNfcTagScanned(tagId: String) {
        _nfcTagId.value = tagId
    }

    fun onNfcTagProcessed() {
        _nfcTagId.value = null
    }
}