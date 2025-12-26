package com.example.pills.ui.main;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    public interface OnEditClickListener {
        void onEdit(Reminder reminder);
    }

    private final List<Reminder> reminders;
    private final OnEditClickListener onEdit;

    public ReminderAdapter(List<Reminder> reminders, OnEditClickListener onEdit) {
        this.reminders = reminders;
        this.onEdit = onEdit;
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
        holder.tvName.setText(reminder.getName()); // ✅ тут теперь список лекарств

        holder.btnMore.setOnClickListener(v ->
                showReminderMenu(holder.itemView.getContext(), reminder)
        );
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public void removeReminder(Reminder r) {
        int index = reminders.indexOf(r);
        if (index != -1) {
            reminders.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, reminders.size());
        }
    }

    private void showReminderMenu(Context ctx, Reminder reminder) {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_reminder_actions, null);

        view.findViewById(R.id.actionAccept).setOnClickListener(v -> {
            updateStatus(ctx, reminder.getId(), "taken");
            saveToHistoryLikeNotification(ctx, reminder, "Принял");
            cancelNotification(ctx, reminder.getId());
            removeReminder(reminder);
            dialog.dismiss();
        });

        view.findViewById(R.id.actionSkip).setOnClickListener(v -> {
            updateStatus(ctx, reminder.getId(), "missed");
            saveToHistoryLikeNotification(ctx, reminder, "Пропустил");
            cancelNotification(ctx, reminder.getId());
            removeReminder(reminder);
            dialog.dismiss();
        });

        view.findViewById(R.id.actionDeleteToday).setOnClickListener(v -> {
            deleteReminderForToday(reminder, ctx);
            cancelNotification(ctx, reminder.getId());
            removeReminder(reminder);
            dialog.dismiss();
        });

        view.findViewById(R.id.actionEdit).setOnClickListener(v -> {
            dialog.dismiss();
            if (onEdit != null) onEdit.onEdit(reminder);
        });

        view.findViewById(R.id.actionCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void updateStatus(Context ctx, long reminderId, String status) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        db.updateReminderStatus(reminderId, status);
        db.close();
    }

    // ✅ история как в уведомлении: (название таблетки) + время + Принял/Пропустил
    // ✅ и если лекарств несколько — запишется несколько строк (по каждому item)
    private void saveToHistoryLikeNotification(Context ctx, Reminder reminder, String statusRu) {
        DatabaseHelper db = new DatabaseHelper(ctx);

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(reminder.getTimestamp()));

        db.saveReminderToHistory(reminder.getId(), reminder.getTime(), date, statusRu);
        db.close();
    }

    private void cancelNotification(Context ctx, long reminderId) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel((int) (reminderId % Integer.MAX_VALUE));
    }

    private void deleteReminderForToday(Reminder reminder, Context ctx) {
        // оставь как у тебя было — если нужно, я потом под multi-logic сделаю удаление
        // (сейчас вы не просили менять удаление “только сегодня”)
        DatabaseHelper db = new DatabaseHelper(ctx);

        long ts = reminder.getTimestamp();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(ts);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();

        db.deleteReminderForDay(reminder.getId(), start, end);
        db.close();
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
