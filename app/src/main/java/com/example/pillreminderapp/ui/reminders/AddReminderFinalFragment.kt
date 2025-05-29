package com.example.pillreminderapp.ui.reminders

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.ViewModelProvider
import com.example.pillreminderapp.R
import java.time.LocalDate
import androidx.lifecycle.lifecycleScope
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.Reminder
import com.example.pillreminderapp.ui.home.HomeViewModel
import com.example.pillreminderapp.ui.home.HomeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

        btnSave.setOnClickListener {
            lifecycleScope.launch {
                val dateStrings = arguments?.getStringArrayList("reminderDates") ?: arrayListOf()
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val parsedDates = dateStrings.map { LocalDate.parse(it, formatter) }

                saveReminders(
                    medicineId = arguments?.getLong("medicineId") ?: 0L,
                    reminderDates = parsedDates,
                    description = arguments?.getString("description") ?: "",
                    view = view
                )

                val homeViewModel = ViewModelProvider(requireActivity(), HomeViewModelFactory(AppDatabase.getInstance(requireContext())))
                    .get(HomeViewModel::class.java)

                homeViewModel.loadReminders()
            }
        }

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


    private fun collectDoseFromUI(view: View): Pair<String, Float>? {
        val doseItem = view.findViewById<View>(R.id.include_dose_time)
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



    private fun collectNotificationOffset(view: View): Long {
        val reminderItem = view.findViewById<View>(R.id.include_reminder_time)
        val spinner = reminderItem.findViewById<Spinner>(R.id.spinner_reminder_time)
        val selectedOption = spinner.selectedItem as String

        val minutesOffset = when (selectedOption) {
            "В момент приема" -> 0
            "За 10 минут" -> 10
            "За 15 минут" -> 15
            "За 30 минут" -> 30
            "За 1 час" -> 60
            else -> 0
        }

        return minutesOffset * 60_000L
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
                    if (doseValue != null && doseValue <= 10f && doseValue > 0f) {
                        doseTextView.text = input
                        doseTextView.setTextColor(resources.getColor(R.color.black, null))
                    } else {
                        Toast.makeText(requireContext(), "Доза должна быть числом от 0.1 до 10.0", Toast.LENGTH_SHORT).show()
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
        val options = listOf("В момент приема", "За 10 минут", "За 15 минут", "За 30 минут", "За 1 час")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(0)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun saveReminders(
        medicineId: Long,
        reminderDates: List<LocalDate>,
        description: String,
        view: View
    ) {
        val reminderDao = AppDatabase.getInstance(requireContext()).reminderDao()

        val dosePair = collectDoseFromUI(view)
        if (dosePair == null) {
            Toast.makeText(requireContext(), "Укажите время и дозу", Toast.LENGTH_SHORT).show()
            return
        }

        val notificationOffset = collectNotificationOffset(view)
        val timeString = dosePair.first

        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val localTime = LocalTime.parse(timeString, formatter)
        val intakeTimeMillis = localTime.toSecondOfDay() * 1000L

        for (date in reminderDates) {
            val localDateTime = date.atStartOfDay()
            val zoneId = ZoneId.systemDefault()
            val instant = localDateTime.atZone(zoneId).toInstant()
            val timestampMillis = instant.toEpochMilli()

            val reminder = Reminder(
                medicineId = medicineId,
                intakeDate = timestampMillis,
                intakeTime = intakeTimeMillis,
                description = description,
                dose = dosePair.second,
                notificationTime = notificationOffset
            )

            withContext(Dispatchers.IO) {
                reminderDao.insert(reminder)
            }
        }

        Toast.makeText(requireContext(), "Напоминание сохранено", Toast.LENGTH_SHORT).show()
        dialog?.dismiss()
    }

    companion object {
        fun newInstance(
            selectedMedicineId: Long,
            description: String,
            reminderDates: ArrayList<LocalDate>
        ): AddReminderFinalFragment {
            val fragment = AddReminderFinalFragment()
            val args = Bundle().apply {
                putLong("medicineId", selectedMedicineId)
                putString("description", description)
                putStringArrayList("reminderDates", ArrayList(reminderDates.map { it.toString() }))
            }
            fragment.arguments = args
            return fragment
        }
    }

}
