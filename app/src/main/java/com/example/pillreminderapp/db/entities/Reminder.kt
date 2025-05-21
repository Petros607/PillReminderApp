package com.example.pillreminderapp.db.entities

import androidx.room.*

enum class PeriodType {
    DAILY,
    EVERY_OTHER_DAY,
    EVERY_TWO_DAYS,
    EVERY_THREE_DAYS,
    WEEKDAYS,
    CUSTOM
}

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicine_id"])]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "medicine_id")
    val medicineId: Long,

    @ColumnInfo(name = "period_type")
    val periodType: PeriodType,

    @ColumnInfo(name = "custom_interval")
    val customInterval: Int? = null,

    @ColumnInfo(name = "selected_weekdays")
    val selectedWeekdays: String? = null,

    val description: String? = null,

    @ColumnInfo(name = "start_date")
    val startDate: Long,

    @ColumnInfo(name = "end_date")
    val endDate: Long
)
