package com.example.pills.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;

public class DoseFragment extends Fragment {

    private static final String ARG_MED_NAME = "med_name";
    private static final String ARG_FORM = "form";
    private static final String ARG_SCHEDULE = "schedule";
    private static final String ARG_TIME = "time";
    private static final String ARG_START_DATE = "start_date";

    private String medName, form, schedule, time, startDate;

    public static DoseFragment newInstance(String medName, String form, String schedule, String time, String startDate) {
        DoseFragment f = new DoseFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MED_NAME, medName);
        b.putString(ARG_FORM, form);
        b.putString(ARG_SCHEDULE, schedule);
        b.putString(ARG_TIME, time);
        b.putString(ARG_START_DATE, startDate);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dose, container, false);

        if (getArguments() != null) {
            medName = getArguments().getString(ARG_MED_NAME);
            form = getArguments().getString(ARG_FORM);
            schedule = getArguments().getString(ARG_SCHEDULE);
            time = getArguments().getString(ARG_TIME);
            startDate = getArguments().getString(ARG_START_DATE);
        }

        EditText etDose = v.findViewById(R.id.etDose);
        Button btnSave = v.findViewById(R.id.btnSave);

        btnSave.setOnClickListener(view -> {
            String dose = etDose.getText().toString();
            // Сюда можно добавить сохранение в БД
            // Пример: сохраняем medName, form, schedule, time, startDate, dose
        });

        return v;
    }
}
