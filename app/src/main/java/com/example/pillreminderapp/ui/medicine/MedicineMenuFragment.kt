package com.example.pillreminderapp.ui.medicine

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.entities.Medicine
//import com.example.pillreminderapp.db.entities.MedicineDatabase
import com.example.pillreminderapp.databinding.FragmentMedicineMenuBinding
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.ui.adapters.MedicineAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        adapter = MedicineAdapter(
            onItemClick = { medicine ->
                val action = MedicineMenuFragmentDirections
                    .actionMedicineMenuToMedicineDetail(medicine.id)
                findNavController().navigate(action)
            },
            onReminderClick = { medicine ->
                val action = MedicineMenuFragmentDirections
                    .actionMedicineMenuToReminderList(medicine.id)
                findNavController().navigate(action)
            }
        )
        binding.rvMedicineList.adapter = adapter

        loadMedicines()

        binding.imageLogo.setOnClickListener {
            findNavController().navigate(R.id.action_medicineMenu_to_helpFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAddMedicineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medicine, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val nameField = dialogView.findViewById<EditText>(R.id.et_name)
        val substanceField = dialogView.findViewById<EditText>(R.id.et_active_substance)
        val manufacturerField = dialogView.findViewById<EditText>(R.id.et_manufacturer)
        val formField = dialogView.findViewById<EditText>(R.id.et_form)
        val submitBtn = dialogView.findViewById<Button>(R.id.btn_submit)

        submitBtn.setOnClickListener {
            val name = nameField.text.toString().trim()
            val substance = substanceField.text.toString().trim()
            val manufacturer = manufacturerField.text.toString().trim()
            val form = formField.text.toString().trim()

            if (name.isNotEmpty() && substance.isNotEmpty() && manufacturer.isNotEmpty() && form.isNotEmpty()) {
                val newMedicine = Medicine(
                    name = name,
                    activeSubstance = substance,
                    manufacturer = manufacturer,
                    form = form
                )

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        AppDatabase.getDatabase(requireContext())
                            .medicineDao()
                            .insertMedicine(newMedicine)
                    }
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Лекарство добавлено", Toast.LENGTH_SHORT).show()
                    loadMedicines()
                }
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun loadMedicines() {
        lifecycleScope.launch {
            val medicines = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext())
                    .medicineDao()
                    .getAllMedicines()
            }
            adapter.submitList(medicines)
        }
    }
}
