package com.example.pillreminderapp.db.entities

import android.content.Context
import androidx.room.*
import com.example.pillreminderapp.R

enum class PeriodType {
    DAILY,
    EVERY_OTHER_DAY,
    EVERY_TWO_DAYS,
    EVERY_THREE_DAYS;

    fun getLocalizedName(context: Context): String {
        return when (this) {
            DAILY -> context.getString(R.string.period_daily)
            EVERY_OTHER_DAY -> context.getString(R.string.period_every_other_day)
            EVERY_TWO_DAYS -> context.getString(R.string.period_every_two_days)
            EVERY_THREE_DAYS -> context.getString(R.string.period_every_three_days)

        }
    }
}

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = Medicine::class,
        parentColumns = ["id"],
        childColumns = ["medicine_id"],
        onDelete = ForeignKey.CASCADE, // опционально: что делать при удалении лекарства
        onUpdate = ForeignKey.CASCADE // опционально: что делать при обновлении id лекарства
    )]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "medicine_id")
    val medicineId: Long,

    val description: String? = null,

    @ColumnInfo(name = "intake_date")
    val intakeDate: Long,

    @ColumnInfo(name = "intake_time")
    val intakeTime: Long,

    val dose: Float,

    @ColumnInfo(name = "notification_time")
    val notificationTime: Long,

    val mark: Boolean? = null
)
