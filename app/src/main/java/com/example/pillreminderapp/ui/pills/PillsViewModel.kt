package com.example.pillreminderapp.ui.pills

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PillsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is pills Fragment"
    }
    val text: LiveData<String> = _text
}