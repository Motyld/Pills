package com.example.pills.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pills.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchMedicineFragment extends Fragment {

    private EditText etSearch;
    private ListView lvResults;
    private List<String> medicines;

    public SearchMedicineFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search_medicine, container, false);
        etSearch = v.findViewById(R.id.etSearch);
        lvResults = v.findViewById(R.id.lvResults);

        medicines = new ArrayList<>(Arrays.asList(
                "Аспирин", "Парацетамол", "Ибупрофен", "Амоксициллин", "Цитрамон"
        ));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, medicines);
        lvResults.setAdapter(adapter);

        // Фильтр поиска
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                List<String> filtered = new ArrayList<>();
                for (String med : medicines) {
                    if (med.toLowerCase().contains(query)) filtered.add(med);
                }
                lvResults.setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_list_item_1, filtered));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Выбор лекарства
        lvResults.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String selectedMed = (String) parent.getItemAtPosition(position);

            // Переход на FormFragment
            FormFragment fragment = FormFragment.newInstance(selectedMed);
            ((AddMedicationActivity) requireActivity())
                    .openFragment(fragment);
        });

        return v;
    }
}
