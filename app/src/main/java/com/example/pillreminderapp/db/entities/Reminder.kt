package com.example.pillreminderapp.db.entities

import android.content.Context
import androidx.room.*
import com.example.pillreminderapp.R

enum class PeriodType {
    DAILY,
    EVERY_OTHER_DAY,
    EVERY_TWO_DAYS,
    EVERY_THREE_DAYS,
    WEEKDAYS,
    CUSTOM;

    fun getLocalizedName(context: Context): String {
        return when (this) {
            DAILY -> context.getString(R.string.period_daily)
            EVERY_OTHER_DAY -> context.getString(R.string.period_every_other_day)
            EVERY_TWO_DAYS -> context.getString(R.string.period_every_two_days)
            EVERY_THREE_DAYS -> context.getString(R.string.period_every_three_days)
            WEEKDAYS -> context.getString(R.string.period_weekdays)
            CUSTOM -> context.getString(R.string.period_custom)
        }
    }
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
