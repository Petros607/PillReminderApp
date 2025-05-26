package com.example.pillreminderapp.db.entities

import android.content.Context
import androidx.room.*
import com.example.pillreminderapp.R

enum class DosageForm {
    TABLET,
    CAPSULE,
    PROCEDURE,
    SOLUTION, //раствор
    DROPLET, //капли
    LIQUID,
    OINTMENT, //мазь
    SPRAY,
    OTHER;

    fun getLocalizedName(context: Context): String {
        return when (this) {
            TABLET -> context.getString(R.string.dosage_tablet)
            CAPSULE -> context.getString(R.string.dosage_capsule)
            PROCEDURE -> context.getString(R.string.dosage_procedure)
            SOLUTION -> context.getString(R.string.dosage_solution)
            DROPLET -> context.getString(R.string.dosage_droplet)
            LIQUID -> context.getString(R.string.dosage_liquid)
            OINTMENT -> context.getString(R.string.dosage_ointment)
            SPRAY -> context.getString(R.string.dosage_spray)
            OTHER -> context.getString(R.string.dosage_other)
        }
    }
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