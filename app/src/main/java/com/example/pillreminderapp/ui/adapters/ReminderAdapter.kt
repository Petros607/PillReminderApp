package com.example.pillreminderapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.dao.ReminderDao
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Reminder
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(
    private var reminders: List<Reminder>,
    private var medicineInfo: Map<Long, Pair<String, DosageForm>>,
    private val context: Context
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.text_reminder_time)
        val dateText: TextView = itemView.findViewById(R.id.text_reminder_date)
        val medicineText: TextView = itemView.findViewById(R.id.text_reminder_medicine)
        val dosageText: TextView = itemView.findViewById(R.id.text_reminder_dosage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        // Форматирование времени и даты
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val timeStr = timeFormat.format(Date(reminder.intakeTime))
        val dateStr = dateFormat.format(Date(reminder.intakeDate))

        holder.timeText.text = timeStr
        holder.dateText.text = dateStr

        val (medicineName, dosageForm) = medicineInfo[reminder.medicineId]
            ?: ("Unknown" to DosageForm.OTHER)

        holder.medicineText.text = medicineName
        holder.dosageText.text = "${reminder.dose} ${dosageForm.getLocalizedName(context)}"
    }

    override fun getItemCount(): Int = reminders.size

    fun updateData(
        newReminders: List<Reminder>,
        newMedicineInfo: Map<Long, Pair<String, DosageForm>>
    ) {
        reminders = newReminders
        medicineInfo = newMedicineInfo
        notifyDataSetChanged()
    }
}
