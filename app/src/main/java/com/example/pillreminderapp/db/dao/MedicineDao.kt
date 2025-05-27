package com.example.pillreminderapp.db.dao

import androidx.room.*
import com.example.pillreminderapp.db.entities.Medicine

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Query("SELECT * FROM medicines")
    suspend fun getAll(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE name LIKE '%' || :query || '%'")
    suspend fun searchMedicines(query: String): List<Medicine>

    @Update
    suspend fun update(medicine: Medicine)

    @Delete
    suspend fun delete(medicine: Medicine)
}