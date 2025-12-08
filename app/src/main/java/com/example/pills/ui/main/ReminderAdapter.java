package com.example.pills.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private final List<Reminder> reminders;

    public ReminderAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);

        holder.tvTime.setText(reminder.getTime());
        holder.tvName.setText(reminder.getNamesList());

        holder.btnMore.setOnClickListener(v ->
                showReminderMenu(holder.itemView.getContext(), reminder)
        );
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    private void showReminderMenu(Context ctx, Reminder reminder) {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_reminder_actions, null);

        view.findViewById(R.id.actionAccept).setOnClickListener(v -> {
            reminder.markAsTaken(ctx);
            dialog.dismiss();
        });

        view.findViewById(R.id.actionSkip).setOnClickListener(v -> {
            reminder.markAsMissed(ctx);
            dialog.dismiss();
        });

        view.findViewById(R.id.actionEdit).setOnClickListener(v -> {
            reminder.editReminder(ctx);
            dialog.dismiss();
        });

        view.findViewById(R.id.actionCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvName;
        ImageButton btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
