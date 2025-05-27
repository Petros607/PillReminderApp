package com.example.pillreminderapp.repository

import com.example.pillreminderapp.db.dao.MedicineDao
import com.example.pillreminderapp.db.entities.Medicine

class MedicineRepository(private val medicineDao: MedicineDao) {

    suspend fun searchMedicines(query: String): List<Medicine> {
        return medicineDao.searchMedicines(query)
    }

    suspend fun getAllMedicines(): List<Medicine> {
        return medicineDao.getAll()
    }

    suspend fun getMedicineById(id: Long): Medicine? {
        return medicineDao.getById(id)
    }
}
