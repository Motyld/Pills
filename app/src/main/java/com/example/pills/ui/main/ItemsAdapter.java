package com.example.pills.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.VH> {

    private final ReminderDraft draft;

    public ItemsAdapter(ReminderDraft draft) {
        this.draft = draft;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_drug, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ReminderDraft.Item item = draft.items.get(position);

        h.tvName.setText(item.drugName);
        h.tvDose.setText(item.getDoseText());

        h.btnRemove.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            draft.items.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, draft.items.size());
        });
    }

    @Override
    public int getItemCount() {
        return (draft.items == null) ? 0 : draft.items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDose;
        Button btnRemove;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDose = itemView.findViewById(R.id.tvItemDose);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
