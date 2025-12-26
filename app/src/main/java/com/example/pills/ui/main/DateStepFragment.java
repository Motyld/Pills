package com.example.pills.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;

import java.util.Calendar;

public class DateStepFragment extends Fragment {

    private static final String ARG_DRAFT = "draft";

    private ReminderDraft draft;

    public DateStepFragment() {}

    public static DateStepFragment newInstance(ReminderDraft d) {
        DateStepFragment f = new DateStepFragment();
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

        View v = inflater.inflate(R.layout.fragment_step_date, container, false);

        draft = (ReminderDraft) requireArguments().getSerializable(ARG_DRAFT);
        if (draft == null) draft = new ReminderDraft();

        DatePicker dp = v.findViewById(R.id.dpStart);
        Button next = v.findViewById(R.id.btnNext);

        // ✅ если уже выбрано ранее — выставим
        if (draft.startYear > 0) {
            dp.updateDate(draft.startYear, draft.startMonth, draft.startDay);
        } else {
            Calendar now = Calendar.getInstance();
            dp.updateDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        }

        next.setOnClickListener(v1 -> {
            draft.startDay = dp.getDayOfMonth();
            draft.startMonth = dp.getMonth();
            draft.startYear = dp.getYear();

            ((NavigationHost) requireActivity())
                    .openFragment(ItemsStepFragment.newInstance(draft)); // или SaveStepFragment, как у тебя по логике
        });

        return v;
    }
}
