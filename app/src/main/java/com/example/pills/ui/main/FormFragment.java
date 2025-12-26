package com.example.pills.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.notifications.NotificationScheduler;

import java.util.Arrays;
import java.util.Calendar;

public class FormFragment extends Fragment {

    private static final String ARG_MEDICINE_NAME = "medicine_name";
    private String medicineName;
    private DatabaseHelper db;

    public static FormFragment newInstance(String medName) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEDICINE_NAME, medName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_form, container, false);
        db = new DatabaseHelper(requireContext());

        TextView tvSelectedDrug = v.findViewById(R.id.tvSelectedDrug);
        Spinner spForm = v.findViewById(R.id.spForm);
        Spinner spSchedule = v.findViewById(R.id.spSchedule);
        TimePicker tpStartTime = v.findViewById(R.id.tpStartTime);
        DatePicker dpStartDate = v.findViewById(R.id.dpStartDate);
        Button btnSave = v.findViewById(R.id.btnSaveMedicine);

        if (getArguments() != null) {
            medicineName = getArguments().getString(ARG_MEDICINE_NAME, "");
            tvSelectedDrug.setText(medicineName);
        }

        // Форма выпуска
        ArrayAdapter<String> formAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("Таблетка", "Капсула", "Жидкое", "Порошок", "Сироп")
        );
        formAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spForm.setAdapter(formAdapter);

        // Расписание
        ArrayAdapter<String> scheduleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("Раз в день", "2 раза в день", "3 раза в день", "После еды", "Перед сном")
        );
        scheduleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSchedule.setAdapter(scheduleAdapter);

        btnSave.setOnClickListener(view -> saveReminder(tpStartTime, dpStartDate, spForm, spSchedule));

        return v;
    }

    private void saveReminder(TimePicker tp, DatePicker dp, Spinner spForm, Spinner spSchedule) {
        int hour = tp.getHour();
        int minute = tp.getMinute();
        int day = dp.getDayOfMonth();
        int month = dp.getMonth();
        int year = dp.getYear();

        String timeText = String.format("%02d:%02d", hour, minute);
        String form = String.valueOf(spForm.getSelectedItem());
        String schedule = String.valueOf(spSchedule.getSelectedItem());

        Log.d("FormFragment", "💊 Создание: " + medicineName + " | " + form + " | " + schedule);

        long drugId = db.findDrugByName(medicineName);
        if (drugId == -1) drugId = db.insertDrugIfMissing(medicineName);

        // дозу можно пока брать из drugs.dosage (или оставить пусто)
        String dose = ""; // или db.getDrugDosageByName(medicineName) если метод есть

        Calendar baseCal = Calendar.getInstance();
        baseCal.set(year, month, day, hour, minute, 0);
        baseCal.set(Calendar.MILLISECOND, 0);

        int totalDays = getDaysCountBySchedule(schedule);
        int dailyOccurrences = getDailyOccurrences(schedule);

        int reminderCount = 0;

        for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
            for (int occurrence = 0; occurrence < dailyOccurrences; occurrence++) {
                Calendar cal = (Calendar) baseCal.clone();

                // 1) сдвиг дня
                cal.add(Calendar.DAY_OF_MONTH, dayOffset);

                // 2) распределяем приёмы в день (примерно равномерно)
                int timesPerDay = dailyOccurrences;
                int hourOffset = (24 / timesPerDay) * occurrence;

                cal.set(Calendar.HOUR_OF_DAY, hour + hourOffset);
                cal.set(Calendar.MINUTE, minute);

                // 3) если перелезли за 24 часа — переносим на следующий день
                if (cal.get(Calendar.HOUR_OF_DAY) >= 24) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 24);
                }

                long timestamp = cal.getTimeInMillis();
                String daySchedule = getDaysArray(schedule);

                // ✅ НОВОЕ: создаём СОБЫТИЕ (reminders)
                long reminderId = db.insertReminderEvent(
                        timestamp,
                        timeText,
                        daySchedule,
                        schedule
                );

                // ✅ добавляем лекарство внутрь (reminder_items)
                db.addReminderItem(reminderId, drugId, dose);

                // ✅ планируем уведомление
                String displayTitle = medicineName + " (" + form + ") - " + schedule;

                NotificationScheduler.scheduleOneTime(
                        requireContext(),
                        medicineName,
                        displayTitle,
                        timestamp,
                        reminderId
                );

                reminderCount++;
                Log.d("FormFragment", "✅ ID=" + reminderId + " " + displayTitle + " ts=" + timestamp);
            }
        }

        Toast.makeText(requireContext(),
                "✅ Создано " + reminderCount + " напоминаний для " + medicineName,
                Toast.LENGTH_LONG).show();

        requireContext().sendBroadcast(new Intent("com.example.pills.REFRESH_REMINDERS"));
        requireActivity().onBackPressed();
    }

    private int getDaysCountBySchedule(String schedule) {
        switch (schedule) {
            case "Раз в день": return 30;
            case "2 раза в день": return 30;
            case "3 раза в день": return 30;
            case "После еды": return 7;
            case "Перед сном": return 14;
            default: return 1;
        }
    }

    private int getDailyOccurrences(String schedule) {
        switch (schedule) {
            case "Раз в день": return 1;
            case "2 раза в день": return 2;
            case "3 раза в день": return 3;
            case "После еды": return 3;
            case "Перед сном": return 1;
            default: return 1;
        }
    }

    private String getDaysArray(String schedule) {
        return "[1,2,3,4,5,6,7]";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) db.close();
    }
}
