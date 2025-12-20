package com.example.pills.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.ui.popup.ReminderPopupActivity;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "pill_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        long reminderId = intent.getLongExtra("reminderId", -1);
        String title = intent.getStringExtra("title");
        long timestamp = intent.getLongExtra("timestamp", 0);

        // Получаем дозу сразу
        DatabaseHelper db = new DatabaseHelper(context);
        String dose = db.getDrugDosageByName(title);

        int notificationId = (int) (reminderId % Integer.MAX_VALUE);

        createChannel(context);

        Intent popupIntent = new Intent(context, ReminderPopupActivity.class);
        popupIntent.putExtra("reminderId", reminderId);
        popupIntent.putExtra("title", title);
        popupIntent.putExtra("timestamp", timestamp);
        popupIntent.putExtra("dose", dose);  // передаем дозу
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                notificationId,
                popupIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle(title)
                .setContentText("Приём лекарства")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null) {
            nm.notify(notificationId, notification);
        }
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "Medication reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(ch);
    }
}
