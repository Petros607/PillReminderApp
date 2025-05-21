package com.example.pillreminderapp.db.dao

import androidx.room.*
import com.example.pillreminderapp.db.entities.Intake

@Dao
interface IntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(intake: Intake): Long

    @Query("SELECT * FROM intakes WHERE reminder_id = :reminderId")
    suspend fun getByReminder(reminderId: Long): List<Intake>

    @Delete
    suspend fun delete(intake: Intake)
}