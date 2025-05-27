package com.example.pillreminderapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DevInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_info)

        // Крестик - возврат в главное меню
        findViewById<ImageView>(R.id.close_button).setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Кнопка перехода к системной информации
        findViewById<Button>(R.id.system_info_button).setOnClickListener {
            startActivity(Intent(this, SystemInfoActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Установка текста
        val devInfo = """
            Самарский университет
            Кафедра программных систем

            Курсовой проект по дисциплине "Программная инженерия"

            Тема проекта: "Мобильное приложение для напоминания о приёме лекарств"

            Разработчики - Обучающиеся группы  6303-020302D
            П.Г. Маркарян
            А.М. Ибрагимов

            Самара 2025
        """.trimIndent()

        findViewById<TextView>(R.id.dev_info_text).text = devInfo
    }
}