package com.example.pills.ui.main;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private final List<Reminder> reminders;

    public ReminderAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
        Log.d("ReminderAdapter", "🔧 Created with " + reminders.size() + " items");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        Log.d("ReminderAdapter", "📱 ViewHolder created");
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        Log.d("ReminderAdapter", "🖥️ Bind pos=" + position + " ID=" + reminder.getId() + " " + reminder.getName());

        holder.tvTime.setText(reminder.getTime());
        holder.tvName.setText(reminder.getName());

        holder.btnMore.setOnClickListener(v ->
                showReminderMenu(holder.itemView.getContext(), reminder)
        );
    }

    @Override
    public int getItemCount() {
        Log.d("ReminderAdapter", "📊 getItemCount() = " + reminders.size());
        return reminders.size();
    }

    public void removeReminder(Reminder r) {
        int index = reminders.indexOf(r);
        if (index != -1) {
            reminders.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, reminders.size());
            Log.d("ReminderAdapter", "🗑️ Removed ID=" + r.getId() + " | left: " + reminders.size());
        }
    }

    private void showReminderMenu(Context ctx, Reminder reminder) {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_reminder_actions, null);

        view.findViewById(R.id.actionAccept).setOnClickListener(v -> {
            updateStatus(ctx, reminder.getId(), "taken");
            saveToHistory(ctx, reminder, "Принял");
            cancelNotification(ctx, reminder.getId());
            removeReminder(reminder);
            dialog.dismiss();
        });

        view.findViewById(R.id.actionSkip).setOnClickListener(v -> {
            updateStatus(ctx, reminder.getId(), "missed");
            saveToHistory(ctx, reminder, "Пропустил");
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
        });

        view.findViewById(R.id.actionCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void updateStatus(Context ctx, long reminderId, String status) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        db.updateReminderStatus(reminderId, status);
        Log.d("ReminderAdapter", "✅ Status updated ID=" + reminderId + " → " + status);
    }

    private void saveToHistory(Context ctx, Reminder reminder, String status) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        // ✅ ИСПРАВЛЕНО: Calendar.getInstance() вместо new Date()
        Calendar cal = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        db.saveToHistory(reminder.getName(), reminder.getTime(), today, status);
        Log.d("ReminderAdapter", "📝 History saved: " + today);
    }

    private void cancelNotification(Context ctx, long reminderId) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel((int) reminderId);
    }

    private void deleteReminderForToday(Reminder reminder, Context ctx) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        long start = startOfDay(reminder.getTimestamp());
        long end = endOfDay(reminder.getTimestamp());
        db.deleteReminderForDay(reminder.getId(), start, end);
    }

    private long startOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long endOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
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
