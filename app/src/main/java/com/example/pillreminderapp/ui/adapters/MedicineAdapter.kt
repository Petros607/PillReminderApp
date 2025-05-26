package com.example.pillreminderapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pillreminderapp.databinding.ItemMedicineBinding
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Medicine

class MedicineAdapter(private val medicines: List<Medicine>,
                      private val onClick: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine) {
            binding.tvMedicineName.text = medicine.name
//            binding.tvMedicineSubstance.text = "Вещество: ${medicine.substance}"
            binding.tvMedicineFirm.text = "Производитель: ${medicine.manufacturer}"
            binding.tvMedicineForm.text = "Форма: ${dosageFormToString(medicine.dosageForm)}"
        }

        fun dosageFormToString(form: DosageForm): String {
            return when (form) {
                DosageForm.TABLET -> "Таблетка"
                DosageForm.CAPSULE -> "Капсула"
                DosageForm.PROCEDURE -> "Процедура"
                DosageForm.SOLUTION -> "Раствор"
                DosageForm.DROPLET -> "Капля"
                DosageForm.LIQUID -> "Жидкость"
                DosageForm.OINTMENT -> "Мазь"
                DosageForm.SPRAY -> "Спрей"
                DosageForm.OTHER -> "Иное"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMedicineBinding.inflate(inflater, parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.bind(medicine)
        holder.itemView.setOnClickListener { onClick(medicine) }
    }

    override fun getItemCount() = medicines.size
}
