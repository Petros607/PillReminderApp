package com.example.pillreminderapp.ui.reminders

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.entities.PeriodType
import java.time.LocalDate
import androidx.core.view.isEmpty
import androidx.lifecycle.lifecycleScope
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.Reminder
import com.example.pillreminderapp.db.entities.Intake
import com.example.pillreminderapp.db.entities.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddReminderFinalFragment : DialogFragment() {

    private lateinit var doseListLayout: LinearLayout
    private lateinit var reminderListLayout: LinearLayout
    private lateinit var btnAddDose: ImageButton
    private lateinit var btnAddReminder: ImageButton
    private lateinit var btnSave: Button

    private var currentFormName: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_reminder_final, null)

        doseListLayout = view.findViewById(R.id.doseListLayout)
        reminderListLayout = view.findViewById(R.id.reminderListLayout)
        btnAddDose = view.findViewById(R.id.btn_add_dose_time)
        btnAddReminder = view.findViewById(R.id.btn_add_reminder)
        btnSave = view.findViewById(R.id.btn_save)

        printAllArguments()

        setupListeners()

        val medicineId = arguments?.getLong("medicineId") ?: 0L
        if (medicineId != 0L) {
            loadMedicineAndDisplayForm(medicineId)
        }

        return builder.setView(view).create()
    }

    private fun printAllArguments() {
        val args = arguments
        if (args == null || args.isEmpty) {
            println("No arguments")
            return
        }

        for (key in args.keySet()) {
            val value = args.get(key)
            println("Argument key=$key, value=$value")
        }
    }



    private fun loadMedicineAndDisplayForm(medicineId: Long) {
        lifecycleScope.launch {
            val medicineDao = AppDatabase.getInstance(requireContext()).medicineDao()
            val medicine = withContext(Dispatchers.IO) {
                medicineDao.getById(medicineId)
            }
            medicine?.let {
                val localizedFormName = it.dosageForm.getLocalizedName(requireContext())
                currentFormName = localizedFormName
            } ?: run {
                currentFormName = getString(R.string.dosage_other) // или другое значение по умолчанию
            }
        }
    }

    private fun setupListeners() {
        btnAddDose.setOnClickListener {
            addDoseItem()
        }

        btnAddReminder.setOnClickListener {
            addReminderItem()
        }

        btnSave.setOnClickListener {
            if (doseListLayout.isEmpty()) {
                Toast.makeText(requireContext(), "Добавьте хотя бы один приём", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                saveReminder(
                    medicineId = arguments?.getLong("medicineId") ?: 0L,
                    periodType = arguments?.getSerializable("periodType") as PeriodType,
                    startDate = LocalDate.parse(arguments?.getString("startDate")),
                    endDate = LocalDate.parse(arguments?.getString("endDate")),
                    description = arguments?.getString("description") ?: "",
                    selectedDays = arguments?.getStringArrayList("selectedDays"),
                    doses = collectDosesFromUI(), // нужно реализовать сбор доз из UI
                    notificationOffsets = collectNotificationOffsetsFromUI() // тоже нужно реализовать сбор из UI
                )
                dismiss()
            }
            dismiss()
        }
    }

    private fun collectDosesFromUI(): List<Pair<String, Float>> {
        val doses = mutableListOf<Pair<String, Float>>()

        for (i in 0 until doseListLayout.childCount) {
            val doseItem = doseListLayout.getChildAt(i)
            val timeText = doseItem.findViewById<TextView>(R.id.text_time)
            val doseText = doseItem.findViewById<TextView>(R.id.text_dose_final)

            val time = timeText.text.toString()
            val doseStr = doseText.text.toString()

            // Проверяем, что время и доза валидны
            if (time.isNotBlank() && doseStr.isNotBlank() && doseStr != "Доза") {
                val doseValue = doseStr.toFloatOrNull()
                if (doseValue != null) {
                    doses.add(time to doseValue)
                }
            }
        }

        return doses
    }

    private fun collectNotificationOffsetsFromUI(): List<Int> {
        val offsets = mutableListOf<Int>()
        for (i in 0 until reminderListLayout.childCount) {
            val reminderItem = reminderListLayout.getChildAt(i)
            val spinner = reminderItem.findViewById<Spinner>(R.id.spinner_reminder_time)
            val selectedOption = spinner.selectedItem as String

            // Преобразуем выбранный текст в минуты
            val offsetMinutes = when (selectedOption) {
                "В момент приема" -> 0
                "За 10 минут" -> 10
                "За 15 минут" -> 15
                "За 30 минут" -> 30
                "За 1 час" -> 60
                "Нет" -> null
                else -> null
            }

            if (offsetMinutes != null) {
                offsets.add(offsetMinutes)
            }
        }

        return offsets
    }


    @SuppressLint("InflateParams")
    private fun addDoseItem() {
        val doseItem = layoutInflater.inflate(R.layout.item_dose_time, null)

        val timeText = doseItem.findViewById<TextView>(R.id.text_time)
        timeText.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(
                requireContext(),
                R.style.CustomTimePickerDialog,
                { _, hourOfDay, minute ->
                    val timeString = String.format("%02d:%02d", hourOfDay, minute)
                    timeText.text = timeString
                    timeText.setTextColor(resources.getColor(R.color.black, null))
                },
                12, 0, true
            )
            timePicker.show()
        }

        val doseItemFormText = doseItem.findViewById<TextView>(R.id.text_form_final)
        doseItemFormText.text = currentFormName

        val doseTextView = doseItem.findViewById<TextView>(R.id.text_dose_final)
        doseTextView.setOnClickListener {
            val inputEditText = EditText(requireContext()).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(if (doseTextView.text.toString() == "Доза") "" else doseTextView.text.toString())
                setSelection(text.length)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Введите дозу")
                .setView(inputEditText)
                .setPositiveButton("OK") { dialog, _ ->
                    val input = inputEditText.text.toString()
                    val doseValue = input.toFloatOrNull()
                    if (doseValue != null && doseValue <= 10f) {
                        doseTextView.text = input
                        doseTextView.setTextColor(resources.getColor(R.color.black, null))
                    } else {
                        Toast.makeText(requireContext(), "Доза должна быть числом не больше 10", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }


        doseListLayout.addView(doseItem)
    }

    @SuppressLint("InflateParams")
    private fun addReminderItem() {
        val reminderItem = layoutInflater.inflate(R.layout.item_reminder_time, null)

        val spinner = reminderItem.findViewById<Spinner>(R.id.spinner_reminder_time)
        val options = listOf("В момент приема", "За 10 минут", "За 15 минут", "За 30 минут", "За 1 час", "Нет")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        reminderListLayout.addView(reminderItem)
    }

    private suspend fun saveReminder(
        medicineId: Long,
        periodType: PeriodType,
        startDate: LocalDate,
        endDate: LocalDate,
        description: String,
        selectedDays: List<String>?, // для WEEKDAYS
        doses: List<Pair<String, Float>>, // время и доза из UI
        notificationOffsets: List<Int> // в минутах: 0, 10, 15 и т.д.
    ) {
        val reminderDao = AppDatabase.getInstance(requireContext()).reminderDao()
        val intakeDao = AppDatabase.getInstance(requireContext()).intakeDao()
        val notificationDao = AppDatabase.getInstance(requireContext()).notificationDao()

        val selectedWeekdaysStr = selectedDays?.joinToString(",")

        val reminder = Reminder(
            medicineId = medicineId,
            periodType = periodType,
            startDate = startDate.toEpochDay() * 24*60*60*1000L, // перевод в millis
            endDate = endDate.toEpochDay() * 24*60*60*1000L,
            description = description,
            selectedWeekdays = selectedWeekdaysStr
        )
        val reminderId = reminderDao.insert(reminder)

        // Пройтись по датам от startDate до endDate с шагом в зависимости от periodType
        val dateList = generateDatesByPeriod(periodType, startDate, endDate, selectedDays)

        for (date in dateList) {
            for ((timeStr, dose) in doses) {
                val (hour, minute) = timeStr.split(":").map { it.toInt() }
                // вычисляем время intake в millis
                val intakeDateTimeMillis = date.atTime(hour, minute)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val intake = Intake(
                    reminderId = reminderId,
                    intakeTime = intakeDateTimeMillis,
                    dose = dose
                )
                val intakeId = intakeDao.insert(intake)

                // Создаем уведомления для каждого offset из UI
                for (offsetMinutes in notificationOffsets) {
                    val notificationTime = intakeDateTimeMillis - offsetMinutes * 60 * 1000L
                    val notification = Notification(
                        intakeId = intakeId,
                        notificationTime = notificationTime
                    )
                    notificationDao.insert(notification)
                }
            }
        }
    }

    private fun generateDatesByPeriod(
        periodType: PeriodType,
        startDate: LocalDate,
        endDate: LocalDate,
        selectedDays: List<String>? = null
    ): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startDate

        when (periodType) {
            PeriodType.WEEKDAYS -> {
                // Преобразуем selectedDays из ["Mon", "Wed"] в DayOfWeek
                val selectedDayOfWeeks = selectedDays?.mapNotNull {
                    when (it) {
                        "Mon" -> java.time.DayOfWeek.MONDAY
                        "Tue" -> java.time.DayOfWeek.TUESDAY
                        "Wed" -> java.time.DayOfWeek.WEDNESDAY
                        "Thu" -> java.time.DayOfWeek.THURSDAY
                        "Fri" -> java.time.DayOfWeek.FRIDAY
                        "Sat" -> java.time.DayOfWeek.SATURDAY
                        "Sun" -> java.time.DayOfWeek.SUNDAY
                        else -> null
                    }
                } ?: emptyList()

                while (!currentDate.isAfter(endDate)) {
                    if (selectedDayOfWeeks.contains(currentDate.dayOfWeek)) {
                        dates.add(currentDate)
                    }
                    currentDate = currentDate.plusDays(1)
                }
            }

            PeriodType.DAILY -> {
                while (!currentDate.isAfter(endDate)) {
                    dates.add(currentDate)
                    currentDate = currentDate.plusDays(1)
                }
            }

            PeriodType.EVERY_OTHER_DAY -> {
                while (!currentDate.isAfter(endDate)) {
                    dates.add(currentDate)
                    currentDate = currentDate.plusDays(2)
                }
            }

            // Другие типы по аналогии...

            else -> {
                // Можно реализовать по своему усмотрению
            }
        }
        return dates
    }


    companion object {
        fun newInstance(
            selectedMedicineId: Long,
            periodType: PeriodType,
            startDate: LocalDate,
            endDate: LocalDate,
            description: String,
            selectedDays: ArrayList<String>? = null
        ): AddReminderFinalFragment {
            val fragment = AddReminderFinalFragment()
            val args = Bundle()
            args.putLong("medicineId", selectedMedicineId)
            args.putSerializable("periodType", periodType)
            args.putString("startDate", startDate.toString())
            args.putString("endDate", endDate.toString())
            args.putString("description", description)
            if (selectedDays != null) {
                args.putStringArrayList("selectedDays", selectedDays)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
