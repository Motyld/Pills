package com.example.pills.ui.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DoseStepFragment extends Fragment {

    private static final String ARG_DRAFT = "draft";
    private static final String ARG_NAME = "name";

    private ReminderDraft draft;
    private String drugName;

    public DoseStepFragment() {}

    public static DoseStepFragment newInstance(ReminderDraft d, String drugName) {
        DoseStepFragment f = new DoseStepFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DRAFT, d);
        b.putString(ARG_NAME, drugName);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_step_dose, container, false);

        ReminderDraft d0 = (ReminderDraft) requireArguments().getSerializable(ARG_DRAFT);
        draft = (d0 == null) ? new ReminderDraft() : d0;

        drugName = requireArguments().getString(ARG_NAME, "");

        final EditText etDose = v.findViewById(R.id.etDoseValue);
        final Spinner spUnit = v.findViewById(R.id.spDoseUnit);
        final Spinner spCourse = v.findViewById(R.id.spCourseDays);
        final Button btnSave = v.findViewById(R.id.btnSaveDose);

        List<String> units = Arrays.asList("мг", "г", "мл", "капли", "таб", "капс");
        spUnit.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, units));

        List<String> courses = Arrays.asList("1", "3", "5", "7", "10", "14", "21", "30", "60", "90");
        spCourse.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, courses));

        // drugId + дефолтный курс из drugs.course
        DatabaseHelper db = new DatabaseHelper(requireContext());
        long tmpDrugId = db.findDrugByName(drugName);
        if (tmpDrugId == -1) tmpDrugId = db.insertDrugIfMissing(drugName);

        int defaultCourseDays = parseCourseDays(db.getDrugCourseByName(drugName), 30);
        db.close();

        final long drugId = tmpDrugId;

        // предзаполнение
        ReminderDraft.Item existing = findItemByDrugId(drugId);
        if (existing != null) {
            if (!TextUtils.isEmpty(existing.doseValue)) etDose.setText(existing.doseValue);
            setSpinnerValue(spUnit, existing.doseUnit);
            setSpinnerValue(spCourse, String.valueOf(existing.courseDays));
        } else {
            setSpinnerValue(spCourse, String.valueOf(defaultCourseDays));
            setSpinnerValue(spUnit, "мг");
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String val = etDose.getText().toString().trim();
                String unit = String.valueOf(spUnit.getSelectedItem());
                String courseStr = String.valueOf(spCourse.getSelectedItem());

                if (TextUtils.isEmpty(val)) {
                    Toast.makeText(requireContext(), "Введите дозировку", Toast.LENGTH_SHORT).show();
                    return;
                }

                int courseDays;
                try {
                    courseDays = Integer.parseInt(courseStr);
                } catch (Exception e) {
                    courseDays = 30;
                }
                if (courseDays <= 0) courseDays = 30;

                ReminderDraft.Item it = findItemByDrugId(drugId);
                if (it == null) {
                    it = new ReminderDraft.Item(drugId, drugName, val, unit, courseDays);
                    draft.items.add(it);
                } else {
                    it.doseValue = val;
                    it.doseUnit = unit;
                    it.courseDays = courseDays;
                }

                // ✅ ВАЖНО: если расписание ещё не настроено — идём на выбор расписания
                if (!draft.scheduleConfigured) {
                    ((AddMedicationActivity) requireActivity())
                            .openFragment(ScheduleStepFragment.newInstance(draft));
                } else {
                    ((AddMedicationActivity) requireActivity())
                            .openFragment(ItemsStepFragment.newInstance(draft));
                }
            }
        });

        return v;
    }

    private ReminderDraft.Item findItemByDrugId(long drugId) {
        for (ReminderDraft.Item it : draft.items) {
            if (it != null && it.drugId == drugId) return it;
        }
        return null;
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

    private int parseCourseDays(String s, int def) {
        if (s == null) return def;
        s = s.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return def;

        String digits = s.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return def;

        try {
            int v = Integer.parseInt(digits);
            return v > 0 ? v : def;
        } catch (Exception e) {
            return def;
        }
    }
}
