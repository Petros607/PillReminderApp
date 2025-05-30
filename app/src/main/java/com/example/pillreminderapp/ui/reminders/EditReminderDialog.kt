package com.example.pillreminderapp.ui.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.pillreminderapp.R
import com.example.pillreminderapp.databinding.DialogEditReminderBinding
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.Reminder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EditReminderDialog : DialogFragment() {
    interface OnReminderEditedListener {
        fun onReminderEdited(updatedReminder: Reminder)
    }
    private var listener: OnReminderEditedListener? = null

    fun setOnReminderEditedListener(listener: OnReminderEditedListener) {
        this.listener = listener
    }

    private val timeOptions = listOf(
        "В момент приема" to 0L,
        "За 10 минут" to 10L,
        "За 15 минут" to 15L,
        "За 30 минут" to 30L,
        "За 1 час" to 60L
    )

    private lateinit var binding: DialogEditReminderBinding
    private var reminderId: Long = -1
    private var startDate: Calendar? = null

    private val dateFormat = SimpleDateFormat("dd.MM.YYYY", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем reminderId из аргументов
        arguments?.let {
            reminderId = it.getLong(ARG_REMINDER_ID)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditReminderBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)

        loadReminderData()

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                saveReminderChanges()
                delay(600L)
                dismiss()
            }
        }

        return dialog
    }

    private suspend fun saveReminderChanges() {
        val db = AppDatabase.getInstance(requireContext())
        val reminder = db.reminderDao().getById(reminderId) ?: return

        val updatedReminder = reminder.copy(
            description = binding.editDescription.text.toString(),
            dose = binding.includeDoseTime.textDoseFinal.text.toString().toFloat(),
            intakeTime = parseTime(binding.includeDoseTime.textTime.text.toString()),
            intakeDate = startDate?.timeInMillis ?: reminder.intakeDate,
            notificationTime = getNotificationTimeFromSpinner()
        )

        listener?.onReminderEdited(updatedReminder)
    }

    private fun parseTime(timeString: String): Long {
        val parts = timeString.split(":")
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        return (hours * 3600 + minutes * 60) * 1000
    }

    private fun getNotificationTimeFromSpinner(): Long {
        val spinner = binding.includeReminderTime.spinnerReminderTime
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

    private fun loadReminderData() {
        val db = AppDatabase.getInstance(requireContext())
        val reminderDao = db.reminderDao()
        val medicineDao = db.medicineDao()

        lifecycleScope.launch {
            val reminder = reminderDao.getById(reminderId) ?: return@launch
            val medicine = medicineDao.getById(reminder.medicineId)

            with(binding) {
                val medicineName = medicine?.name ?: "Без названия"
                val medicineManufacturer = medicine?.manufacturer.toString()
                tvNameEditReminder.setText("${medicineName} (${medicineManufacturer})" ?: "")

                editDescription.setText(reminder.description ?: "")

                val reminderTime = Date(reminder.intakeTime)
                val reminderDate = Date(reminder.intakeDate)

                var formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC") // ⬅️ важно!
                val formattedTime = formatter.format(reminderTime)
                includeDoseTime.textTime.setText(formattedTime)
                includeDoseTime.textTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                includeDoseTime.textTime.setOnClickListener {
                    val timePicker = android.app.TimePickerDialog(
                        requireContext(),
                        R.style.CustomTimePickerDialog,
                        { _, hourOfDay, minute ->
                            val timeString = String.format("%02d:%02d", hourOfDay, minute)
                            includeDoseTime.textTime.text = timeString
                            includeDoseTime.textTime.setTextColor(resources.getColor(R.color.black, null))
                        },
                        12, 0, true
                    )
                    timePicker.show()
                }

                formatter = SimpleDateFormat("dd.MM.YYYY", Locale.getDefault())
                val formattedDate = formatter.format(reminderDate)
                textStartDate.setText(formattedDate)
                textStartDate.setOnClickListener {
                    showDatePicker { calendar ->
                        startDate = calendar
                        textStartDate.text = dateFormat.format(calendar.time)
                    }
                }

                includeDoseTime.textDoseFinal.setText(reminder.dose.toString() ?: "")
                includeDoseTime.textDoseFinal.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                includeDoseTime.textDoseFinal.setOnClickListener {
                    showDoseInputDialog(includeDoseTime.textDoseFinal)
                }

                includeDoseTime.textDoseFinal.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        showDoseInputDialog(includeDoseTime.textDoseFinal)
                    }
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    timeOptions.map { it.first }
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                includeReminderTime.spinnerReminderTime.adapter = adapter

                val offsetMinutes = (reminder.intakeTime - (reminder.notificationTime ?: reminder.intakeTime)) / 60000L
                val selectedPosition = timeOptions.indexOfFirst { it.second == (720 - offsetMinutes) }
                    .takeIf { it != -1 } ?: 0
                includeReminderTime.spinnerReminderTime.setSelection(selectedPosition, false)
            }
        }
    }

    companion object {
        private const val ARG_REMINDER_ID = "reminder_id"

        fun newInstance(reminderId: Long): EditReminderDialog {
            val fragment = EditReminderDialog()
            val args = Bundle()
            args.putLong(ARG_REMINDER_ID, reminderId)
            fragment.arguments = args
            return fragment
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val today = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            ContextThemeWrapper(requireContext(), R.style.CustomDatePickerDialog),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(selectedDate)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showDoseInputDialog(textView: TextView) {
        val inputEditText = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(if (textView.text.toString() == "Доза") "" else textView.text.toString())
            setSelection(text.length)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Введите дозу")
            .setView(inputEditText)
            .setPositiveButton("OK") { dialog, _ ->
                val input = inputEditText.text.toString()
                val doseValue = input.toFloatOrNull()
                if (doseValue != null && doseValue <= 10f && doseValue > 0f) {
                    textView.text = input
                    textView.setTextColor(resources.getColor(R.color.black, null))
                } else {
                    Toast.makeText(requireContext(),
                        "Доза должна быть числом от 0.1 до 10.0",
                        Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
