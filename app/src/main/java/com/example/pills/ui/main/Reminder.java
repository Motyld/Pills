package com.example.pills.ui.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pills.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class Reminder {

    private long id;
    private String time;
    private List<String> names;

    public Reminder(long id, String time, List<String> names) {
        this.id = id;
        this.time = time;
        this.names = names;
    }

    public long getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getNamesList() {
        return String.join(", ", names);
    }

    public void markAsTaken(Context ctx) {
        updateStatus(ctx, "taken");
    }

    public void markAsMissed(Context ctx) {
        updateStatus(ctx, "missed");
    }

    private void updateStatus(Context ctx, String status) {
        DatabaseHelper dbh = new DatabaseHelper(ctx);
        SQLiteDatabase w = dbh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        w.update("reminders", cv, "time = ?", new String[]{time});
        w.close();
    }

    public void editReminder(Context ctx) {
        // Можно открыть AddMedicationActivity с передачей времени или id
    }

    public static List<Reminder> getGroupedReminders(Context ctx) {
        List<Reminder> list = new ArrayList<>();
        DatabaseHelper dbh = new DatabaseHelper(ctx);
        SQLiteDatabase rdb = dbh.getReadableDatabase();

        Cursor c = rdb.rawQuery(
                "SELECT time, GROUP_CONCAT(drugs.name) " +
                        "FROM reminders " +
                        "JOIN drugs ON drugs.id = reminders.drug_id " +
                        "WHERE status IS NULL OR status='none' " +
                        "GROUP BY time " +
                        "ORDER BY time ASC", null);

        while (c.moveToNext()) {
            String time = c.getString(0);
            String namesStr = c.getString(1);
            List<String> names = new ArrayList<>();
            for (String n : namesStr.split(",")) {
                names.add(n.trim());
            }
            list.add(new Reminder(-1, time, names));
        }
        c.close();
        rdb.close();

        return list;
    }
}
