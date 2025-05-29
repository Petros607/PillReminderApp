package com.example.pillreminderapp.ui.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.Reminder
import com.example.pillreminderapp.db.entities.Medicine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditReminderDialog : DialogFragment() {

    private var reminder: Reminder? = null
    private var medicine: Medicine? = null

    private lateinit var tv_title_edit_reminder: TextView
    private lateinit var spinner: Spinner

    private val timeOptions = listOf(
        "В момент приема" to 0L,
        "За 10 минут" to 10L,
        "За 15 минут" to 15L,
        "За 30 минут" to 30L,
        "За 1 час" to 60L
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_edit_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title_edit_reminder = view.findViewById(R.id.tv_title_edit_reminder)

        // 1. Сначала инициализируем Spinner с пустым адаптером
        spinner = view.findViewById(R.id.spinner_reminder_time)
        setupEmptySpinner()

        // 2. Загружаем данные напоминания
        val reminderId = arguments?.getLong(ARG_REMINDER_ID) ?: run {
            dismiss()
            return
        }
        loadReminder(reminderId)
        loadMedicine()
    }

    private fun setupEmptySpinner() {
        // Временный пустой адаптер, чтобы избежать дефолтного выбора
        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun loadReminder(reminderId: Long) {
        lifecycleScope.launch {
            // 3. Загружаем данные из базы
            reminder = AppDatabase.getInstance(requireContext())
                .reminderDao()
                .getById(reminderId)

            // 4. После загрузки обновляем UI
            reminder?.let { updateUI(it) }
        }
    }

    private fun loadMedicine() {
        lifecycleScope.launch {
            val medicineDao = AppDatabase.getInstance(requireContext()).medicineDao()
            medicine = medicineDao.getById(reminder!!.medicineId)
        }
    }

    private fun updateUI(reminder: Reminder) {
        // Заполнение основных полей
        view?.findViewById<EditText>(R.id.edit_description)?.setText(reminder.description ?: "")
        view?.findViewById<TextView>(R.id.text_start_date)?.text = convertMillisToDate(reminder.intakeDate)
        view?.findViewById<TextView>(R.id.text_dose_final)?.setText(reminder.dose.toString())

//        tv_title_edit_reminder.text = medicine!!.name

        // Время приема
        val calendar = Calendar.getInstance().apply { timeInMillis = reminder.intakeTime }
        view?.findViewById<TextView>(R.id.text_time)?.text =
            String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        // Название лекарства
        lifecycleScope.launch {
            val medicineName = getMedicineNameById(reminder.medicineId)
            view?.findViewById<AutoCompleteTextView>(R.id.auto_medicine_search)?.setText(medicineName)
        }

        // Установка Spinner
        // 5. Теперь заполняем Spinner реальными данными
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeOptions.map { it.first }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter

        // 6. Вычисляем нужную позицию для Spinner
        val offsetMillis = reminder.intakeTime - (reminder.notificationTime ?: reminder.intakeTime)
        val offsetMinutes = offsetMillis / 60000L

        // Находим позицию, соответствующую offsetMinutes
        val selectedPosition = timeOptions.indexOfFirst { it.second == offsetMinutes }
            .takeIf { it != -1 } ?: 0 // Дефолтно 0 если не нашли

        // 7. Устанавливаем выбранную позицию
        spinner.setSelection(selectedPosition, false) // false - без анимации
    }

    private fun convertMillisToDate(millis: Long): String {
        val date = Date(millis)
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(date)
    }

    private suspend fun getMedicineNameById(medicineId: Long): String {
        val medicineDao = AppDatabase.getInstance(requireContext()).medicineDao()
        return medicineDao.getById(medicineId)?.name ?: "Неизвестно"
    }

    companion object {
        private const val ARG_REMINDER_ID = "arg_reminder_id"

        fun newInstance(reminderId: Long): EditReminderDialog {
            val fragment = EditReminderDialog()
            val args = Bundle()
            args.putLong(ARG_REMINDER_ID, reminderId)
            fragment.arguments = args
            return fragment
        }
    }
}


