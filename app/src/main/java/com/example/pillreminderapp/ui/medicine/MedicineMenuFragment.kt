package com.example.pillreminderapp.ui.medicine


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pillreminderapp.R
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.databinding.FragmentMedicineMenuBinding
import com.example.pillreminderapp.ui.adapters.MedicineAdapter
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
//        adapter = MedicineAdapter(
//            onItemClick = { medicine ->
//                // переход к подробной информации о лекарстве
//                val action = MedicineMenuFragmentDirections
//                    .actionMedicineMenuToMedicineDetail(medicine.id)
//                findNavController().navigate(action)
//            },
//            onReminderClick = { medicine ->
//                // переход к экрану напоминаний по лекарству
//                val action = MedicineMenuFragmentDirections
//                    .actionMedicineMenuToReminderList(medicine.id)
//                findNavController().navigate(action)
//            }
//        )
//        binding.rvMedicineList.adapter = adapter

        // Загрузка данных
//        val dao = AppDatabase.getInstance(requireContext()).medicineDao()
//        lifecycleScope.launch {
//            val medicines = dao.getAll()
//            adapter.submitList(medicines)
//        }

        // Добавить лекарство
//        binding.fabAddMedicine.setOnClickListener {
//            findNavController().navigate(R.id.action_medicineMenu_to_addMedicineFragment)
//        }
//
//        // Справка по логотипу
//        binding.imageLogo.setOnClickListener {
//            findNavController().navigate(R.id.action_medicineMenu_to_helpFragment)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}