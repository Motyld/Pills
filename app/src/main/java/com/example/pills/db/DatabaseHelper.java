package com.example.pills.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pills.ui.main.HistoryRecord;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "medapp.db";
    public static final int DB_VERSION = 12; // Увеличили версию для миграции

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Таблица пользователей
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "login TEXT UNIQUE, " +
                        "password TEXT, " +
                        "fullName TEXT, " +
                        "email TEXT, " +
                        "phone TEXT" +
                        ");"
        );

        // Таблица лекарств
        db.execSQL(
                "CREATE TABLE drugs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "dosage TEXT, " +
                        "form TEXT, " +
                        "manufacturer TEXT, " +
                        "country TEXT, " +
                        "active_substance TEXT, " +
                        "indication TEXT, " +
                        "description TEXT" +
                        ");"
        );

        // Таблица напоминаний (ДОБАВЛЕНЫ ПОЛЯ form и schedule)
        db.execSQL(
                "CREATE TABLE reminders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "drug_id INTEGER, " +
                        "time TEXT, " +
                        "timestamp INTEGER, " +
                        "days TEXT, " +
                        "form TEXT, " +
                        "schedule TEXT, " +
                        "status TEXT DEFAULT 'none'," +
                        "drug_name TEXT," +
                        "FOREIGN KEY(drug_id) REFERENCES drugs(id)" +
                        ");"
        );

        // Таблица истории
        db.execSQL(
                "CREATE TABLE history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "drug_name TEXT, " +
                        "time TEXT, " +
                        "date TEXT, " +
                        "status TEXT" +
                        ");"
        );

        // Примеры 20 лекарств
        insertSampleDrugs(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 12) {
            // Добавляем новые колонки form и schedule в reminders
            db.execSQL("ALTER TABLE reminders ADD COLUMN form TEXT");
            db.execSQL("ALTER TABLE reminders ADD COLUMN schedule TEXT");
        }
        // Если версия еще меньше - пересоздаем
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS reminders");
            db.execSQL("DROP TABLE IF EXISTS drugs");
            db.execSQL("DROP TABLE IF EXISTS history");
            onCreate(db);
        }
    }

    private void insertSampleDrugs(SQLiteDatabase db) {
        insertDrug(db, "Парацетамол", "500мг", "Таблетка", "ФармФирма", "Россия", "Парацетамол", "Жар, боль", "");
        insertDrug(db, "Ибупрофен", "200мг", "Капсула", "МедПро", "Германия", "Ибупрофен", "Боль, воспаление", "");
        insertDrug(db, "Амоксициллин", "250мг", "Капсула", "ФармПро", "Россия", "Амоксициллин", "Инфекция", "");
        insertDrug(db, "Цефтриаксон", "1г", "Порошок", "Фармацевт", "Италия", "Цефтриаксон", "Инфекция", "");
        insertDrug(db, "Аспирин", "100мг", "Таблетка", "АспирПро", "Швейцария", "Ацетилсалициловая кислота", "Сердечно-сосудистые", "");
    }

    // ✅ НОВЫЙ МЕТОД: Создает лекарство если его нет
    public long insertDrugIfMissing(String name) {
        long drugId = findDrugByName(name);
        if (drugId != -1) return drugId; // Уже есть

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("dosage", "");
        cv.put("form", "");
        cv.put("manufacturer", "");
        cv.put("country", "");
        cv.put("active_substance", "");
        cv.put("indication", "");
        cv.put("description", "");
        drugId = db.insert("drugs", null, cv);
        db.close();
        Log.d("DatabaseHelper", "➕ Создано новое лекарство: " + name + " (ID=" + drugId + ")");
        return drugId;
    }

    private void insertDrug(SQLiteDatabase db, String name, String dosage, String form,
                            String manufacturer, String country, String activeSubstance, String indication,
                            String description) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("dosage", dosage);
        cv.put("form", form);
        cv.put("manufacturer", manufacturer);
        cv.put("country", country);
        cv.put("active_substance", activeSubstance);
        cv.put("indication", indication);
        cv.put("description", description);
        db.insert("drugs", null, cv);
    }

    public ArrayList<HistoryRecord> getAllHistorySortedByDate() {
        ArrayList<HistoryRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, drug_name, time, date, status FROM history ORDER BY date DESC, time DESC",
                null
        );

        if (c.moveToFirst()) {
            do {
                list.add(new HistoryRecord(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4)
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }

    // ---------------- Методы ----------------

    public long findDrugByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM drugs WHERE name = ? LIMIT 1", new String[]{name});
        long id = -1;
        if (c.moveToFirst()) id = c.getLong(0);
        c.close();
        db.close();
        return id;
    }

    public String getDrugDosageByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT dosage FROM drugs WHERE name = ? LIMIT 1", new String[]{name});
        String dosage = "1";
        if (c.moveToFirst()) dosage = c.getString(0);
        c.close();
        db.close();
        return dosage;
    }

    public String getDrugFormByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT form FROM drugs WHERE name = ? LIMIT 1", new String[]{name});
        String form = "таблетка";
        if (c.moveToFirst()) form = c.getString(0);
        c.close();
        db.close();
        return form;
    }

    // ✅ ИСПРАВЛЕННЫЙ МЕТОД: добавлены параметры form и schedule
    public long insertReminder(long drugId, long timestamp, String days, String time,
                               String drugName, String form, String schedule) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("drug_id", drugId);
        cv.put("timestamp", timestamp);
        cv.put("days", days);
        cv.put("time", time);
        cv.put("drug_name", drugName);
        cv.put("form", form);
        cv.put("schedule", schedule);
        long id = db.insert("reminders", null, cv);
        db.close();
        Log.d("DatabaseHelper", "💊 Сохранено напоминание ID=" + id + " для " + drugName);
        return id;
    }

    public void updateReminderStatus(long reminderId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("reminders", cv, "id=?", new String[]{String.valueOf(reminderId)});
        db.close();
    }

    public void saveToHistory(String drugName, String time, String date, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("drug_name", drugName);
        cv.put("time", time);
        cv.put("date", date);
        cv.put("status", status);
        db.insert("history", null, cv);
        db.close();
    }

    public void deleteReminderForDay(long reminderId, long dayStart, long dayEnd) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(
                "reminders",
                "id = ? AND timestamp >= ? AND timestamp <= ?",
                new String[]{
                        String.valueOf(reminderId),
                        String.valueOf(dayStart),
                        String.valueOf(dayEnd)
                }
        );
        db.close();
        Log.d("DatabaseHelper", "🗑️ Deleted " + deleted + " reminders for ID=" + reminderId);
    }
}
