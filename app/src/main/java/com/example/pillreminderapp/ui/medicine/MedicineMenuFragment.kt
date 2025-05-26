package com.example.pillreminderapp.ui.medicine

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pillreminderapp.R
import com.example.pillreminderapp.databinding.FragmentMedicineMenuBinding
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Medicine
import com.example.pillreminderapp.ui.adapters.MedicineAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import android.view.ViewGroup

class MedicineMenuFragment : Fragment() {

    private var _binding: FragmentMedicineMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MedicineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddMedicine.setOnClickListener {
            showAddMedicineDialog()
        }

        loadMedicines()
    }

    private fun loadMedicines() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val medicines = db.medicineDao().getAll()
            adapter = MedicineAdapter(medicines) { selectedMedicine ->
                showMedicineDetailsDialog(selectedMedicine)
            }
            binding.rvMedicineList.adapter = adapter
        }
    }

    private fun showAddMedicineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medicine, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.dialog_add_medicine_spinner_form)

        // Устанавливаем адаптер с локализованными значениями DosageForm
        val forms = DosageForm.values()
        val spinnerAdapter = object : ArrayAdapter<DosageForm>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            forms
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.getLocalizedName(context)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.getLocalizedName(context)
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)

        dialogView.findViewById<Button>(R.id.dialog_add_medicine_btn_save).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_medicine_name).text.toString()
            val substance = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_active_substance).text.toString()
            val firm = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_firm).text.toString()

            // Получаем выбранный DosageForm напрямую из enum
            val dosageForm = spinner.selectedItem as DosageForm

            val medicine = Medicine(
                name = name,
                substance = substance,
                manufacturer = firm,
                dosageForm = dosageForm
            )

            lifecycleScope.launch {
                val db = AppDatabase.getInstance(requireContext())
                db.medicineDao().insert(medicine)
                loadMedicines()
            }
            Log.d("Table medicine", "medicine сохранен!")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMedicineDetailsDialog(medicine: Medicine) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_medicine_details, null)

        dialogView.findViewById<TextView>(R.id.tv_detail_name).text = medicine.name
        dialogView.findViewById<TextView>(R.id.tv_detail_substance).text = "Активное вещество: ${medicine.substance}"
        dialogView.findViewById<TextView>(R.id.tv_detail_manufacturer).text = "Производитель: ${medicine.manufacturer}"
        dialogView.findViewById<TextView>(R.id.tv_detail_form).text = "Форма выпуска: ${medicine.dosageForm.getLocalizedName(requireContext())}"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)

        dialogView.findViewById<Button>(R.id.btn_delete).setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).medicineDao().delete(medicine)
                loadMedicines()
            }
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_edit).setOnClickListener {
            dialog.dismiss()
            showEditMedicineDialog(medicine)
        }

        dialog.show()
    }

    private fun showEditMedicineDialog(medicine: Medicine) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medicine, null)

        val title = dialogView.findViewById<TextView>(R.id.dialog_add_medicine_title)
        val nameEdit = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_medicine_name)
        val substanceEdit = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_active_substance)
        val firmEdit = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_firm)
        val spinner = dialogView.findViewById<Spinner>(R.id.dialog_add_medicine_spinner_form)

        title.text = "Редактирование лекарства"

        nameEdit.setText(medicine.name)
        substanceEdit.setText(medicine.substance)
        firmEdit.setText(medicine.manufacturer)

        val forms = DosageForm.values()
        val spinnerAdapter = object : ArrayAdapter<DosageForm>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            forms
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.getLocalizedName(context)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.getLocalizedName(context)
                return view
            }
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.setSelection(forms.indexOf(medicine.dosageForm))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)

        dialogView.findViewById<Button>(R.id.dialog_add_medicine_btn_save).setOnClickListener {
            val newName = nameEdit.text.toString()
            val newSubstance = substanceEdit.text.toString()
            val newFirm = firmEdit.text.toString()
            val newDosageForm = spinner.selectedItem as DosageForm

            val updatedMedicine = medicine.copy(
                name = newName,
                substance = newSubstance,
                manufacturer = newFirm,
                dosageForm = newDosageForm
            )

            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).medicineDao().update(updatedMedicine)
                loadMedicines()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
