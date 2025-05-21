package com.example.pillreminderapp.db.entities

import androidx.room.*

@Entity(
    tableName = "intakes",
    foreignKeys = [
        ForeignKey(
            entity = Reminder::class,
            parentColumns = ["id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["reminder_id"])]
)
data class Intake(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "reminder_id")
    val reminderId: Long,

    @ColumnInfo(name = "intake_time")
    val intakeTime: Long,

    val dose: Float
)