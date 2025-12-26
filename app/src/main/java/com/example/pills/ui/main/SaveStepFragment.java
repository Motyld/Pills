package com.example.pills.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.notifications.NotificationScheduler;

import java.util.Calendar;

public class SaveStepFragment extends Fragment {

    private static final String ARG_DRAFT = "draft";

    public SaveStepFragment() {}

    public static SaveStepFragment newInstance(ReminderDraft d) {
        SaveStepFragment f = new SaveStepFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DRAFT, d);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_step_save, container, false);

        Button btnSave = v.findViewById(R.id.btnSave);

        final ReminderDraft d = (ReminderDraft) requireArguments().getSerializable(ARG_DRAFT);
        btnSave.setOnClickListener(view -> saveAll(d));

        return v;
    }

    private void saveAll(ReminderDraft d) {
        if (d == null) return;

        if (d.items == null || d.items.isEmpty()) {
            Toast.makeText(requireContext(), "Добавь хотя бы одно лекарство", Toast.LENGTH_SHORT).show();
            return;
        }

        // базовое время
        Calendar base = Calendar.getInstance();
        base.set(d.startYear, d.startMonth, d.startDay, d.startHour, d.startMinute, 0);
        base.set(Calendar.MILLISECOND, 0);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        int created = 0;

        // ✅ РЕДАКТИРОВАНИЕ одного события
        if (d.editMode && d.editReminderId != -1) {

            long reminderId = d.editReminderId;

            long ts = base.getTimeInMillis();
            String timeText = String.format("%02d:%02d",
                    base.get(Calendar.HOUR_OF_DAY), base.get(Calendar.MINUTE));

            db.updateReminderTime(reminderId, ts, timeText);
            db.updateReminderSchedule(reminderId, d.schedule);

            // пересоздаём items
            db.deleteReminderItems(reminderId);
            for (ReminderDraft.Item it : d.items) {
                if (it == null || it.drugId <= 0) continue;

                // ✅ dose теперь не поле, а текст из value+unit
                db.addReminderItem(reminderId, it.drugId, it.getDoseText());
            }

            NotificationScheduler.scheduleOneTime(
                    requireContext(),
                    "multi",
                    "Время " + timeText,
                    ts,
                    reminderId
            );

            db.close();

            Toast.makeText(requireContext(), "✅ Обновлено", Toast.LENGTH_SHORT).show();
            requireContext().sendBroadcast(new Intent("com.example.pills.REFRESH_REMINDERS"));
            requireActivity().finish();
            return;
        }

        // ✅ СОЗДАНИЕ (как было: много дней/времён)
        int totalDays = 30;
        String daysStr = "[1,2,3,4,5,6,7]";

        for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
            for (int occ = 0; occ < d.timesPerDay; occ++) {

                Calendar cal = (Calendar) base.clone();
                cal.add(Calendar.DAY_OF_MONTH, dayOffset);

                if (d.timesPerDay > 1) {
                    cal.add(Calendar.MINUTE, occ * d.repeatMinutes);
                }

                long ts = cal.getTimeInMillis();
                String timeText = String.format("%02d:%02d",
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

                long reminderId = db.insertReminderEvent(
                        ts,
                        timeText,
                        daysStr,
                        d.schedule
                );

                for (ReminderDraft.Item it : d.items) {
                    if (it == null || it.drugId <= 0) continue;
                    db.addReminderItem(reminderId, it.drugId, it.getDoseText());
                }

                NotificationScheduler.scheduleOneTime(
                        requireContext(),
                        "multi",
                        "Время " + timeText,
                        ts,
                        reminderId
                );

                created++;
            }
        }

        db.close();

        Toast.makeText(requireContext(),
                "✅ Создано " + created + " уведомлений",
                Toast.LENGTH_LONG).show();

        requireContext().sendBroadcast(new Intent("com.example.pills.REFRESH_REMINDERS"));
        requireActivity().finish();
    }
}
