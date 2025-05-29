package com.example.pillreminderapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.Medicine
import com.example.pillreminderapp.db.entities.Reminder
import kotlinx.coroutines.launch

class AddReminderStartDialogViewModel(application: Application) : AndroidViewModel(application) {

    private val medicineDao = AppDatabase.getInstance(application).medicineDao()

    private val _searchResults = MutableLiveData<List<Medicine>>()
    val searchResults: LiveData<List<Medicine>> get() = _searchResults

    fun searchMedicines(query: String) {
        viewModelScope.launch {
            _searchResults.postValue(medicineDao.searchMedicines(query))
        }
    }
}
