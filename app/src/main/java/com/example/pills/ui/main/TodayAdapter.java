package com.example.pills.ui.main;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.util.ArrayList;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.VH> {

    private final ArrayList<TodayItem> items;
    private final DatabaseHelper db;

    public TodayAdapter(ArrayList<TodayItem> items, DatabaseHelper db) {
        this.items = items;
        this.db = db;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.today_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TodayItem it = items.get(pos);

        h.title.setText(it.name);
        h.dosage.setText(it.dosage);
        h.time.setText(it.time);

        h.btnTaken.setOnClickListener(v -> updateStatus(it, pos, "taken"));
        h.btnMissed.setOnClickListener(v -> updateStatus(it, pos, "missed"));
    }

    private void updateStatus(TodayItem item, int position, String status) {
        SQLiteDatabase wdb = db.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("status", status);

        // Обновляем по id напоминания (самый безопасный вариант)
        wdb.update("reminders", cv, "id = ?", new String[]{String.valueOf(item.reminderId)});

        // удалим из списка и уведомим адаптер
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, dosage, time;
        ImageView btnTaken, btnMissed;

        public VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.medTitle);
            dosage = v.findViewById(R.id.medDosage);
            time = v.findViewById(R.id.medTime);
            btnTaken = v.findViewById(R.id.btnTaken);
            btnMissed = v.findViewById(R.id.btnMissed);
        }
    }
}
