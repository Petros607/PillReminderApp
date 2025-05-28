package com.example.pillreminderapp.db.entities

import androidx.room.*

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
    val notificationTime: Long
)
