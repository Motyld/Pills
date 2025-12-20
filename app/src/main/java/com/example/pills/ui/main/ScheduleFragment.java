package com.example.pills.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;

public class ScheduleFragment extends Fragment {

    private static final String ARG_MEDICINE_NAME = "medicine_name";
    private static final String ARG_FORM = "form";
    private static final String ARG_SCHEDULE = "schedule";
    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";
    private static final String ARG_DAY = "day";
    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";
    private static final String ARG_DOSE = "dose";

    public static ScheduleFragment newInstance(String medName, String form, String schedule,
                                               int hour, int minute, int day, int month, int year, String dose) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEDICINE_NAME, medName);
        args.putString(ARG_FORM, form);
        args.putString(ARG_SCHEDULE, schedule);
        args.putInt(ARG_HOUR, hour);
        args.putInt(ARG_MINUTE, minute);
        args.putInt(ARG_DAY, day);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_YEAR, year);
        args.putString(ARG_DOSE, dose);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_schedule, container, false);

        TextView tvSummary = v.findViewById(R.id.tvSummary);

        if (getArguments() != null) {
            String medName = getArguments().getString(ARG_MEDICINE_NAME);
            String form = getArguments().getString(ARG_FORM);
            String schedule = getArguments().getString(ARG_SCHEDULE);
            int hour = getArguments().getInt(ARG_HOUR);
            int minute = getArguments().getInt(ARG_MINUTE);
            int day = getArguments().getInt(ARG_DAY);
            int month = getArguments().getInt(ARG_MONTH);
            int year = getArguments().getInt(ARG_YEAR);
            String dose = getArguments().getString(ARG_DOSE);

            String summary = "Лекарство: " + medName +
                    "\nФорма: " + form +
                    "\nРасписание: " + schedule +
                    "\nВремя начала: " + hour + ":" + minute +
                    "\nДата начала: " + day + "/" + (month + 1) + "/" + year +
                    "\nДоза: " + dose;

            tvSummary.setText(summary);
        }

        return v;
    }
}
