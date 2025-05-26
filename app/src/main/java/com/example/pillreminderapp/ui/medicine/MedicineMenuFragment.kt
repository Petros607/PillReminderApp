package com.example.pillreminderapp.ui.medicine


import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pillreminderapp.R
import com.example.pillreminderapp.databinding.FragmentMedicineMenuBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.lifecycle.lifecycleScope
import com.example.pillreminderapp.db.entities.DosageForm
import com.example.pillreminderapp.db.entities.Medicine
import com.example.pillreminderapp.db.AppDatabase
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_medicine)
        fab?.setOnClickListener {
            showAddMedicineDialog()
        }

        loadMedicines()

        binding.fabAddMedicine.setOnClickListener {
            showAddMedicineDialog()
        }
    }

    private fun loadMedicines() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val medicines = db.medicineDao().getAll()
            adapter = MedicineAdapter(medicines) { selectedMedicine ->
                // TODO: открыть подробную информацию (например, диалог или другой фрагмент)
                Toast.makeText(requireContext(), "Нажато: ${selectedMedicine.name}", Toast.LENGTH_SHORT).show()
            }
            binding.rvMedicineList.adapter = adapter
        }
    }

    private fun showAddMedicineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medicine, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.dialog_add_medicine_spinner_form)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.medicine_forms,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Закрытие по тапу вне окна
        dialog.setCanceledOnTouchOutside(true)

        // Обработка нажатия кнопки "Готово"
        dialogView.findViewById<Button>(R.id.dialog_add_medicine_btn_save).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_medicine_name).text.toString()
            val substance = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_active_substance).text.toString()
            val firm = dialogView.findViewById<EditText>(R.id.dialog_add_medicine_edit_firm).text.toString()
            val formString = spinner.selectedItem.toString()

            // Конвертация строки из спиннера в DosageForm
            val dosageForm = when (formString) {
                "Таблетка" -> DosageForm.TABLET
                "Капсула" -> DosageForm.CAPSULE
                "Процедура" -> DosageForm.PROCEDURE
                "Раствор" -> DosageForm.SOLUTION
                "Капля" -> DosageForm.DROPLET
                "Жидкость" -> DosageForm.LIQUID
                "Мазь" -> DosageForm.OINTMENT
                "Спрей" -> DosageForm.SPRAY
                "Иное" -> DosageForm.OTHER
                else -> DosageForm.OTHER
            }

            val medicine = Medicine(
                name = name,
                substance = substance,
                manufacturer = firm,
                dosageForm = dosageForm
            )

            lifecycleScope.launch {
                // Получаем инстанс БД и DAO
                val db = AppDatabase.getInstance(requireContext())
                db.medicineDao().insert(medicine)
                loadMedicines()
            }
            Log.d("Table medicine", "medicine сохранен!")
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}