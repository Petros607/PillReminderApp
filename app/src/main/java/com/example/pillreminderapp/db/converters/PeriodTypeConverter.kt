package com.example.pillreminderapp.db.converters

import androidx.room.TypeConverter
import com.example.pillreminderapp.db.entities.PeriodType

class PeriodTypeConverter {
    @TypeConverter
    fun fromPeriodType(type: PeriodType): String = type.name

    @TypeConverter
    fun toPeriodType(value: String): PeriodType = PeriodType.valueOf(value)
}