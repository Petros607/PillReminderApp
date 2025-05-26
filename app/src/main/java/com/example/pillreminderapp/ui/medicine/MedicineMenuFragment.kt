package com.example.pillreminderapp.ui.medicine


import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.databinding.FragmentMedicineMenuBinding
import com.example.pillreminderapp.ui.adapters.MedicineAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MedicineMenuFragment : Fragment() {

    private var _binding: FragmentMedicineMenuBinding? = null
    private val binding get() = _binding!!

//    private lateinit var adapter: MedicineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fabAddMedicine = requireActivity().findViewById<FloatingActionButton>(R.id.fab_add_medicine)

        fabAddMedicine.setOnClickListener {
            showAddMedicineDialog()
        }
    }

    private fun showAddMedicineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medicine, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_form)
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
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.edit_medicine_name).text.toString()
            val substance = dialogView.findViewById<EditText>(R.id.edit_active_substance).text.toString()
            val firm = dialogView.findViewById<EditText>(R.id.edit_firm).text.toString()
            val form = dialogView.findViewById<Spinner>(R.id.spinner_form).selectedItem.toString()

            // TODO: Сохрани в базу данных

            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}