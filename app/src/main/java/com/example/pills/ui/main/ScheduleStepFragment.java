package com.example.pills.ui.main;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ScheduleStepFragment extends Fragment {

    private static final String ARG_DRAFT = "draft";

    // ✅ делаем draft полем
    private ReminderDraft draft;

    public ScheduleStepFragment() {}

    public static ScheduleStepFragment newInstance(ReminderDraft d) {
        ScheduleStepFragment f = new ScheduleStepFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DRAFT, d);
        f.setArguments(b);
        return f;
    }

    public static ScheduleStepFragment newInstanceEdit(long reminderId) {
        ReminderDraft d = new ReminderDraft();
        d.editMode = true;
        d.editReminderId = reminderId;

        ScheduleStepFragment f = new ScheduleStepFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DRAFT, d);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_step_schedule, container, false);

        Spinner spSchedule = v.findViewById(R.id.spSchedule);
        Button next = v.findViewById(R.id.btnNext);

        List<String> schedules = Arrays.asList("Раз в день", "2 раза в день", "3 раза в день");

        ArrayAdapter<String> ad = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                schedules
        );
        spSchedule.setAdapter(ad);

        // ✅ берём draft один раз и НЕ переопределяем локально
        draft = (ReminderDraft) requireArguments().getSerializable(ARG_DRAFT);
        if (draft == null) draft = new ReminderDraft();

        // ✅ редактирование: подтягиваем timestamp + schedule
        if (draft.editMode) {
            DatabaseHelper db = new DatabaseHelper(requireContext());
            Cursor c = db.getReminderById(draft.editReminderId); // должен отдавать schedule тоже

            if (c != null) {
                if (c.moveToFirst()) {
                    long ts = c.getLong(c.getColumnIndexOrThrow("timestamp"));
                    String schedule = c.getString(c.getColumnIndexOrThrow("schedule"));

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(ts);

                    draft.startYear = cal.get(Calendar.YEAR);
                    draft.startMonth = cal.get(Calendar.MONTH);
                    draft.startDay = cal.get(Calendar.DAY_OF_MONTH);
                    draft.startHour = cal.get(Calendar.HOUR_OF_DAY);
                    draft.startMinute = cal.get(Calendar.MINUTE);

                    if (schedule != null) {
                        draft.schedule = schedule;
                        setSpinnerValue(spSchedule, schedule);
                    }

                    if ("Раз в день".equals(draft.schedule)) draft.timesPerDay = 1;
                    if ("2 раза в день".equals(draft.schedule)) draft.timesPerDay = 2;
                    if ("3 раза в день".equals(draft.schedule)) draft.timesPerDay = 3;

                    // ✅ расписание уже настроено
                    draft.scheduleConfigured = true;
                }
                c.close();
            }
            db.close();
        } else {
            // ✅ если уже было выбрано ранее (возврат назад)
            if (draft.schedule != null && !draft.schedule.isEmpty()) {
                setSpinnerValue(spSchedule, draft.schedule);
            }
        }

        next.setOnClickListener(v1 -> {
            draft.schedule = String.valueOf(spSchedule.getSelectedItem());

            if ("Раз в день".equals(draft.schedule)) draft.timesPerDay = 1;
            if ("2 раза в день".equals(draft.schedule)) draft.timesPerDay = 2;
            if ("3 раза в день".equals(draft.schedule)) draft.timesPerDay = 3;

            // ✅ расписание настроено один раз
            draft.scheduleConfigured = true;

            ((AddMedicationActivity) requireActivity())
                    .openFragment(TimeStepFragment.newInstance(draft));
        });

        return v;
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            String it = String.valueOf(spinner.getItemAtPosition(i));
            if (value.equalsIgnoreCase(it)) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}
