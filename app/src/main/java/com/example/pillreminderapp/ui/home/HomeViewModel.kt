package com.example.pillreminderapp.ui.home

import androidx.lifecycle.*
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Reminder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _filteredReminders = MutableLiveData<List<Reminder>>()
    val filteredReminders: LiveData<List<Reminder>> = _filteredReminders

    private var allReminders: List<Reminder> = emptyList()

    private val _medicineInfo = MutableLiveData<Map<Long, Pair<String, DosageForm>>>()
    val medicineInfo: LiveData<Map<Long, Pair<String, DosageForm>>> = _medicineInfo

    fun loadReminders() {
        viewModelScope.launch {
            val reminderDao = db.reminderDao()
            val medicineDao = db.medicineDao()

            allReminders = reminderDao.getAllReminders()
            _filteredReminders.postValue(allReminders)

            val medicineMap = allReminders.mapNotNull { reminder ->
                val medicine = medicineDao.getById(reminder.medicineId)
                medicine?.let {
                    reminder.medicineId to (it.name to it.dosageForm)
                }
            }.toMap()

            _medicineInfo.postValue(medicineMap)
        }
    }

    fun applyFilter(filterType: HomeFragment.FilterType) {
        viewModelScope.launch {
            val filtered = when (filterType) {
                HomeFragment.FilterType.TODAY -> filterForToday()
                HomeFragment.FilterType.TOMORROW -> filterForTomorrow()
                HomeFragment.FilterType.WEEK -> filterForWeek()
                HomeFragment.FilterType.ALL -> allReminders
            }
            _filteredReminders.postValue(filtered)
        }
    }

    private suspend fun filterForToday(): List<Reminder> {
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val todayEnd = todayStart + TimeUnit.DAYS.toMillis(1)
        return db.reminderDao().getBetweenDates(todayStart, todayEnd)
    }

    private suspend fun filterForTomorrow(): List<Reminder> {
        val tomorrowStart = getStartOfDay(System.currentTimeMillis()) + TimeUnit.DAYS.toMillis(1)
        val tomorrowEnd = tomorrowStart + TimeUnit.DAYS.toMillis(1)
        return db.reminderDao().getBetweenDates(tomorrowStart, tomorrowEnd)
    }

    private suspend fun filterForWeek(): List<Reminder> {
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val weekEnd = todayStart + TimeUnit.DAYS.toMillis(7)
        return db.reminderDao().getBetweenDates(todayStart, weekEnd)
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            val timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

