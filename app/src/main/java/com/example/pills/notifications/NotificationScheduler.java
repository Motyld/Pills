package com.example.pills.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    public static void scheduleOneTime(Context ctx, String title, int hour, int minute, long reminderId) {
        if (!canScheduleExactAlarms(ctx)) {
            Log.d(TAG, "Cannot schedule exact alarms!");
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        schedule(ctx, title, cal.getTimeInMillis(), reminderId);
    }

    public static void scheduleRepeating(Context ctx, String title, int hour, int minute, List<Integer> days, long reminderId) {
        if (!canScheduleExactAlarms(ctx)) {
            Log.d(TAG, "Cannot schedule exact alarms!");
            return;
        }

        for (int day : days) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);

            if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            }

            schedule(ctx, title, cal.getTimeInMillis(), reminderId + day);
        }
    }

    private static void schedule(Context ctx, String title, long timeInMillis, long requestCode) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("reminderId", requestCode);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                (int) requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
        }

        Log.d(TAG, "Scheduled: " + title + " at " + timeInMillis + " req=" + requestCode);
    }

    // Проверка точных будильников
    public static boolean canScheduleExactAlarms(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            return am != null && am.canScheduleExactAlarms();
        }
        return true;
    }
}
