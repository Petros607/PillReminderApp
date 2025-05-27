package com.example.pillreminderapp.ui.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.pillreminderapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SelectPeriodDialogFragment : DialogFragment() {

    private lateinit var startDateTextView: TextView
    private lateinit var endDateTextView: TextView
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            } else {
                // TODO: сохранить или передать даты
                dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
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

