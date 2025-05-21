package com.example.pillreminderapp.db.entities

import androidx.room.*

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = Intake::class,
            parentColumns = ["id"],
            childColumns = ["intake_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["intake_id"])]
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "intake_id")
    val intakeId: Long,
    // Время срабатывания уведомления (может быть рассчитано как смещение от времени приёма или абсолютное время)
    @ColumnInfo(name = "notification_time")
    val notificationTime: Long
)