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
            
            -При запуске приложения открывается главный экран. В верхней части экрана расположен логотип, ведущий на страницу сведений о разработчиках, а также кнопка добавления нового напоминания. По центру экрана отображается список напоминаний, над которым размещен небольшой фильтр. В нижней части экрана находится навигационное меню, позволяющее переключаться между разделами с лекарствами и напоминаниями.

            -При нажатии на логотип открывается экран со сведениями о разработчиках. В верхней части этого экрана отображается логотип, кнопка для перехода к справочной информации, а также кнопка закрытия, возвращающая пользователя в главное меню.
            
            -При переходе на экран «Сведения о системе» отображаются логотип, основные сведения о приложении и кнопка закрытия экрана.
            
            -Для добавления напоминания пользователь нажимает кнопку «+», после чего открывается стартовое диалоговое окно. Оно содержит поле для ввода и выпадающие списки для выбора необходимых параметров.
            
            -После нажатия кнопки «Далее» открывается следующее диалоговое окно с полями для ввода дат. Затем открывается ещё одно диалоговое окно, содержащее поля для ввода времени и количества лекарства. При сохранении данных диалог закрывается, и информация добавляется в базу данных и отображается в главном меню.
            
            -При выборе конкретного напоминания открывается окно с полной информацией, а также с возможностью изменить, удалить или отметить напоминание как выполненное или пропущенное.
            
            -Если пользователь нажимает кнопку «Изменить», текущее окно закрывается и открывается диалог редактирования напоминания, где можно изменить все параметры. После сохранения изменений отображается обновлённый главный экран.
            
            -Для перехода к списку лекарств пользователь может нажать на кнопку «Медицина» в нижнем навигационном меню. Открывается экран, на котором визуализируется список всех добавленных пользователем лекарств.
            
            -Добавление нового лекарства осуществляется нажатием на кнопку «+», после чего открывается диалоговое окно с полями для ввода названия и других параметров. После сохранения окно закрывается, и список лекарств обновляется.
            
            -При выборе конкретного лекарства открывается окно с полной информацией и кнопками действий. Пользователь может удалить лекарство, при этом также удаляются связанные с ним напоминания, либо изменить данные о лекарстве в соответствующем окне редактирования. После подтверждения изменения данные сохраняются в базе и отображаются в обновленном виде.
        """.trimIndent()


        findViewById<TextView>(R.id.system_info_text).text = systemInfoText
    }
}