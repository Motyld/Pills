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

public class RecordFragment extends Fragment {

    private RecyclerView rvRecord;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_record, container, false);

        rvRecord = v.findViewById(R.id.rvRecord);
        rvRecord.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new DatabaseHelper(requireContext());

        loadHistory();

        return v;
    }

    private void loadHistory() {

        ArrayList<HistoryItem> list = new ArrayList<>();

        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT drugs.name, reminders.time, reminders.status " +
                        "FROM reminders " +
                        "JOIN drugs ON drugs.id = reminders.drug_id " +
                        "WHERE reminders.status IN ('taken','missed') " +
                        "ORDER BY reminders.time ASC", null);

        while (c.moveToNext()) {
            list.add(new HistoryItem(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)
            ));
        }
        c.close();

        rvRecord.setAdapter(new HistoryAdapter(list));
    }
}
