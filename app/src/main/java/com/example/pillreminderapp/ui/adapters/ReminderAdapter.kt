package com.example.pillreminderapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.entities.Reminder
import com.example.pillreminderapp.db.entities.ReminderDisplayItem


class ReminderAdapter(
    private var reminders: List<ReminderDisplayItem>
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
        holder.timeText.text = reminder.time
        holder.dateText.text = reminder.date
        holder.medicineText.text = reminder.medicineName
        holder.dosageText.text = reminder.dosage
    }

    override fun getItemCount(): Int = reminders.size

    fun updateData(newReminders: List<ReminderDisplayItem>) {
        reminders = newReminders
        notifyDataSetChanged()
    }
}

