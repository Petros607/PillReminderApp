package com.example.pillreminderapp.db.dao

import androidx.room.*
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Reminder

@Dao
interface ReminderDao {
    // Вставка одного напоминания (возвращает ID вставленной записи)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    // Вставка списка напоминаний
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<Reminder>)

    // Получение всех напоминаний
    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<Reminder>

    // Получение напоминаний по ID лекарства
    @Query("SELECT * FROM reminders WHERE medicine_id = :medicineId")
    suspend fun getByMedicine(medicineId: Long): List<Reminder>

    // Получение напоминания по его ID
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getById(reminderId: Long): Reminder?

    // Обновление напоминания
    @Update
    suspend fun update(reminder: Reminder)

    // Удаление напоминания
    @Delete
    suspend fun delete(reminder: Reminder)

    // Удаление напоминания по ID
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteById(reminderId: Long)

    // Удаление всех напоминаний для определенного лекарства
    @Query("DELETE FROM reminders WHERE medicine_id = :medicineId")
    suspend fun deleteByMedicine(medicineId: Long)

    // Удаление всех напоминаний
    @Query("DELETE FROM reminders")
    suspend fun deleteAll()

    // Дополнительные полезные запросы:
    /**
     * Получает форму дозировки лекарства по ID лекарства
     */
    @Query("SELECT dosage_form FROM medicines WHERE id = :medicineId")
    suspend fun getDosageFormByMedicineId(medicineId: Long): DosageForm

    // Получение напоминаний на определенную дату
    @Query("SELECT * FROM reminders WHERE intake_date = :date")
    suspend fun getByDate(date: Long): List<Reminder>

    // Получение напоминаний в временном диапазоне
    @Query("SELECT * FROM reminders WHERE intake_date BETWEEN :startDate AND :endDate")
    suspend fun getBetweenDates(startDate: Long, endDate: Long): List<Reminder>
}