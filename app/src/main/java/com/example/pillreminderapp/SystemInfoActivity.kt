package com.example.pillreminderapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class SystemInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_info)

        // Крестик - возврат к сведениям о разработчиках
        findViewById<ImageView>(R.id.close_button).setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Установка текста
        val systemInfoText = """
            Система представляет собой мобильное приложение для создания и управления напоминаниями о приёме лекарственных средств.
            
            Основные функции:
            • Добавление, редактирование и удаление напоминаний
            • Настройка времени приёма и уведомлений
            • Ведение списка используемых лекарств
            • Отображение актуальных напоминаний
            • Справочная информация о системе
            
            Приложение обеспечивает простой интерфейс для повышения дисциплины в приёме препаратов.
        """.trimIndent()


        findViewById<TextView>(R.id.system_info_text).text = systemInfoText
    }
}