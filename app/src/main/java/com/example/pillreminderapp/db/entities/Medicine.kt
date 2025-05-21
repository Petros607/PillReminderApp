package com.example.pillreminderapp.db.entities

import androidx.room.*

enum class DosageForm {
    TABLET,
    CAPSULE,
    PROCEDURE,
    SOLUTION, //раствор
    DROPLET, //капли
    LIQUID,
    OINTMENT, //мазь
    SPRAY,
    OTHER
}

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val substance: String,
    val manufacturer: String,

    @ColumnInfo(name = "dosage_form")
    val dosageForm: DosageForm
)