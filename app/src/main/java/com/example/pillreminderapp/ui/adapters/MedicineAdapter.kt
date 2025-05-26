//package com.example.pillreminderapp.ui.adapters
//
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.pillreminderapp.db.entities.Medicine
//import com.example.pillreminderapp.databinding.ItemMedicineBinding
//
//class MedicineAdapter(
//    private val onItemClick: (Medicine) -> Unit,
//    private val onReminderClick: (Medicine) -> Unit
//) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {
//
//    private var items = listOf<Medicine>()
//
//    fun submitList(data: List<Medicine>) {
//        items = data
//        notifyDataSetChanged()
//    }
//
//    inner class MedicineViewHolder(
//        private val binding: ItemMedicineBinding
//    ) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(medicine: Medicine) {
//            binding.tvName.text = medicine.name
//            binding.tvSubstance.text = medicine.substance
//            binding.root.setOnClickListener { onItemClick(medicine) }
//            binding.btnReminders.setOnClickListener { onReminderClick(medicine) }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
//        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return MedicineViewHolder(binding)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
//        holder.bind(items[position])
//    }
//}

package com.example.pillreminderapp.ui.medicine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pillreminderapp.databinding.ItemMedicineBinding
import com.example.pillreminderapp.db.entities.Medicine

class MedicineAdapter(private val medicines: List<Medicine>) :
    RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine) {
            binding.tvMedicineName.text = medicine.name
            binding.tvMedicineSubstance.text = "Вещество: ${medicine.substance}"
            binding.tvMedicineFirm.text = "Производитель: ${medicine.manufacturer}"
            binding.tvMedicineForm.text = "Форма: ${medicine.dosageForm}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMedicineBinding.inflate(inflater, parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(medicines[position])
    }

    override fun getItemCount() = medicines.size
}
