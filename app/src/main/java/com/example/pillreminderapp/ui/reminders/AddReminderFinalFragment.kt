package com.example.pillreminderapp.ui.reminders

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.entities.PeriodType
import java.time.LocalDate

class AddReminderFinalFragment : DialogFragment() {

    private lateinit var doseListLayout: LinearLayout
    private lateinit var reminderListLayout: LinearLayout
    private lateinit var btnAddDose: ImageButton
    private lateinit var btnAddReminder: ImageButton
    private lateinit var btnSave: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_reminder_final, null)

        doseListLayout = view.findViewById(R.id.doseListLayout)
        reminderListLayout = view.findViewById(R.id.reminderListLayout)
        btnAddDose = view.findViewById(R.id.btn_add_dose_time)
        btnAddReminder = view.findViewById(R.id.btn_add_reminder)
        btnSave = view.findViewById(R.id.btn_save)

        setupListeners()

        return builder.setView(view).create()
    }

    private fun setupListeners() {
        btnAddDose.setOnClickListener {
            addDoseItem()
        }

        btnAddReminder.setOnClickListener {
            addReminderItem()
        }

        btnSave.setOnClickListener {
            Toast.makeText(requireContext(), "Напоминание сохранено", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun addDoseItem() {
        val doseItem = layoutInflater.inflate(R.layout.item_dose_time, null)

        val timeText = doseItem.findViewById<TextView>(R.id.text_time)
        timeText.setOnClickListener {
            val now = LocalDate.now()
            val timePicker = android.app.TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val timeString = String.format("%02d:%02d", hourOfDay, minute)
                    timeText.text = timeString
                },
                12, 0, true
            )
            timePicker.show()
        }

        doseListLayout.addView(doseItem)
    }

    private fun addReminderItem() {
        val reminderItem = layoutInflater.inflate(R.layout.item_reminder_time, null)

        val spinner = reminderItem.findViewById<Spinner>(R.id.spinner_reminder_time)
        val options = listOf("В момент приема", "За 10 минут", "За 15 минут", "За 30 минут", "За 1 час", "Нет")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        reminderListLayout.addView(reminderItem)
    }


    companion object {
        fun newInstance(
            selectedMedicineId: Int,
            periodType: PeriodType,
            startDate: LocalDate,
            endDate: LocalDate,
            description: String
        ): AddReminderFinalFragment {
            val args = Bundle().apply {
                putInt("medicineId", selectedMedicineId)
                putSerializable("periodType", periodType)
                putSerializable("startDate", startDate.toString())
                putSerializable("endDate", endDate.toString())
                putString("description", description)
            }
            return AddReminderFinalFragment().apply {
                arguments = args
            }
        }
    }
}
