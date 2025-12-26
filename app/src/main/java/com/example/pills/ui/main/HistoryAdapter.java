package com.example.pills.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<HistoryItem> list;

    public HistoryAdapter(ArrayList<HistoryItem> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == HistoryItem.TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_history, parent, false);
            return new HeaderVH(v);
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        HistoryItem item = list.get(position);

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).title.setText(item.title);
            return;
        }

        ItemVH vh = (ItemVH) holder;

        vh.plannedTime.setText(item.plannedTime == null ? "" : item.plannedTime);
        vh.actionTime.setText(item.actionTime == null ? "" : item.actionTime);
        vh.status.setText(item.status == null ? "" : item.status);
        vh.medicines.setText(item.medicines == null ? "" : item.medicines);
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView title;
        HeaderVH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.headerTitle);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView plannedTime, actionTime, status, medicines;

        ItemVH(View itemView) {
            super(itemView);
            plannedTime = itemView.findViewById(R.id.historyPlannedTime);
            actionTime  = itemView.findViewById(R.id.historyActionTime);
            status      = itemView.findViewById(R.id.historyStatus);
            medicines   = itemView.findViewById(R.id.historyMedicines);
        }
    }
}
