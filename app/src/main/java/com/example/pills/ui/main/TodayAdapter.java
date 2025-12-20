package com.example.pills.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;

import java.util.Iterator;
import java.util.List;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.ViewHolder> {

    private final List<TodayItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TodayItem item);
    }


    public TodayAdapter(List<TodayItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.today_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodayItem item = items.get(position);
        holder.tvTime.setText(item.time);
        holder.tvName.setText(item.drugName);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void refreshList(List<TodayItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    // Удаляет напоминание по id и дате
    public void removeByIdAndDate(long id, String date) {
        Iterator<TodayItem> iterator = items.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            TodayItem item = iterator.next();
            if (item.id == id && date.equals(item.date)) {
                iterator.remove();
                removed = true;
                break;
            }
        }
        if (removed) notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
