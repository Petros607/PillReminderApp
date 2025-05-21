package com.example.pillreminderapp.db.dao

import androidx.room.*
import com.example.pillreminderapp.db.entities.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Query("SELECT * FROM reminders WHERE medicine_id = :medicineId")
    suspend fun getByMedicine(medicineId: Long): List<Reminder>

    @Delete
    suspend fun delete(reminder: Reminder)
}