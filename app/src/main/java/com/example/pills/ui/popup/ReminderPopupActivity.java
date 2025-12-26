package com.example.pills.ui.popup;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.notifications.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderPopupActivity extends Activity {

    private long reminderId;
    private long timestamp;
    private String timeText;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_reminder_popup);

        reminderId = getIntent().getLongExtra("reminderId", -1);
        timestamp = getIntent().getLongExtra("timestamp", 0);

        timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));

        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvItems = findViewById(R.id.tvItems);

        Button btnAccept = findViewById(R.id.btnAccept);
        Button btnSkip = findViewById(R.id.btnSkip);
        Button btnSnooze = findViewById(R.id.btnSnooze);

        tvTime.setText(timeText);

        // ✅ список лекарств + дозировки берём из БД
        DatabaseHelper db = new DatabaseHelper(this);
        String itemsText = db.getReminderItemsText(reminderId);
        db.close();

        if (itemsText == null || itemsText.trim().isEmpty()) {
            itemsText = "Нет лекарств";
        }

        tvItems.setText(itemsText);

        btnAccept.setOnClickListener(v -> finishWithStatus("taken", "Принял"));
        btnSkip.setOnClickListener(v -> finishWithStatus("missed", "Пропустил"));
        btnSnooze.setOnClickListener(v -> snooze5Minutes());
    }

    private void finishWithStatus(String statusDb, String statusHistoryRu) {
        DatabaseHelper db = new DatabaseHelper(this);

        db.updateReminderStatus(reminderId, statusDb);

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(timestamp));

        // ✅ как в уведомлении: по каждому лекарству
        db.saveReminderToHistory(reminderId, timeText, date, statusHistoryRu);

        db.close();

        cancelAlarm(reminderId);
        cancelNotification(reminderId);

        sendBroadcast(new Intent("com.example.pills.REFRESH_REMINDERS"));
        finish();
    }

    // ✅ Отложить на 5 минут
    private void snooze5Minutes() {
        long newTs = System.currentTimeMillis() + 5 * 60 * 1000L;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(newTs);

        String newTimeText = String.format(Locale.getDefault(),
                "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        DatabaseHelper db = new DatabaseHelper(this);
        db.updateReminderTime(reminderId, newTs, newTimeText);
        db.close();

        scheduleAlarmAgain(reminderId, newTs);

        cancelNotification(reminderId);
        sendBroadcast(new Intent("com.example.pills.REFRESH_REMINDERS"));

        finish();
    }

    private void scheduleAlarmAgain(long reminderId, long newTs) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;

        int requestCode = (int) (reminderId % Integer.MAX_VALUE);

        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("reminderId", reminderId);
        i.putExtra("timestamp", newTs);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pi);

        if (newTs <= System.currentTimeMillis()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, newTs, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, newTs, pi);
        }
    }

    private void cancelAlarm(long reminderId) {
        int requestCode = (int) (reminderId % Integer.MAX_VALUE);

        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                i,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pi != null) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null) am.cancel(pi);
            pi.cancel();
        }
    }

    private void cancelNotification(long reminderId) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel((int) (reminderId % Integer.MAX_VALUE));
    }
}
