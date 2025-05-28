package com.example.pillreminderapp.ui.home

import androidx.lifecycle.*
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Reminder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _reminders = MutableLiveData<List<Reminder>>()
    val reminders: LiveData<List<Reminder>> = _reminders

    private val _medicineInfo = MutableLiveData<Map<Long, Pair<String, DosageForm>>>()
    val medicineInfo: LiveData<Map<Long, Pair<String, DosageForm>>> = _medicineInfo

    fun loadReminders() {
        viewModelScope.launch {
            val reminderDao = db.reminderDao()
            val medicineDao = db.medicineDao()

            val remindersList = reminderDao.getAllReminders() // Обязательно реализуй в DAO
            _reminders.postValue(remindersList)

            val medicineMap = remindersList.mapNotNull { reminder ->
                val medicine = medicineDao.getById(reminder.medicineId)
                medicine?.let {
                    reminder.medicineId to (it.name to it.dosageForm)
                }
            }.toMap()

            _medicineInfo.postValue(medicineMap)
        }
    }
}

