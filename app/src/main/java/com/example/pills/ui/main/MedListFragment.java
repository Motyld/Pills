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

        ArrayList<MedicationItem> list = new ArrayList<>();

        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT id, name, dosage FROM drugs",
                null
        );

        while (c.moveToNext()) {
            list.add(new MedicationItem(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2)
            ));
        }
        c.close();

        rvMeds.setAdapter(new MedicationAdapter(list));
    }
}
