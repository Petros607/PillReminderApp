package com.example.pillreminderapp.db.converters

import androidx.room.TypeConverter
import com.example.pillreminderapp.db.entities.DosageForm

class DosageFormConverter {
    @TypeConverter
    fun fromDosageForm(form: DosageForm): String = form.name

    @TypeConverter
    fun toDosageForm(value: String): DosageForm = DosageForm.valueOf(value)
}