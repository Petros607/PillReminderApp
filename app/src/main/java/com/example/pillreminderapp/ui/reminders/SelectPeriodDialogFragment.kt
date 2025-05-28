package com.example.pillreminderapp.ui.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.entities.PeriodType
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class SelectPeriodDialogFragment : DialogFragment() {

    private lateinit var startDateTextView: TextView
    private lateinit var endDateTextView: TextView
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var medicineId: Long = -1
    private lateinit var periodType: PeriodType
    private lateinit var description: String
    private val reminderDates = mutableListOf<LocalDate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            medicineId = it.getLong("medicineId")
            periodType = it.getSerializable("periodType") as PeriodType
            description = it.getString("description") ?: ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_select_period, null)

        startDateTextView = view.findViewById(R.id.text_start_date)
        endDateTextView = view.findViewById(R.id.text_end_date)

        val buttonNext = view.findViewById<Button>(R.id.button_next_period)

        startDateTextView.setOnClickListener {
            showDatePicker { calendar ->
                startDate = calendar
                startDateTextView.text = dateFormat.format(calendar.time)
            }
        }

        endDateTextView.setOnClickListener {
            showDatePicker { calendar ->
                endDate = calendar
                endDateTextView.text = dateFormat.format(calendar.time)
            }
        }

        buttonNext.setOnClickListener {
            if (startDate == null || endDate == null) {
                Toast.makeText(requireContext(), "Выберите обе даты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = LocalDate.now()
            val localStartDate = startDate!!.toLocalDate()
            val localEndDate = endDate!!.toLocalDate()

            // Проверка, что даты не в прошлом
            if (localStartDate.isBefore(now) || localEndDate.isBefore(now)) {
                Toast.makeText(requireContext(), "Выберите будущие даты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (localStartDate.isAfter(localEndDate)) {
                Toast.makeText(requireContext(), "Дата начала позже даты окончания", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var currentDate = localStartDate

            while (!currentDate.isAfter(localEndDate)) {
                reminderDates.add(currentDate)
                currentDate = when (periodType) {
                    PeriodType.DAILY -> currentDate.plusDays(1)
                    PeriodType.EVERY_OTHER_DAY -> currentDate.plusDays(2)
                    PeriodType.EVERY_TWO_DAYS -> currentDate.plusDays(3)
                    PeriodType.EVERY_THREE_DAYS -> currentDate.plusDays(4)
                    else -> {
                        Toast.makeText(requireContext(), "Неподдерживаемый тип периода", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            }

            if (reminderDates.isEmpty()) {
                Toast.makeText(requireContext(), "Не удалось найти подходящие даты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

//            // Формируем сообщение с данными
//            val message = buildString {
//                append("💊 Лекарство ID: $medicineId\n")
//                append("📆 Тип периода: $periodType\n")
//                append("📝 Описание: $description\n")
//                append("📅 Даты напоминаний:\n")
//                reminderDates.forEach { date ->
//                    append("- $date\n")
//                }
//            }
//
////          Показываем Toast
//            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

//
            val finalDialog = AddReminderFinalFragment.newInstance(
                selectedMedicineId = medicineId,
                periodType = periodType,
                description = description,
                reminderDates = ArrayList(reminderDates) // добавьте поддержку, если нужно
            )
            finalDialog.show(parentFragmentManager, "DialogAddReminderFinal")
            dismiss()
        }


        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Calendar.toLocalDate(): LocalDate {
        return LocalDate.of(get(Calendar.YEAR), get(Calendar.MONTH) + 1, get(Calendar.DAY_OF_MONTH))
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
}

