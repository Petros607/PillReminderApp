package com.example.pillreminderapp.ui.reminders

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.pillreminderapp.R
import com.example.pillreminderapp.databinding.DialogAddReminderStartBinding
import com.example.pillreminderapp.db.entities.Medicine
import com.example.pillreminderapp.db.entities.PeriodType
import com.example.pillreminderapp.viewmodel.AddReminderStartDialogViewModel

class AddReminderStartDialogFragment : DialogFragment() {

    private var _binding: DialogAddReminderStartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddReminderStartDialogViewModel by viewModels()

    private lateinit var adapter: ArrayAdapter<String>
    private val searchResults = mutableListOf<Medicine>()
    private var selectedMedicine: Medicine? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_reminder_start)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddReminderStartBinding.inflate(inflater, container, false)

        setupPeriodSpinner()
        setupMedicineAutoComplete()

        binding.buttonNext.setOnClickListener {
            val periodType = PeriodType.values()[binding.spinnerPeriod.selectedItemPosition]
            val description = binding.editDescription.text.toString()

            if (selectedMedicine == null) {
                Toast.makeText(requireContext(), "Выберите лекарство", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.length > 50) {
                binding.editDescription.error = "Описание не должно превышать 50 символов"
                return@setOnClickListener
            }

            val args = Bundle().apply {
                putLong("medicineId", selectedMedicine!!.id)
                putSerializable("periodType", periodType)
                putString("description", description)
            }

            val dialog = SelectPeriodDialogFragment()
            dialog.arguments = args
            dialog.show(parentFragmentManager, "SelectPeriodDialog")

            dismiss()

        }

        return binding.root
    }

    private fun setupPeriodSpinner() {
        val localizedNames = PeriodType.values().map { it.getLocalizedName(requireContext()) }

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, localizedNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriod.adapter = spinnerAdapter

    }

    private fun setupMedicineAutoComplete() {
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.autoMedicineSearch.setAdapter(adapter)

        binding.autoMedicineSearch.addTextChangedListener {
            val query = it.toString()
            viewModel.searchMedicines(query)
        }

        binding.autoMedicineSearch.setOnItemClickListener { _, _, position, _ ->
            selectedMedicine = searchResults[position]
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { medicines ->
            searchResults.clear()
            searchResults.addAll(medicines)
            adapter.clear()
            adapter.addAll(medicines.map { "${it.name} (${it.manufacturer})" })
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
