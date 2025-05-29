package com.example.pillreminderapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pillreminderapp.DevInfoActivity
import com.example.pillreminderapp.R
import com.example.pillreminderapp.databinding.FragmentHomeBinding
import com.example.pillreminderapp.db.AppDatabase
import com.example.pillreminderapp.db.entities.Reminder
import com.example.pillreminderapp.ui.adapters.ReminderAdapter
import com.example.pillreminderapp.ui.reminders.AddReminderStartDialogFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
        ) { reminder ->
            showReminderDetailsDialog(reminder) // 👈 вызываем диалог
        }
        // Пока создаём с пустыми списками
        reminderAdapter = ReminderAdapter(emptyList(), emptyMap(), requireContext(), { reminder ->
            showReminderDetailsDialog(reminder) // 👈 вызываем диалог
        })
        )

        setupDateFilters()

        binding.remindersRecyclerView.adapter = reminderAdapter
        binding.remindersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Подписка на данные
        homeViewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            val currentInfo = homeViewModel.medicineInfo.value ?: emptyMap()
            reminderAdapter.updateData(reminders, currentInfo)
        }

        homeViewModel.medicineInfo.observe(viewLifecycleOwner) { medicineInfo ->
            val currentReminders = homeViewModel.reminders.value ?: emptyList()
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

    private fun showReminderDetailsDialog(reminder: Reminder) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_info, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvSubtitle)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEdit)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnMarkTaken = dialogView.findViewById<Button>(R.id.btnMarkTaken)
        val tvMissed = dialogView.findViewById<TextView>(R.id.tvMissed)

        // Установка текста
        lifecycleScope.launch {
            val medicine = AppDatabase.getInstance(requireContext())
                .medicineDao()
                .getById(reminder.medicineId)

            val medicineName = medicine?.name ?: "Без названия"
            tvTitle.text = medicineName

        }

        val date = Date(reminder.intakeTime)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC") // ⬅️ важно!

        val formattedTime = formatter.format(date)
        tvSubtitle.text = "Время: $formattedTime"



        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)

        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).reminderDao().delete(reminder)
                homeViewModel.loadReminders()
            }
            dialog.dismiss()
        }

//        btnEdit.setOnClickListener {
//            dialog.dismiss()
//            val dialogFragment = AddReminderStartDialogFragment.newInstance(reminder.id)
//            dialogFragment.show(parentFragmentManager, "EditReminderDialog")
//        }

//        btnMarkTaken.setOnClickListener {
//            lifecycleScope.launch {
//                val updatedReminder = reminder.copy(isTaken = true)
//                AppDatabase.getInstance(requireContext()).reminderDao().update(updatedReminder)
//                homeViewModel.loadReminders()
//            }
//            dialog.dismiss()
//        }

        dialog.show()
    }


}
