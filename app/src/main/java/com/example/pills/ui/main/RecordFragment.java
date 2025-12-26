package com.example.pills.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.util.ArrayList;

public class RecordFragment extends Fragment {

    private RecyclerView rv;
    private HistoryAdapter adapter;
    private final ArrayList<HistoryItem> uiList = new ArrayList<>();

    public RecordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_record, container, false);

        Button btnTemplates = v.findViewById(R.id.btnTemplates);

        rv = v.findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HistoryAdapter(uiList);
        rv.setAdapter(adapter);

        btnTemplates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new TemplatesFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadHistory();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        ArrayList<HistoryRecord> records = db.getAllHistorySortedByDate();
        db.close();

        uiList.clear();

        String lastDate = null;

        for (HistoryRecord r : records) {

            // Заголовок по дате
            if (lastDate == null || !lastDate.equals(r.date)) {
                lastDate = r.date;
                uiList.add(new HistoryItem(formatDateTitle(lastDate)));
            }

            String planned = (r.plannedTime == null || r.plannedTime.isEmpty())
                    ? ""
                    : ("План: " + r.plannedTime);

            String action = (r.actionTime == null || r.actionTime.isEmpty())
                    ? ""
                    : ("Факт: " + r.actionTime);

            String status = r.status == null ? "" : r.status;
            String meds = r.medicines == null ? "" : r.medicines;

            uiList.add(new HistoryItem(planned, action, status, meds));
        }

        adapter.notifyDataSetChanged();
    }

    private String formatDateTitle(String date) {
        if (date == null) return "";
        return date; // можно потом красиво: 26.12.2025
    }
}
