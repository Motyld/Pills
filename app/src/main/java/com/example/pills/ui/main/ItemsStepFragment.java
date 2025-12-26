package com.example.pills.ui.main;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.notifications.NotificationScheduler;

import java.util.Calendar;
import java.util.Locale;

public class ItemsStepFragment extends Fragment {

    private static final String ARG_DRAFT = "draft";

    public static final String REQ_PICK_MED = "pick_med";
    public static final String KEY_MED_NAME = "name";

    private ReminderDraft draft;
    private ItemsAdapter adapter;

    public ItemsStepFragment() {}

    public static ItemsStepFragment newInstance(ReminderDraft draft) {
        ItemsStepFragment f = new ItemsStepFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DRAFT, draft);
        f.setArguments(b);
        return f;
    }

    public static ItemsStepFragment newInstanceEdit(long reminderId) {
        ReminderDraft d = new ReminderDraft();
        d.editMode = true;
        d.editReminderId = reminderId;

        ItemsStepFragment f = new ItemsStepFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DRAFT, d);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_step_items, container, false);

        ReminderDraft d0 = (ReminderDraft) requireArguments().getSerializable(ARG_DRAFT);
        draft = (d0 == null) ? new ReminderDraft() : d0;

        RecyclerView rv = v.findViewById(R.id.rvItems);
        Button btnAdd = v.findViewById(R.id.btnAddMedicine);
        final Button btnAction = v.findViewById(R.id.btnNext);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ItemsAdapter(draft);
        rv.setAdapter(adapter);

        if (draft.editMode) {
            loadDraftForEdit(draft.editReminderId);
            draft.scheduleConfigured = true; // редактирование = расписание уже есть
        }

        // подпись кнопки
        btnAction.setText(draft.scheduleConfigured ? "Сохранить" : "Дальше");

        getParentFragmentManager().setFragmentResultListener(
                REQ_PICK_MED,
                this,
                (requestKey, bundle) -> {
                    String name = bundle.getString(KEY_MED_NAME, "");
                    if (TextUtils.isEmpty(name)) return;

                    ((AddMedicationActivity) requireActivity())
                            .openFragment(DoseStepFragment.newInstance(draft, name));
                }
        );

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AddMedicationActivity) requireActivity())
                        .openFragment(SearchMedicineFragment.newInstancePickOnly());
            }
        });

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (draft.items == null || draft.items.isEmpty()) {
                    Toast.makeText(requireContext(), "Добавьте хотя бы одно лекарство", Toast.LENGTH_SHORT).show();
                    return;
                }

                // если расписание еще не настроено — идём на шаг расписания
                if (!draft.scheduleConfigured) {
                    ((AddMedicationActivity) requireActivity())
                            .openFragment(ScheduleStepFragment.newInstance(draft));
                    return;
                }

                // иначе — сохраняем всё в БД
                saveAll();
            }
        });

        return v;
    }

    private void loadDraftForEdit(long reminderId) {
        DatabaseHelper db = new DatabaseHelper(requireContext());

        Cursor c = db.getReminderByIdFull(reminderId);
        if (c != null) {
            if (c.moveToFirst()) {
                long ts = c.getLong(c.getColumnIndexOrThrow("timestamp"));
                draft.schedule = c.getString(c.getColumnIndexOrThrow("schedule"));

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(ts);

                draft.startYear = cal.get(Calendar.YEAR);
                draft.startMonth = cal.get(Calendar.MONTH);
                draft.startDay = cal.get(Calendar.DAY_OF_MONTH);
                draft.startHour = cal.get(Calendar.HOUR_OF_DAY);
                draft.startMinute = cal.get(Calendar.MINUTE);
            }
            c.close();
        }

        draft.items.clear();
        Cursor ci = db.getReminderItemsForReminder(reminderId);
        if (ci != null) {
            while (ci.moveToNext()) {
                long drugId = ci.getLong(ci.getColumnIndexOrThrow("drug_id"));
                String name = ci.getString(ci.getColumnIndexOrThrow("name"));
                String dose = ci.getString(ci.getColumnIndexOrThrow("dose"));

                int courseDays = 30;
                try {
                    courseDays = ci.getInt(ci.getColumnIndexOrThrow("course_days"));
                } catch (Exception ignored) {}

                String val = "";
                String unit = "мг";
                if (dose != null && dose.contains(" ")) {
                    String[] p = dose.split(" ", 2);
                    val = p[0];
                    unit = p[1];
                } else if (dose != null && !dose.trim().isEmpty()) {
                    val = dose.trim();
                }

                draft.items.add(new ReminderDraft.Item(drugId, name, val, unit, courseDays));
            }
            ci.close();
        }

        db.close();
        adapter.notifyDataSetChanged();
    }

    private String buildTemplateTitle() {
        if (draft.items == null || draft.items.isEmpty()) return "Курс";
        if (draft.items.size() == 1) return "Курс: " + draft.items.get(0).drugName;
        return "Курс: " + draft.items.get(0).drugName + " + ещё " + (draft.items.size() - 1);
    }

    private void saveAll() {
        // проверки дозы
        for (ReminderDraft.Item it : draft.items) {
            if (it == null) continue;
            if (TextUtils.isEmpty(it.drugName)) {
                Toast.makeText(requireContext(), "Ошибка: пустое лекарство", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(it.doseValue) || TextUtils.isEmpty(it.doseUnit)) {
                Toast.makeText(requireContext(), "Заполните дозировку для: " + it.drugName, Toast.LENGTH_SHORT).show();
                return;
            }
            if (it.courseDays <= 0) it.courseDays = 30;
        }

        // базовая дата/время
        Calendar base = Calendar.getInstance();
        base.set(draft.startYear, draft.startMonth, draft.startDay, draft.startHour, draft.startMinute, 0);
        base.set(Calendar.MILLISECOND, 0);

        String days = "[1,2,3,4,5,6,7]";

        // максимальный курс среди лекарств
        int maxDays = 1;
        for (ReminderDraft.Item it : draft.items) {
            if (it != null && it.courseDays > maxDays) maxDays = it.courseDays;
        }

        DatabaseHelper db = new DatabaseHelper(requireContext());

        // редактирование одного события
        if (draft.editMode && draft.editReminderId != -1) {
            long reminderId = draft.editReminderId;

            long ts = base.getTimeInMillis();
            String timeText = String.format(Locale.getDefault(), "%02d:%02d",
                    base.get(Calendar.HOUR_OF_DAY), base.get(Calendar.MINUTE));

            db.updateReminderTime(reminderId, ts, timeText);
            db.updateReminderSchedule(reminderId, draft.schedule);

            db.deleteReminderItems(reminderId);
            for (ReminderDraft.Item it : draft.items) {
                if (it == null) continue;
                db.addReminderItem(reminderId, it.drugId, it.getDoseText(), it.courseDays);
            }

            db.close();
            Toast.makeText(requireContext(), "Сохранено", Toast.LENGTH_SHORT).show();
            DatabaseHelper dbT = new DatabaseHelper(requireContext());
            dbT.saveTemplateFromDraft(buildTemplateTitle(), draft);
            dbT.close();
            requireActivity().finish();
            return;
        }

        // создание: события на каждый день/приём, и в каждый день кладём только активные лекарства
        int created = 0;

        for (int dayOffset = 0; dayOffset < maxDays; dayOffset++) {
            for (int occ = 0; occ < draft.timesPerDay; occ++) {

                Calendar cal = (Calendar) base.clone();
                cal.add(Calendar.DAY_OF_MONTH, dayOffset);

                if (draft.timesPerDay > 1) {
                    cal.add(Calendar.MINUTE, occ * draft.repeatMinutes);
                }

                long ts = cal.getTimeInMillis();
                String timeText = String.format(Locale.getDefault(), "%02d:%02d",
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

                long reminderId = db.insertReminderEvent(ts, timeText, days, draft.schedule);

                for (ReminderDraft.Item it : draft.items) {
                    if (it == null) continue;
                    if (dayOffset < it.courseDays) {
                        db.addReminderItem(reminderId, it.drugId, it.getDoseText(), it.courseDays);
                    }
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

        Toast.makeText(requireContext(), "✅ Создано " + created + " уведомлений", Toast.LENGTH_LONG).show();
        DatabaseHelper dbT = new DatabaseHelper(requireContext());
        dbT.saveTemplateFromDraft(buildTemplateTitle(), draft);
        dbT.close();
        requireActivity().finish();
    }
}
