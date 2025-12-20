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

    private ArrayList<HistoryItem> list;

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

        } else if (holder instanceof ItemVH) {
            ItemVH vh = (ItemVH) holder;

            vh.name.setText(item.name);
            vh.time.setText(item.time);
            vh.status.setText(item.status);
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView title;
        HeaderVH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.headerTitle);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView name, time, status;
        ItemVH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.historyName);
            time = itemView.findViewById(R.id.historyTime);
            status = itemView.findViewById(R.id.historyStatus);
        }
    }
}
