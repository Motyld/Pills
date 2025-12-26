package com.example.pills.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchMedicineFragment extends Fragment {

    private static final String ARG_PICK_ONLY = "pick_only"; // ✅ важно

    public static SearchMedicineFragment newInstancePickOnly() {
        SearchMedicineFragment f = new SearchMedicineFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_PICK_ONLY, true);
        f.setArguments(b);
        return f;
    }

    private AutoCompleteTextView actMedicine;
    private Button btnAdd;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper db;

    public SearchMedicineFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search_medicine, container, false);

        actMedicine = v.findViewById(R.id.actMedicine);
        btnAdd = v.findViewById(R.id.btnAddMedicine);
        db = new DatabaseHelper(requireContext());

        setupDropdown(db.getAllDrugNames());

        boolean pickOnly = getArguments() != null && getArguments().getBoolean(ARG_PICK_ONLY, false);

        // ✅ если сохранили лекарство в MedicineEditorFragment
        getParentFragmentManager().setFragmentResultListener(
                "medicine_saved",
                this,
                (requestKey, bundle) -> {
                    String savedName = bundle.getString("name", "");
                    setupDropdown(db.getAllDrugNames());
                    actMedicine.setText(savedName, false);
                    btnAdd.setVisibility(View.GONE);

                    if (savedName != null && !savedName.trim().isEmpty()) {
                        handlePicked(savedName.trim(), pickOnly);
                    }
                }
        );

        // ✅ Выбрали лекарство
        actMedicine.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if (selected != null) selected = selected.trim();
            if (selected == null || selected.isEmpty()) return;

            handlePicked(selected, pickOnly);
        });

        // ✅ Добавить новое лекарство -> открыть редактор
        btnAdd.setOnClickListener(view -> {
            String typed = actMedicine.getText().toString().trim();
            MedicineEditorFragment fragment = MedicineEditorFragment.newInstanceCreateOpenWizard(typed);
            ((AddMedicationActivity) requireActivity()).openFragment(fragment);
        });

        // поиск + логика кнопки "добавить"
        actMedicine.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    btnAdd.setVisibility(View.GONE);
                    setupDropdown(db.getAllDrugNames());
                    return;
                }

                List<String> filtered = db.searchDrugNames(query);
                adapter.clear();
                adapter.addAll(filtered);
                adapter.notifyDataSetChanged();

                btnAdd.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
                actMedicine.post(() -> actMedicine.showDropDown());
            }
        });

        actMedicine.setOnClickListener(view -> actMedicine.showDropDown());

        return v;
    }

    private void handlePicked(String medName, boolean pickOnly) {
        // 1) отправляем результат в ItemsStepFragment
        Bundle b = new Bundle();
        b.putString(ItemsStepFragment.KEY_MED_NAME, medName);
        getParentFragmentManager().setFragmentResult(ItemsStepFragment.REQ_PICK_MED, b);

        if (pickOnly) {
            // ✅ мы пришли из ItemsStepFragment -> просто вернуться назад
            getParentFragmentManager().popBackStack();
            return;
        }

        // ✅ первый выбор (старт) -> открыть ItemsStepFragment с новым draft
        ReminderDraft d = new ReminderDraft();
        d.editMode = false;

        ((AddMedicationActivity) requireActivity())
                .openFragment(ItemsStepFragment.newInstance(d));
    }

    private void setupDropdown(List<String> names) {
        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(names));
        actMedicine.setAdapter(adapter);
        actMedicine.setThreshold(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) db.close();
    }
}
