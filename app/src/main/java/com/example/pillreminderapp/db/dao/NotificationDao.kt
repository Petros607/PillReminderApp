package com.example.pillreminderapp.db.dao

import androidx.room.*
import com.example.pillreminderapp.db.entities.Notification

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification): Long

    @Query("SELECT * FROM notifications WHERE intake_id = :intakeId")
    suspend fun getByIntakeId(intakeId: Long): List<Notification>

    @Query("SELECT * FROM notifications")
    suspend fun getAll(): List<Notification>

    @Delete
    suspend fun delete(notification: Notification)
}