/*package com.example.pills.ui.main;

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

import java.util.Arrays;

public class DrugFormStepFragment extends Fragment {

    private static final String ARG_DRAFT = "draft";

    public static DrugFormStepFragment newInstance(ReminderDraft d) {
        DrugFormStepFragment f = new DrugFormStepFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DRAFT, d);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_step_drug_form, container, false);

        ReminderDraft d = (ReminderDraft) requireArguments().getSerializable(ARG_DRAFT);

        Spinner sp = v.findViewById(R.id.spForm);
        Button next = v.findViewById(R.id.btnNext);

        sp.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Таблетка", "Капсула", "Жидкое", "Порошок", "Сироп")
        ));

        next.setOnClickListener(view -> {
            d.form = sp.getSelectedItem().toString();
            ((AddMedicationActivity) requireActivity())
                    .openFragment(DoseStepFragment.newInstance(d));
        });

        return v;
    }
}*/
