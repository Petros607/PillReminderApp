package com.example.pillreminderapp.db

import android.content.Context
import androidx.room.*
import com.example.pillreminderapp.db.converters.*
import com.example.pillreminderapp.db.dao.*
import com.example.pillreminderapp.db.entities.*

@Database(
    entities = [Medicine::class, Reminder::class, Intake::class, Notification::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DosageFormConverter::class, PeriodTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun reminderDao(): ReminderDao
    abstract fun intakeDao(): IntakeDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_reminder_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
