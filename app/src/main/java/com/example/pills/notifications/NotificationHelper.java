package com.example.pills.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.pills.R;
import com.example.pills.ui.popup.ReminderPopupActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_ID = "pills_reminders_channel";

    /**
     * Показывает уведомление о приёме лекарства
     *
     * @param context    Контекст
     * @param reminderId Уникальный ID напоминания
     * @param drugName   Название лекарства
     * @param time       Время приёма
     */
    public static void showReminderNotification(Context context,
                                                long reminderId,
                                                String drugName,
                                                String time) {

        // Создаём канал уведомлений (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Напоминания о лекарствах");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Передаём сегодняшнюю дату
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Intent для открытия ReminderPopupActivity
        Intent intent = new Intent(context, ReminderPopupActivity.class);
        intent.putExtra("reminderId", reminderId);
        intent.putExtra("title", drugName);
        intent.putExtra("time", time);
        intent.putExtra("date", today);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) reminderId, // уникальный requestCode для каждого уведомления
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Строим уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // твоя иконка
                .setContentTitle("Напоминание о лекарстве")
                .setContentText(drugName + " в " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) reminderId, builder.build());
        }
    }
}
