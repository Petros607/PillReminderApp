package com.example.pillreminderapp.ui.medicine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MedicineMenuViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is pills Fragment"
    }
    val text: LiveData<String> = _text
}