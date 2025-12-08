package com.example.pills.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pills.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == null) return;

        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) &&
                !intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            return;
        }

        DatabaseHelper dbh = new DatabaseHelper(context);
        SQLiteDatabase rdb = dbh.getReadableDatabase();

        Cursor c = rdb.rawQuery(
                "SELECT id, time, days, status FROM reminders WHERE status IS NULL OR status='none'",
                null
        );

        while (c.moveToNext()) {

            long id = c.getLong(c.getColumnIndexOrThrow("id"));
            String time = c.getString(c.getColumnIndexOrThrow("time"));
            String daysStr = c.getString(c.getColumnIndexOrThrow("days"));

            if (time == null || !time.contains(":")) continue;

            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            List<Integer> days = new ArrayList<>();
            if (daysStr != null && daysStr.length() > 2) {
                daysStr = daysStr.replace("[", "").replace("]", "");
                for (String d : daysStr.split(",")) {
                    try { days.add(Integer.parseInt(d.trim())); } catch (Exception ignored) {}
                }
            }

            if (!days.isEmpty()) {
                NotificationScheduler.scheduleRepeating(context, "Напоминание", hour, minute, days, id);
            } else {
                NotificationScheduler.scheduleOneTime(context, "Напоминание", hour, minute, id);
            }
        }

        c.close();
        rdb.close();
    }
}
