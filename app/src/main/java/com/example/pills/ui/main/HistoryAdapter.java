package com.example.pills.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<HistoryItem> list;

    public HistoryAdapter(ArrayList<HistoryItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        HistoryItem item = list.get(pos);

        h.name.setText(item.name);
        h.time.setText(item.time);

        if ("taken".equals(item.status)) {
            h.status.setText("✓ Принято");
            h.status.setTextColor(0xFF4CAF50);
        } else {
            h.status.setText("✗ Пропущено");
            h.status.setTextColor(0xFFF44336);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, time, status;

        public ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.historyName);
            time = v.findViewById(R.id.historyTime);
            status = v.findViewById(R.id.historyStatus);
        }
    }
}
