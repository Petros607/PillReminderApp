package com.example.pillreminderapp.db.entities

data class ReminderDisplayItem(
    val reminderId: Long,
    val time: String,         // например, "09:00"
    val date: String,         // например, "27.05.2025"
    val medicineName: String,
    val dosage: String
)

