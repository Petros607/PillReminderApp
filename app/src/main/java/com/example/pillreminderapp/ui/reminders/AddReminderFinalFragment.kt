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

            Toast.makeText(requireContext(), "Напоминание сохранено", Toast.LENGTH_SHORT).show()
            dismiss()
        }
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
