package com.example.pillreminderapp.db

import android.content.Context
import androidx.room.*
import com.example.pillreminderapp.db.converters.*
import com.example.pillreminderapp.db.dao.*
import com.example.pillreminderapp.db.entities.*

@Database(
    entities = [Medicine::class, Reminder::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(DosageFormConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_reminder_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
