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
        long timestamp = intent.getLongExtra("timestamp", 0);

        int notificationId = (int) (reminderId % Integer.MAX_VALUE);

        createChannel(context);

        // ✅ Получаем список лекарств + дозы из БД
        DatabaseHelper db = new DatabaseHelper(context);
        String itemsText = db.getReminderItemsText(reminderId);
        db.close();

        if (itemsText == null || itemsText.trim().isEmpty()) {
            itemsText = "Лекарства не указаны";
        }

        // Открытие попапа
        Intent popupIntent = new Intent(context, ReminderPopupActivity.class);
        popupIntent.putExtra("reminderId", reminderId);
        popupIntent.putExtra("timestamp", timestamp);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                notificationId,
                popupIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // ✅ Уведомление (большое)
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle("Пора принять лекарства")
                .setContentText(itemsText.split("\n")[0]) // первая строка как превью
                .setStyle(new NotificationCompat.BigTextStyle().bigText(itemsText))
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

        // ✅ сразу открываем большое окно (как ты хотел)
        context.startActivity(popupIntent);
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null && nm.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "Medication reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        if (nm != null) nm.createNotificationChannel(ch);
    }
}
