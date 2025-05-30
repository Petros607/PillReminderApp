package com.example.pillreminderapp.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pillreminderapp.R
import android.util.Log


class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dosageFormText = intent.getStringExtra("dosageFormText") ?: "Лекарства"
        val dose = intent.getFloatExtra("dose", 0f)
        val time = intent.getStringExtra("time") ?: ""
        val medicineName = intent.getStringExtra("medicineName") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("reminder_channel", "Напоминания", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        Log.d("ReminderReceiver", "Received intent with extras:")
        intent.extras?.keySet()?.forEach { key ->
            Log.d("ReminderReceiver", "$key = ${intent.extras?.get(key)}")
        }


        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_menu_pill) // добавь такой ресурс
            .setContentTitle("Пора принять лекарство")
            .setContentText("Примите в $time - $dose $dosageFormText - $medicineName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

