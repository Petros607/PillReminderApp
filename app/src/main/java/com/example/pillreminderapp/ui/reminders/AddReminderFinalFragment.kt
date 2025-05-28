package com.example.pillreminderapp.ui.reminders

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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

    private lateinit var btnSave: Button

    private var currentFormName: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_reminder_final, null)

        // Теперь мы ищем элементы в `view`, а не через `requireView()`
        btnSave = view.findViewById(R.id.btn_save)

        setupSingleReminderSpinner(view)
        addDoseItem(view)

        printAllArguments()

        val medicineId = arguments?.getLong("medicineId") ?: 0L
        if (medicineId != 0L) {
            loadMedicineAndDisplayForm(medicineId, view)
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



    private fun loadMedicineAndDisplayForm(medicineId: Long, view: View) {
        lifecycleScope.launch {
            val medicineDao = AppDatabase.getInstance(requireContext()).medicineDao()
            val medicine = withContext(Dispatchers.IO) {
                medicineDao.getById(medicineId)
            }

            val formTextView = view.findViewById<TextView>(R.id.text_form_final)

            medicine?.let {
                val localizedFormName = it.dosageForm.getLocalizedName(requireContext())
                currentFormName = localizedFormName
            } ?: run {
                currentFormName = getString(R.string.dosage_other)
            }

            // Обновляем текст после загрузки формы
            formTextView.text = currentFormName
        }
    }


    private fun collectDoseFromUI(): Pair<String, Float>? {
        val doseItem = requireView().findViewById<View>(R.id.include_dose_time)
        val timeText = doseItem.findViewById<TextView>(R.id.text_time)
        val doseText = doseItem.findViewById<TextView>(R.id.text_dose_final)

        val time = timeText.text.toString()
        val doseStr = doseText.text.toString()

        return if (time.isNotBlank() && doseStr.isNotBlank() && doseStr != "Доза") {
            val doseValue = doseStr.toFloatOrNull()
            if (doseValue != null) {
                time to doseValue
            } else null
        } else {
            null
        }
    }


    private fun collectNotificationOffset(): Int? {
        val reminderItem = requireView().findViewById<View>(R.id.include_reminder_time)
        val spinner = reminderItem.findViewById<Spinner>(R.id.spinner_reminder_time)
        val selectedOption = spinner.selectedItem as String

        return when (selectedOption) {
            "В момент приема" -> 0
            "За 10 минут" -> 10
            "За 15 минут" -> 15
            "За 30 минут" -> 30
            "За 1 час" -> 60
            "Нет" -> null
            else -> null
        }
    }

    @SuppressLint("InflateParams")
    private fun addDoseItem(view: View) {
        val timeText = view.findViewById<TextView>(R.id.text_time)
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

        val doseItemFormText = view.findViewById<TextView>(R.id.text_form_final)
        doseItemFormText.text = currentFormName

        val doseTextView = view.findViewById<TextView>(R.id.text_dose_final)
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
    }


    private fun setupSingleReminderSpinner(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.spinner_reminder_time)
        val options = listOf("В момент приема", "За 10 минут", "За 15 минут", "За 30 минут", "За 1 час", "Нет")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }


//
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun saveReminder(
        medicineId: Long,
        periodType: PeriodType,
        startDate: LocalDate,
        endDate: LocalDate,
        description: String,
        doses: List<Pair<String, Float>>, // время и доза из UI
        notificationOffsets: List<Int> // в минутах: 0, 10, 15 и т.д.
    ) {
        val reminderDao = AppDatabase.getInstance(requireContext()).reminderDao()
        val intakeDao = AppDatabase.getInstance(requireContext()).intakeDao()
        val notificationDao = AppDatabase.getInstance(requireContext()).notificationDao()


        val reminder = Reminder(
            medicineId = medicineId,
            periodType = periodType,
            startDate = startDate.toEpochDay() * 24*60*60*1000L, // перевод в millis
            endDate = endDate.toEpochDay() * 24*60*60*1000L,
            description = description,

        )
        val reminderId = reminderDao.insert(reminder)


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

    companion object {
        fun newInstance(
            selectedMedicineId: Long,
            periodType: PeriodType,
            description: String,
            reminderDates: ArrayList<LocalDate>
        ): AddReminderFinalFragment {
            val fragment = AddReminderFinalFragment()
            val args = Bundle().apply {
                putLong("medicineId", selectedMedicineId)
                putSerializable("periodType", periodType)
                putString("description", description)
                putStringArrayList("reminderDates", ArrayList(reminderDates.map { it.toString() }))
            }
            fragment.arguments = args
            return fragment
        }
    }

}
