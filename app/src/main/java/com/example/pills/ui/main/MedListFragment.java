package com.example.pills.ui.main;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.util.ArrayList;

public class MedListFragment extends Fragment {

    private RecyclerView rvMeds;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_med_list, container, false);

        rvMeds = v.findViewById(R.id.rvMedications);
        rvMeds.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new DatabaseHelper(requireContext());

        loadMedications();

        return v;
    }

    private void loadMedications() {
        ArrayList<String> list = new ArrayList<>();

        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT name FROM drugs ORDER BY name ASC",
                null
        );

        while (c.moveToNext()) {
            list.add(c.getString(0));
        }
        c.close();

        MedicineAdapter adapter = new MedicineAdapter(list, name -> {
            MedicineEditorFragment f = MedicineEditorFragment.newInstanceEditBack(name);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, f) // ✅ контейнер MainActivity
                    .addToBackStack(null)
                    .commit();
        });

        rvMeds.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) db.close();
    }
}
