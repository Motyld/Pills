package com.example.pills.ui.popup;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.notifications.AlarmReceiver;
import com.example.pills.ui.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderPopupActivity extends Activity {

    private long reminderId;
    private String title;
    private String timeText;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_reminder_popup);

        reminderId = getIntent().getLongExtra("reminderId", -1);
        title = getIntent().getStringExtra("title");
        long timestamp = getIntent().getLongExtra("timestamp", 0);

        // Форматируем время
        Date date = new Date(timestamp);
        timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);

        // Устанавливаем текст в попапе
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTime = findViewById(R.id.tvTime);

        tvTitle.setText(title);
        tvTime.setText("Время: " + timeText);

        findViewById(R.id.btnAccept).setOnClickListener(v -> action("taken"));
        findViewById(R.id.btnSkip).setOnClickListener(v -> action("missed"));
    }

    private void action(String status) {
        DatabaseHelper db = new DatabaseHelper(this);

        // Обновляем статус
        db.updateReminderStatus(reminderId, status);

        // Сохраняем в историю
        db.saveToHistory(title, timeText,
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()),
                status);

        cancelAlarm();
        cancelNotification();
        notifyMain();
        finish();
    }

    private void cancelAlarm() {
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
            am.cancel(pi);
            pi.cancel();
        }
    }

    private void cancelNotification() {
        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel((int) (reminderId % Integer.MAX_VALUE));
    }

    private void notifyMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
