package com.example.pills.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pills.db.DatabaseHelper;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        Log.d("BootReceiver", "🔁 BOOT_COMPLETED → restoring alarms");

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // ✅ восстанавливаем только будущие активные напоминания
        Cursor cursor = db.rawQuery(
                "SELECT r.id, r.timestamp, COALESCE(r.drug_name, d.name) AS drugName, " +
                        "r.form, r.schedule " +
                        "FROM reminders r " +
                        "LEFT JOIN drugs d ON d.id = r.drug_id " +
                        "WHERE (r.status = 'none' OR r.status IS NULL) AND r.timestamp > ?",
                new String[]{String.valueOf(System.currentTimeMillis())}
        );

        int restored = 0;

        while (cursor.moveToNext()) {
            long reminderId = cursor.getLong(0);
            long timestamp = cursor.getLong(1);
            String drugName = cursor.getString(2);

            String form = cursor.getString(3);
            String schedule = cursor.getString(4);

            // ✅ что показываем пользователю
            String displayTitle = drugName;
            if (form != null && !form.trim().isEmpty()) {
                displayTitle += " (" + form + ")";
            }
            if (schedule != null && !schedule.trim().isEmpty()) {
                displayTitle += " - " + schedule;
            }

            NotificationScheduler.scheduleOneTime(
                    context,
                    drugName,
                    displayTitle,
                    timestamp,
                    reminderId
            );

            restored++;
            Log.d("BootReceiver", "✅ Restored alarm ID=" + reminderId + " at " + timestamp);
        }

        cursor.close();
        db.close();
        dbHelper.close();

        Log.d("BootReceiver", "✅ Total restored: " + restored);
    }
}
