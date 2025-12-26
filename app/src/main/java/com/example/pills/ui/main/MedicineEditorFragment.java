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

public class MedicineEditorFragment extends Fragment {

    private static final String ARG_EDIT_NAME = "arg_edit_name";
    private static final String ARG_PREFILL_NAME = "arg_prefill_name";
    private static final String ARG_AFTER_SAVE = "arg_after_save";

    private static final String AFTER_SAVE_BACK = "back";
    private static final String AFTER_SAVE_OPEN_WIZARD = "open_wizard";

    private EditText etName, etDescription, etCountry, etManufacturer;
    private Spinner spForm, spType, spCourse;
    private Button btnSave;

    private DatabaseHelper db;

    private long editingDrugId = -1;
    private String oldName = null;

    private String afterSaveAction = AFTER_SAVE_OPEN_WIZARD;

    public MedicineEditorFragment() {}

    public static MedicineEditorFragment newInstanceEditBack(String medName) {
        MedicineEditorFragment f = new MedicineEditorFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EDIT_NAME, medName);
        b.putString(ARG_AFTER_SAVE, AFTER_SAVE_BACK);
        f.setArguments(b);
        return f;
    }

    public static MedicineEditorFragment newInstanceCreateOpenWizard(String prefillName) {
        MedicineEditorFragment f = new MedicineEditorFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PREFILL_NAME, prefillName);
        b.putString(ARG_AFTER_SAVE, AFTER_SAVE_OPEN_WIZARD);
        f.setArguments(b);
        return f;
    }

    public static MedicineEditorFragment newInstanceEditOpenWizard(String medName) {
        MedicineEditorFragment f = new MedicineEditorFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EDIT_NAME, medName);
        b.putString(ARG_AFTER_SAVE, AFTER_SAVE_OPEN_WIZARD);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_medicine_editor, container, false);

        db = new DatabaseHelper(requireContext());

        etName = v.findViewById(R.id.etName);
        etDescription = v.findViewById(R.id.etDescription);
        etCountry = v.findViewById(R.id.etCountry);
        etManufacturer = v.findViewById(R.id.etManufacturer);

        spForm = v.findViewById(R.id.spForm);
        spType = v.findViewById(R.id.spType);
        spCourse = v.findViewById(R.id.spCourse);

        btnSave = v.findViewById(R.id.btnSave);

        setupSpinners();
        readArgsAndFill();

        btnSave.setOnClickListener(view -> saveDrug());

        return v;
    }

    private void setupSpinners() {
        List<String> forms = Arrays.asList("Таблетки", "Капсулы", "Сироп", "Инъекции", "Капли", "Мазь", "Другое");
        List<String> types = Arrays.asList("Без рецепта", "Рецептурное", "Витамины", "БАД", "Другое");
        List<String> courses = Arrays.asList("1 день", "3 дня", "5 дней", "7 дней", "10 дней", "14 дней", "21 день", "30 дней");

        spForm.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, forms));
        spType.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, types));
        spCourse.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, courses));
    }

    private void readArgsAndFill() {
        Bundle args = getArguments();
        if (args == null) return;

        afterSaveAction = args.getString(ARG_AFTER_SAVE, AFTER_SAVE_OPEN_WIZARD);

        String editName = args.getString(ARG_EDIT_NAME, null);
        String prefillName = args.getString(ARG_PREFILL_NAME, "");

        if (!TextUtils.isEmpty(editName)) {
            DatabaseHelper.DrugData d = db.getDrugDataByName(editName);
            if (d != null) {
                editingDrugId = d.id;
                oldName = d.name;

                etName.setText(d.name);
                etDescription.setText(d.description);
                etCountry.setText(d.country);
                etManufacturer.setText(d.manufacturer);

                setSpinnerValue(spForm, d.form);
                setSpinnerValue(spType, d.drugType);
                setSpinnerValue(spCourse, d.course);
            }
        } else if (!TextUtils.isEmpty(prefillName)) {
            etName.setText(prefillName);
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equalsIgnoreCase(String.valueOf(spinner.getItemAtPosition(i)))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void saveDrug() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper.DrugData d = new DatabaseHelper.DrugData();
        d.name = name;
        d.description = etDescription.getText().toString().trim();
        d.country = etCountry.getText().toString().trim();
        d.manufacturer = etManufacturer.getText().toString().trim();
        d.form = String.valueOf(spForm.getSelectedItem());
        d.drugType = String.valueOf(spType.getSelectedItem());
        d.course = String.valueOf(spCourse.getSelectedItem());

        if (editingDrugId != -1) {
            db.updateDrugFull(editingDrugId, d);

            if (oldName != null && !oldName.equals(name)) {
                db.updateRemindersDrugName(editingDrugId, name);
            }
        } else {
            long existingId = db.findDrugByName(name);
            if (existingId != -1) {
                db.updateDrugFull(existingId, d);
            } else {
                db.insertDrugFull(d);
            }
        }

        Toast.makeText(requireContext(), "Сохранено", Toast.LENGTH_SHORT).show();

        // ✅ Поведение после сохранения
        if (AFTER_SAVE_BACK.equals(afterSaveAction)) {
            requireActivity().onBackPressed();
            return;
        }

        // ✅ сценарий создания уведомления:
        // после сохранения лекарства — сразу открываем ШАГ ДОЗИРОВКИ
        ReminderDraft draft = new ReminderDraft();

        ((AddMedicationActivity) requireActivity())
                .openFragment(DoseStepFragment.newInstance(draft, name));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) db.close();
    }
}
