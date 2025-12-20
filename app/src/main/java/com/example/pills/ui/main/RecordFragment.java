package com.example.pills.ui.main;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pills.db.DatabaseHelper;
import com.example.pills.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseHelper db;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.rvHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new DatabaseHelper(getContext());

        loadHistory();
    }

    private void loadHistory() {
        ArrayList<HistoryRecord> records = db.getAllHistorySortedByDate();

        ArrayList<HistoryItem> finalList = new ArrayList<>();
        String lastDate = "";

        for (HistoryRecord r : records) {
            // ИСПРАВЛЕНО: используем правильные имена полей
            if (!r.date.equals(lastDate)) {
                lastDate = r.date;
                String title = formatDate(r.date);
                finalList.add(new HistoryItem(title));
            }

            // ИСПРАВЛЕНО: drugName вместо name
            finalList.add(new HistoryItem(r.drugName, r.time, r.status));
        }

        adapter = new HistoryAdapter(finalList);
        recyclerView.setAdapter(adapter);
    }

    private String formatDate(String yyyyMMdd) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = in.parse(yyyyMMdd);

            Calendar c = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            c.setTime(d);

            if (isSameDay(c, today)) return "Сегодня";

            today.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(c, today)) return "Вчера";

            SimpleDateFormat out = new SimpleDateFormat("d MMMM", new Locale("ru"));
            return out.format(d);

        } catch (Exception e) {
            return yyyyMMdd;
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
