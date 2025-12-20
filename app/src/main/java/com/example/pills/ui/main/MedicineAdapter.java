package com.example.pills.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pills.R;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<String> medicines;
    private final OnMedicineClickListener listener;

    public interface OnMedicineClickListener {
        void onClick(String name);
    }

    public MedicineAdapter(List<String> medicines, OnMedicineClickListener listener) {
        this.medicines = medicines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String med = medicines.get(position);
        holder.tvName.setText(med);
        holder.itemView.setOnClickListener(v -> listener.onClick(med));
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public void updateList(List<String> newList) {
        medicines = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedName);
        }
    }
}
