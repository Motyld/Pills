package com.example.pills.ui.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;

import java.util.Calendar;

public class TimeStepFragment extends Fragment {

    private static final String ARG_DRAFT = "draft";

    private ReminderDraft draft;

    public TimeStepFragment() {}

    public static TimeStepFragment newInstance(ReminderDraft d) {
        TimeStepFragment f = new TimeStepFragment();
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

        View v = inflater.inflate(R.layout.fragment_step_time, container, false);

        draft = (ReminderDraft) requireArguments().getSerializable(ARG_DRAFT);
        if (draft == null) draft = new ReminderDraft();

        TimePicker tp = v.findViewById(R.id.tpStart);
        TextView label = v.findViewById(R.id.tvRepeatLabel);
        EditText etRepeat = v.findViewById(R.id.etRepeatMinutes);
        Button next = v.findViewById(R.id.btnNext);

        // ✅ 24-часовой формат
        tp.setIs24HourView(true);
        tp.post(() -> tp.setIs24HourView(true));

        // ✅ Время по умолчанию
        if (draft.startHour >= 0 && draft.startMinute >= 0) {
            tp.setHour(draft.startHour);
            tp.setMinute(draft.startMinute);
        } else {
            Calendar now = Calendar.getInstance();
            tp.setHour(now.get(Calendar.HOUR_OF_DAY));
            tp.setMinute(now.get(Calendar.MINUTE));
        }

        // ✅ Интервал повтора (если >1 раз в день)
        if (draft.timesPerDay > 1) {
            label.setVisibility(View.VISIBLE);
            etRepeat.setVisibility(View.VISIBLE);

            if (draft.repeatMinutes > 0) {
                etRepeat.setText(String.valueOf(draft.repeatMinutes));
            } else {
                etRepeat.setText(draft.timesPerDay == 2 ? "720" : "480");
            }
        } else {
            label.setVisibility(View.GONE);
            etRepeat.setVisibility(View.GONE);
            draft.repeatMinutes = 0;
        }

        next.setOnClickListener(v1 -> {

            draft.startHour = tp.getHour();
            draft.startMinute = tp.getMinute();

            if (draft.timesPerDay > 1) {
                String txt = etRepeat.getText().toString().trim();

                if (TextUtils.isEmpty(txt)) {
                    Toast.makeText(requireContext(),
                            "Введите интервал повтора в минутах",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    draft.repeatMinutes = Integer.parseInt(txt);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(),
                            "Введите число (например 480)",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (draft.repeatMinutes <= 0) {
                    Toast.makeText(requireContext(),
                            "Интервал должен быть больше 0",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                draft.repeatMinutes = 0;
            }

            // ✅ важно для новой логики
            draft.scheduleConfigured = true;

            // ✅ дальше выбор даты
            ((NavigationHost) requireActivity())
                    .openFragment(DateStepFragment.newInstance(draft));
        });

        return v;
    }
}
