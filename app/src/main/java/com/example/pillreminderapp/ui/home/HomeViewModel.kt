package com.example.pillreminderapp.ui.home

import androidx.lifecycle.*
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.Reminder
import com.example.pillreminderapp.db.entities.ReminderDisplayItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _reminderDisplayItems = MutableLiveData<List<ReminderDisplayItem>>()
    val reminderDisplayItems: LiveData<List<ReminderDisplayItem>> = _reminderDisplayItems

    fun loadReminders() {
        viewModelScope.launch {
            val reminderDao = db.reminderDao()
            val medicineDao = db.medicineDao()
            val intakeDao = db.intakeDao()

            val reminders: List<Reminder> = reminderDao.getAllReminders() // Нужно добавить метод getAllReminders()
            val displayItems = mutableListOf<ReminderDisplayItem>()

            // Форматеры для даты и времени
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            for (reminder in reminders) {
                val medicine = medicineDao.getById(reminder.medicineId)
                if (medicine == null) continue  // если нет лекарства — пропускаем

                val intakes = intakeDao.getByReminder(reminder.id)
                val intake = intakes.firstOrNull() ?: continue

                val intakeTimeStr = timeFormat.format(Date(intake.intakeTime))
                val startDateStr = dateFormat.format(Date(reminder.startDate))

                val dosageText = "${intake.dose}"

                val item = ReminderDisplayItem(
                    reminderId = reminder.id,
                    time = intakeTimeStr,
                    date = startDateStr,
                    medicineName = medicine.name,
                    dosage = dosageText
                )
                displayItems.add(item)
            }


            _reminderDisplayItems.postValue(displayItems)
        }
    }
}
