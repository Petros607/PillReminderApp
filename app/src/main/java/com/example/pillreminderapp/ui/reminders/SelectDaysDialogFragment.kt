package com.example.pillreminderapp.ui.reminders

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.example.pillreminderapp.R

class SelectDaysDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_select_days)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        val selectedDays = mutableListOf<String>()
        val daysIds = listOf(
            R.id.checkbox_monday,
            R.id.checkbox_tuesday,
            R.id.checkbox_wednesday,
            R.id.checkbox_thursday,
            R.id.checkbox_friday,
            R.id.checkbox_saturday,
            R.id.checkbox_sunday
        )

        val buttonNext = dialog?.findViewById<View>(R.id.button_next_days)
        buttonNext?.setOnClickListener {
            selectedDays.clear()
            for (id in daysIds) {
                val checkBox = dialog?.findViewById<CheckBox>(id)
                if (checkBox?.isChecked == true) {
                    selectedDays.add(checkBox.text.toString())
                }
            }

            val args = Bundle(arguments) // скопировать текущие аргументы

            args.putStringArrayList("selectedDays", ArrayList(selectedDays)) // если нужно

            val dialog = SelectPeriodDialogFragment()
            dialog.arguments = args
            dialog.show(parentFragmentManager, "SelectPeriodDialog")
            dismiss()
        }
    }
}
