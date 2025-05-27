package com.example.pillreminderapp.ui.medicine

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pillreminderapp.DevInfoActivity
import com.example.pillreminderapp.R
import com.example.pillreminderapp.SystemInfoActivity
import com.example.pillreminderapp.databinding.FragmentMedicineMenuBinding
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Medicine
import com.example.pillreminderapp.ui.adapters.MedicineAdapter
import kotlinx.coroutines.launch

class MedicineMenuFragment : Fragment() {

    private var _binding: FragmentMedicineMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MedicineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineMenuBinding.inflate(inflater, container, false)

        binding.imageLogo.setOnClickListener {
            openDevInfo()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddMedicine.setOnClickListener { showMedicineDialog() }
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

    // Общий метод для создания адаптера Spinner с локализованными значениями DosageForm
    private fun createDosageFormAdapter(): ArrayAdapter<DosageForm> {
        val forms = DosageForm.values()
        return object : ArrayAdapter<DosageForm>(
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
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    // Показать диалог добавления/редактирования лекарства.
    // Если medicine == null — добавление, иначе редактирование
    private fun showMedicineDialog(medicine: Medicine? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medicine, null)

        val title = dialogView.findViewById<TextView>(R.id.dialog_add_medicine_title)
        val nameEdit = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_medicine_name)
        val substanceEdit = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_active_substance)
        val firmEdit = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_firm)
        val spinner = dialogView.findViewById<Spinner>(R.id.dialog_add_medicine_spinner_form)

        val adapter = createDosageFormAdapter()
        spinner.adapter = adapter

        if (medicine == null) {
            title.text = getString(R.string.add_medicine)
        } else {
            title.text = getString(R.string.edit_medicine)
            nameEdit.setText(medicine.name)
            substanceEdit.setText(medicine.substance)
            firmEdit.setText(medicine.manufacturer)
            spinner.setSelection(DosageForm.values().indexOf(medicine.dosageForm))
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)

        dialogView.findViewById<Button>(R.id.dialog_add_medicine_btn_save).setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val substance = substanceEdit.text.toString().trim()
            val firm = firmEdit.text.toString().trim()
            val dosageForm = spinner.selectedItem as DosageForm

            if (name.isEmpty()) {
                nameEdit.error = getString(R.string.error_empty_name)
                return@setOnClickListener
            }

            val med = if (medicine == null) {
                Medicine(name = name, substance = substance, manufacturer = firm, dosageForm = dosageForm)
            } else {
                medicine.copy(name = name, substance = substance, manufacturer = firm, dosageForm = dosageForm)
            }

            lifecycleScope.launch {
                val db = AppDatabase.getInstance(requireContext())
                if (medicine == null) {
                    db.medicineDao().insert(med)
                    Log.d("Table medicine", "medicine сохранен!")
                } else {
                    db.medicineDao().update(med)
                }
                loadMedicines()
            }
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
            showMedicineDialog(medicine)
        }

        dialog.show()
    }

    private fun openDevInfo() {
        val intent = Intent(requireActivity(), DevInfoActivity::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
