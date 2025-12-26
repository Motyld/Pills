package com.example.pills.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;

import java.util.ArrayList;

public class TemplatesAdapter extends RecyclerView.Adapter<TemplatesAdapter.VH> {

    public interface OnTemplateAction {
        void onRepeat(long templateId);
        void onDelete(long templateId);
    }

    private final ArrayList<TemplateRecord> list;
    private final OnTemplateAction cb;

    public TemplatesAdapter(ArrayList<TemplateRecord> list, OnTemplateAction cb) {
        this.list = list;
        this.cb = cb;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TemplateRecord t = list.get(position);

        h.title.setText(t.title == null ? "Курс" : t.title);

        String sch = (t.schedule == null ? "" : t.schedule);
        h.meta.setText(sch + " • " + t.timesPerDay + " раз/день");

        h.btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cb.onRepeat(t.id);
            }
        });

        h.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cb.onDelete(t.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, meta;
        Button btnRepeat, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTemplateTitle);
            meta = itemView.findViewById(R.id.tvTemplateMeta);
            btnRepeat = itemView.findViewById(R.id.btnTemplateRepeat);
            btnDelete = itemView.findViewById(R.id.btnTemplateDelete);
        }
    }
}
