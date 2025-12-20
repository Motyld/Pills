package com.example.pills.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.pills.db.DatabaseHelper;

public class NotificationScheduler {

    public static void scheduleOneTime(
            Context context,
            String title,
            long triggerTimestamp,
            long reminderId
    ) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        int requestCode = (int) (reminderId % Integer.MAX_VALUE);

        // ❗ Отменяем старый
        Intent cancelIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent cancelPi = PendingIntent.getBroadcast(
                context,
                requestCode,
                cancelIntent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (cancelPi != null) {
            am.cancel(cancelPi);
            cancelPi.cancel();
            Log.d("Scheduler", "🛑 Old alarm cancelled ID=" + reminderId);
        }

        // Получаем дозу сразу из базы
        DatabaseHelper db = new DatabaseHelper(context);
        String dose = db.getDrugDosageByName(title);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("reminderId", reminderId);
        intent.putExtra("timestamp", triggerTimestamp);
        intent.putExtra("dose", dose); // передаем дозу

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (triggerTimestamp <= System.currentTimeMillis()) {
            Log.d("Scheduler", "⛔ Timestamp in past, skip");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimestamp,
                    pi
            );
        } else {
            am.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimestamp,
                    pi
            );
        }

        Log.d("Scheduler", "✅ Alarm scheduled ID=" + reminderId + " ts=" + triggerTimestamp);
    }
}
