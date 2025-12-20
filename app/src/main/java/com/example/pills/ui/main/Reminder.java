package com.example.pills.ui.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pills.db.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Reminder {

    private long id;
    private String time;
    private String name;
    private long timestamp;

    public Reminder(long id, String time, String name, long timestamp) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public String getTime() { return time; }
    public String getName() { return name; }
    public long getTimestamp() { return timestamp; }

    public void markAsTaken(Context ctx) { updateStatus(ctx, "taken", timestamp);  }
    public void markAsMissed(Context ctx) { updateStatus(ctx, "missed", timestamp); }

    private long startOfReminderDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long endOfReminderDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    private void updateStatus(Context ctx, String status, long reminderTimestamp) {  // ← ДОБАВЬ параметр
        DatabaseHelper dbh = new DatabaseHelper(ctx);
        SQLiteDatabase w = dbh.getWritableDatabase();

        // ИСПРАВЛЕНО: используем дату НАПОМИНАНИЯ, а не сегодня!
        long startOfDay = startOfReminderDay(reminderTimestamp);
        long endOfDay = endOfReminderDay(reminderTimestamp);

        ContentValues cv = new ContentValues();
        cv.put("status", status);
        int updated = w.update(
                "reminders",
                cv,
                "id = ? AND timestamp >= ? AND timestamp <= ?",
                new String[]{String.valueOf(id), String.valueOf(startOfDay), String.valueOf(endOfDay)}
        );

        if (updated > 0) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

            String reminderDate = sdfDate.format(new Date(reminderTimestamp));  // ← ДАТА НАПОМИНАНИЯ!
            String currentTime = sdfTime.format(new Date());  // ← ВРЕМЯ ДЕЙСТВИЯ

            // ✅ ПЕРЕВОД НА РУССКИЙ
            String russianStatus = status.equals("taken") ? "Принял" : "Пропустил";
            dbh.saveToHistory(name, time, reminderDate, russianStatus);
        }

        w.close();
    }

    private long getStartOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public void editReminder(Context ctx) {
        // можно открыть AddMedicationActivity с передачей времени или id
    }

    public static List<Reminder> getTodaysReminders(Context ctx) {
        List<Reminder> list = new ArrayList<>();
        DatabaseHelper dbh = new DatabaseHelper(ctx);
        SQLiteDatabase rdb = dbh.getReadableDatabase();

        long startOfDay = new Reminder(0,"","",0).getStartOfToday();
        long endOfDay = new Reminder(0,"","",0).getEndOfToday();

        Cursor c = rdb.rawQuery(
                "SELECT reminders.id, reminders.time, reminders.timestamp, drugs.name " +
                        "FROM reminders " +
                        "JOIN drugs ON drugs.id = reminders.drug_id " +
                        "WHERE (reminders.status IS NULL OR reminders.status='none') " +
                        "AND reminders.timestamp BETWEEN ? AND ? " +
                        "ORDER BY reminders.time ASC",
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)}
        );

        while (c.moveToNext()) {
            long id = c.getLong(0);
            String time = c.getString(1);
            long timestamp = c.getLong(2);
            String name = c.getString(3);
            list.add(new Reminder(id, time, name, timestamp));
        }

        c.close();
        rdb.close();
        return list;
    }

    // В методе updateStatus добавь сохранение в историю


}
