package com.example.pillreminderapp.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.pillreminderapp.db.entities.Medicine
import com.example.pillreminderapp.db.entities.Reminder
import com.example.pillreminderapp.reminder.ReminderReceiver
import com.example.pillreminderapp.ui.adapters.ReminderAdapter
import com.example.pillreminderapp.ui.reminders.AddReminderStartDialogFragment
import com.example.pillreminderapp.ui.reminders.EditReminderDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
            showReminderDetailsDialog(reminder)
        }
        // Пока создаём с пустыми списками
        reminderAdapter = ReminderAdapter(emptyList(), emptyMap(), requireContext(), { reminder ->
            showReminderDetailsDialog(reminder)
        })

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

    private fun showReminderDetailsDialog(reminder: Reminder) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_info, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvSubtitle)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEdit)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnMarkTaken = dialogView.findViewById<Button>(R.id.btnMarkTaken)
        val tvMissed = dialogView.findViewById<TextView>(R.id.tvMissed)

        lifecycleScope.launch {
            val medicine = AppDatabase.getInstance(requireContext())
                .medicineDao()
                .getById(reminder.medicineId)

            val medicineName = medicine?.name ?: "Без названия"
            val medicineManufacturer = medicine?.manufacturer.toString()
            tvTitle.text = "${medicineName} (${medicineManufacturer})"

        }

        val reminderTime = Date(reminder.intakeTime)
        val reminderDate = Date(reminder.intakeDate)

        var formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC") // ⬅️ важно!
        val formattedTime = formatter.format(reminderTime)

        formatter = SimpleDateFormat("dd.MM.YYYY", Locale.getDefault())
        val formattedDate = formatter.format(reminderDate)

        tvSubtitle.text = "${formattedTime} (${formattedDate})"

        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
        val tvNotificationTime = dialogView.findViewById<TextView>(R.id.tvNotificationTime)

        tvDescription.text = if (!reminder.description.isNullOrBlank()) {
            "Описание: ${reminder.description}"
        } else {
            "Описание: отсутствует"
        }

        val notificationMinutes = reminder.notificationTime / 60000 // 60000 мс = 1 мин
        tvNotificationTime.text = if (notificationMinutes == 0L) {
            "Уведомление придёт во время приёма"
        } else {
            "Уведомление придёт за $notificationMinutes мин до приёма"
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)

        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                val medicineDao = AppDatabase.getInstance(requireContext()).medicineDao()
                val medicine = medicineDao.getById(reminder.medicineId)!!
                cancelNotification(reminder, medicine)
                AppDatabase.getInstance(requireContext()).reminderDao().delete(reminder)
                homeViewModel.loadReminders()
            }
            dialog.dismiss()
        }

        btnEdit.setOnClickListener {
            val newDialog = EditReminderDialog.newInstance(reminder.id).apply {
                setOnReminderEditedListener(object : EditReminderDialog.OnReminderEditedListener {
                    override fun onReminderEdited(updatedReminder: Reminder) {
                        lifecycleScope.launch {
                            val db = AppDatabase.getInstance(requireContext())
                            db.reminderDao().update(updatedReminder)

                            val medicineDao = db.medicineDao()
                            val medicine = medicineDao.getById(updatedReminder.medicineId)!!

                            cancelNotification(updatedReminder, medicine)
                            scheduleNotification(updatedReminder, medicine)

                            homeViewModel.loadReminders()
                        }
                    }
                })
            }
            dialog.dismiss()
            newDialog.show(parentFragmentManager, "EditReminderDialog")
        }

        btnMarkTaken.setOnClickListener {
            lifecycleScope.launch {
                val updatedReminder = reminder.copy(mark = true)
                AppDatabase.getInstance(requireContext()).reminderDao().update(updatedReminder)
                homeViewModel.loadReminders()
            }
            dialog.dismiss()
        }

        tvMissed.setOnClickListener {
            lifecycleScope.launch {
                val updatedReminder = reminder.copy(mark = false)
                AppDatabase.getInstance(requireContext()).reminderDao().update(updatedReminder)
                homeViewModel.loadReminders()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    public fun cancelNotification(reminder: Reminder, medicine: Medicine) {
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
            .apply {
                putExtra("ID", reminder.id.toInt())
                putExtra("dosageFormText", medicine.dosageForm.getLocalizedName(requireContext()))
                putExtra("dose", reminder.dose)
                putExtra("time", getTimeStringFromReminder(reminder))
                putExtra("medicineName", medicine.name)
        }
        Log.d("SchedulingNotification", "Scheduling notification with requestCode: ${reminder.id.toInt()}")

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id.toInt(), // тот же requestCode, что и при создании
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }


    public fun scheduleNotification(reminder: Reminder, medicine: Medicine) {
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
            .apply {
                putExtra("ID", reminder.id.toInt())
                putExtra("dosageFormText", medicine.dosageForm.getLocalizedName(requireContext()))
                putExtra("dose", reminder.dose)
                putExtra("time", getTimeStringFromReminder(reminder))
                putExtra("medicineName", medicine.name)
            }
        Log.d("SchedulingNotification", "Scheduling notification with requestCode: ${reminder.id.toInt()}")


        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = reminder.intakeDate + reminder.intakeTime - reminder.notificationTime

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    private fun getTimeStringFromReminder(reminder: Reminder): String {
        // intakeTime — миллисекунды от начала дня
        val totalSeconds = reminder.intakeTime / 1000L
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()

        val localTime = LocalTime.of(hours, minutes)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return localTime.format(formatter)
    }



}
