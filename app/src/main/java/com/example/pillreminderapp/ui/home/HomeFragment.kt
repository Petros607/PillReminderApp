package com.example.pillreminderapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pillreminderapp.DevInfoActivity
import com.example.pillreminderapp.R
import com.example.pillreminderapp.databinding.FragmentHomeBinding
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.ui.adapters.ReminderAdapter
import com.example.pillreminderapp.ui.reminders.AddReminderStartDialogFragment
import com.google.android.material.chip.Chip

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Получаем instance базы
        val db = AppDatabase.getInstance(requireContext())

        // Создаем ViewModel с фабрикой (HomeViewModelFactory нужно реализовать)
        homeViewModel = ViewModelProvider(requireActivity(), HomeViewModelFactory(db))
            .get(HomeViewModel::class.java)

        // Инициализируем адаптер с пустым списком
        reminderAdapter = ReminderAdapter(
            reminders = emptyList(),
            medicineInfo = emptyMap(),
            context = requireContext()
        )

        setupDateFilters()

        binding.remindersRecyclerView.adapter = reminderAdapter
        binding.remindersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Подписка на данные
        homeViewModel.filteredReminders.observe(viewLifecycleOwner) { reminders ->
            val currentInfo = homeViewModel.medicineInfo.value ?: emptyMap()
            reminderAdapter.updateData(reminders, currentInfo)
        }

        homeViewModel.medicineInfo.observe(viewLifecycleOwner) { medicineInfo ->
            val currentReminders = homeViewModel.filteredReminders.value ?: emptyList()
            reminderAdapter.updateData(currentReminders, medicineInfo)
        }

        // Кнопка добавления напоминания
        binding.fabAddReminder.setOnClickListener {
            val dialog = AddReminderStartDialogFragment()
            dialog.show(parentFragmentManager, "AddReminderDialog")
        }

        // Логотип - открываем активити с инфо разработчика
        binding.imageLogo.setOnClickListener {
            openDevInfo()
        }

        return binding.root
    }

    private fun setupDateFilters() {
        val chipGroup = binding.filterChipGroup
        val todayChip = createFilterChip("Сегодня", FilterType.TODAY)
        val tomorrowChip = createFilterChip("Завтра", FilterType.TOMORROW)
        val weekChip = createFilterChip("Неделя", FilterType.WEEK)
        val allChip = createFilterChip("Все", FilterType.ALL)

        chipGroup.addView(todayChip)
        chipGroup.addView(tomorrowChip)
        chipGroup.addView(weekChip)
        chipGroup.addView(allChip)

        // Выбираем "Все" по умолчанию
        allChip.isChecked = true

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener)
            val filterType = chip.tag as FilterType
            homeViewModel.applyFilter(filterType)
        }
    }

    enum class FilterType {
        TODAY, TOMORROW, WEEK, ALL
    }

    private fun createFilterChip(text: String, filterType: FilterType): Chip {
        return Chip(requireContext()).apply {
            this.text = text
            tag = filterType
            isCheckable = true
            setEnsureMinTouchTargetSize(false)
            setChipBackgroundColorResource(R.color.chip_background_selector)
            setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Запускаем загрузку данных из БД через ViewModel
        homeViewModel.loadReminders()
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
