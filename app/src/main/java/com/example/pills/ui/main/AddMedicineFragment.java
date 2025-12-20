package com.example.pills.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.pills.R;

import java.util.ArrayList;
import java.util.Arrays;

public class AddMedicineFragment extends Fragment {

    private EditText etSearch;
    private ListView lvResults;

    private final ArrayList<String> medicines = new ArrayList<>(Arrays.asList(
            "Парацетамол", "Ибупрофен", "Амоксициллин", "Цефтриаксон", "Аспирин",
            "Метформин", "Лизиноприл", "Аторвастатин", "Фуросемид", "Омепразол",
            "Амлодипин", "Ципрофлоксацин", "Кларитромицин", "Диклофенак", "Витамин C",
            "Пантопразол", "Глицерол", "Лоперамид", "Цетиризин", "Монтелукаст"
    ));
    private ArrayAdapter<String> adapter;

    public AddMedicineFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_add_medicine, container, false);

        etSearch = v.findViewById(R.id.etSearch);
        lvResults = v.findViewById(R.id.lvResults);

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, medicines);
        lvResults.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        lvResults.setOnItemClickListener((parent, view, position, id) -> {
            String selected = adapter.getItem(position);
            if (selected == null) return;

            FragmentTransaction ft = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            ft.replace(R.id.add_med_container, FormFragment.newInstance(selected));
            ft.addToBackStack(null);
            ft.commit();
        });

        return v;
    }
}
