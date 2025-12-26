package com.example.pills.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pills.ui.main.HistoryRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "medapp.db";
    public static final int DB_VERSION = 19; // ✅ v17: history = planned_time + action_time + medicines

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // ================= USERS =================
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "login TEXT UNIQUE, " +
                        "password TEXT, " +
                        "fullName TEXT, " +
                        "email TEXT, " +
                        "phone TEXT, " +
                        "pin TEXT" +
                        ");"
        );

        // ================= DRUGS =================
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
                        "description TEXT, " +
                        "drug_type TEXT, " +
                        "course TEXT" +
                        ");"
        );

        // ================= REMINDERS (EVENT) =================
        // одно время -> одно событие
        db.execSQL(
                "CREATE TABLE reminders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "drug_id INTEGER, " +   // legacy поле (старый код), оставляем
                        "time TEXT, " +
                        "timestamp INTEGER, " +
                        "days TEXT, " +
                        "form TEXT, " +
                        "schedule TEXT, " +
                        "dose TEXT, " +         // legacy поле (старый код), оставляем
                        "repeat_minutes INTEGER, " +
                        "times_per_day INTEGER, " +
                        "status TEXT DEFAULT 'none', " +
                        "drug_name TEXT, " +    // legacy поле, оставляем
                        "FOREIGN KEY(drug_id) REFERENCES drugs(id)" +
                        ");"
        );

        // ================= REMINDER_ITEMS =================
        // несколько лекарств в одном напоминании
        db.execSQL(
                "CREATE TABLE reminder_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "reminder_id INTEGER NOT NULL, " +
                        "drug_id INTEGER NOT NULL, " +
                        "dose TEXT DEFAULT '', " +
                        "course_days INTEGER DEFAULT 30, " +
                        "FOREIGN KEY(reminder_id) REFERENCES reminders(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(drug_id) REFERENCES drugs(id)" +
                        ");"
        );

        // ================= HISTORY (v17) =================
        // одна строка = одно событие (reminder): план/факт/статус/список лекарств
        db.execSQL(
                "CREATE TABLE history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "date TEXT, " +                 // "2025-12-26"
                        "planned_time TEXT, " +         // "09:00"
                        "action_time TEXT, " +          // "09:07"
                        "status TEXT, " +               // "Принял" / "Пропустил"
                        "medicines TEXT, " +            // "• Парацетамол — 1 таб\n• Витамин C — 500 мг"
                        "drug_name TEXT, " +            // legacy (для совместимости)
                        "time TEXT" +                   // legacy (для совместимости)
                        ");"
        );

        // ================= REMINDER_TEMPLATES =================
        db.execSQL(
                "CREATE TABLE reminder_templates (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT, " +
                        "schedule TEXT, " +
                        "times_per_day INTEGER DEFAULT 1, " +
                        "repeat_minutes INTEGER DEFAULT 0, " +
                        "created_at INTEGER DEFAULT 0" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE template_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "template_id INTEGER NOT NULL, " +
                        "drug_id INTEGER NOT NULL, " +
                        "dose TEXT DEFAULT '', " +
                        "course_days INTEGER DEFAULT 30, " +
                        "FOREIGN KEY(template_id) REFERENCES reminder_templates(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(drug_id) REFERENCES drugs(id)" +
                        ");"
        );


        insertSampleDrugs(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // если совсем старая версия — пересоздаём
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS reminder_items");
            db.execSQL("DROP TABLE IF EXISTS reminders");
            db.execSQL("DROP TABLE IF EXISTS drugs");
            db.execSQL("DROP TABLE IF EXISTS history");
            db.execSQL("DROP TABLE IF EXISTS users");
            onCreate(db);
            return;
        }

        // v12: form + schedule в reminders
        if (oldVersion < 12) {
            try { db.execSQL("ALTER TABLE reminders ADD COLUMN form TEXT"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE reminders ADD COLUMN schedule TEXT"); } catch (Exception ignored) {}
        }

        // v13: drug_type + course в drugs
        if (oldVersion < 13) {
            try { db.execSQL("ALTER TABLE drugs ADD COLUMN drug_type TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE drugs ADD COLUMN course TEXT DEFAULT ''"); } catch (Exception ignored) {}
        }

        // v14: поля мастера уведомлений
        if (oldVersion < 14) {
            try { db.execSQL("ALTER TABLE reminders ADD COLUMN dose TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE reminders ADD COLUMN repeat_minutes INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE reminders ADD COLUMN times_per_day INTEGER DEFAULT 1"); } catch (Exception ignored) {}
        }

        // v16: новая таблица reminder_items + миграция старых reminders -> items
        if (oldVersion < 16) {
            try {
                db.execSQL(
                        "CREATE TABLE IF NOT EXISTS reminder_items (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "reminder_id INTEGER NOT NULL, " +
                                "drug_id INTEGER NOT NULL, " +
                                "dose TEXT DEFAULT '', " +
                                "FOREIGN KEY(reminder_id) REFERENCES reminders(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY(drug_id) REFERENCES drugs(id)" +
                                ");"
                );
            } catch (Exception ignored) {}

            migrateOldRemindersToItems(db);
        }

        // v17: history теперь хранит событие: planned_time + action_time + medicines
        if (oldVersion < 17) {
            try { db.execSQL("ALTER TABLE history ADD COLUMN planned_time TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE history ADD COLUMN action_time TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE history ADD COLUMN medicines TEXT DEFAULT ''"); } catch (Exception ignored) {}

            // legacy колонки на всякий случай
            try { db.execSQL("ALTER TABLE history ADD COLUMN drug_name TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE history ADD COLUMN time TEXT DEFAULT ''"); } catch (Exception ignored) {}
        }

        if (oldVersion < 18) {
            try {
                db.execSQL(
                        "CREATE TABLE IF NOT EXISTS reminder_templates (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "title TEXT, " +
                                "schedule TEXT, " +
                                "times_per_day INTEGER DEFAULT 1, " +
                                "repeat_minutes INTEGER DEFAULT 0, " +
                                "created_at INTEGER DEFAULT 0" +
                                ");"
                );
            } catch (Exception ignored) {}

            try {
                db.execSQL(
                        "CREATE TABLE IF NOT EXISTS template_items (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "template_id INTEGER NOT NULL, " +
                                "drug_id INTEGER NOT NULL, " +
                                "dose TEXT DEFAULT '', " +
                                "course_days INTEGER DEFAULT 30, " +
                                "FOREIGN KEY(template_id) REFERENCES reminder_templates(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY(drug_id) REFERENCES drugs(id)" +
                                ");"
                );
            } catch (Exception ignored) {}
        }
    }

    // ========================= MIGRATION =========================

    private void migrateOldRemindersToItems(SQLiteDatabase db) {
        // Если items уже есть — не трогаем
        Cursor check = db.rawQuery("SELECT COUNT(*) FROM reminder_items", null);
        boolean hasAny = false;
        if (check.moveToFirst()) hasAny = check.getInt(0) > 0;
        check.close();
        if (hasAny) return;

        Cursor c = db.rawQuery(
                "SELECT id, drug_id, dose FROM reminders WHERE drug_id IS NOT NULL AND drug_id > 0",
                null
        );

        int migrated = 0;
        while (c.moveToNext()) {
            long reminderId = c.getLong(0);
            long drugId = c.getLong(1);
            String dose = c.getString(2);
            if (dose == null) dose = "";

            ContentValues cv = new ContentValues();
            cv.put("reminder_id", reminderId);
            cv.put("drug_id", drugId);
            cv.put("dose", dose);
            db.insert("reminder_items", null, cv);
            migrated++;
        }
        c.close();

        Log.d("DatabaseHelper", "✅ Migrated old reminders -> reminder_items: " + migrated);
    }

    // ========================= SAMPLE DRUGS =========================

    private void insertSampleDrugs(SQLiteDatabase db) {
        insertDrug(db, "Парацетамол", "500мг", "Таблетка", "ФармФирма", "Россия",
                "Парацетамол", "Жар, боль", "",
                "Без рецепта", "5 дней");

        insertDrug(db, "Ибупрофен", "200мг", "Капсула", "МедПро", "Германия",
                "Ибупрофен", "Боль, воспаление", "",
                "Без рецепта", "7 дней");
    }

    private void insertDrug(SQLiteDatabase db, String name, String dosage, String form,
                            String manufacturer, String country, String activeSubstance,
                            String indication, String description,
                            String drugType, String course) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("dosage", dosage);
        cv.put("form", form);
        cv.put("manufacturer", manufacturer);
        cv.put("country", country);
        cv.put("active_substance", activeSubstance);
        cv.put("indication", indication);
        cv.put("description", description);
        cv.put("drug_type", drugType);
        cv.put("course", course);
        db.insert("drugs", null, cv);
    }

    // ========================= DRUGS BASIC =========================

    public long findDrugByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM drugs WHERE name = ? LIMIT 1", new String[]{name});
        long id = -1;
        if (c.moveToFirst()) id = c.getLong(0);
        c.close();
        db.close();
        return id;
    }

    public long insertDrugIfMissing(String name) {
        long drugId = findDrugByName(name);
        if (drugId != -1) return drugId;

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
        cv.put("drug_type", "");
        cv.put("course", "");
        long id = db.insert("drugs", null, cv);
        db.close();
        return id;
    }

    // ✅ чтобы не падали AlarmReceiver/Popup
    public String getDrugDosageByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT dosage FROM drugs WHERE name = ? LIMIT 1", new String[]{name});
        String dosage = "";
        if (c.moveToFirst()) dosage = c.getString(0);
        c.close();
        db.close();
        return dosage == null ? "" : dosage;
    }

    public String getDrugDescriptionByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT description FROM drugs WHERE name = ? LIMIT 1", new String[]{name});
        String res = "";
        if (c.moveToFirst()) res = c.getString(0);
        c.close();
        db.close();
        return res == null ? "" : res;
    }

    // ========================= HISTORY (v17) =========================

    /**
     * ✅ НОВОЕ: сохраняем одно событие в историю:
     * plannedTime = время по расписанию (например 09:00)
     * actionTime  = во сколько нажал (например 09:07)
     * medicines   = список лекарств из reminder_items
     */
    public void saveReminderEventToHistory(long reminderId,
                                           String plannedTime,
                                           String date,
                                           String actionTime,
                                           String statusRu) {

        String medicinesText = getReminderItemsText(reminderId);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("date", date == null ? "" : date);
        cv.put("planned_time", plannedTime == null ? "" : plannedTime);
        cv.put("action_time", actionTime == null ? "" : actionTime);
        cv.put("status", statusRu == null ? "" : statusRu);
        cv.put("medicines", medicinesText == null ? "" : medicinesText);

        // legacy
        cv.put("drug_name", "");
        cv.put("time", plannedTime == null ? "" : plannedTime);

        db.insert("history", null, cv);
        db.close();
    }

    /**
     * ✅ Оставляем для совместимости: если старый код зовёт saveToHistory(...)
     * Будет сохранять как "событие" с одним лекарством (medicines = drugName)
     */
    public void saveToHistory(String drugName, String time, String date, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("date", date == null ? "" : date);
        cv.put("planned_time", time == null ? "" : time);
        cv.put("action_time", time == null ? "" : time); // если нет факта — ставим как план
        cv.put("status", status == null ? "" : status);

        String meds = (drugName == null) ? "" : ("• " + drugName);
        cv.put("medicines", meds);

        // legacy
        cv.put("drug_name", drugName == null ? "" : drugName);
        cv.put("time", time == null ? "" : time);

        db.insert("history", null, cv);
        db.close();
    }

    /**
     * ✅ Старый метод "сохранить по reminderId".
     * Теперь сохраняем ОДНУ запись-событие (а не по каждому лекарству),
     * но actionTime тут берём как "текущее время", чтобы был факт.
     */
    public void saveReminderToHistory(long reminderId, String plannedTime, String date, String statusRu) {
        String actionTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        saveReminderEventToHistory(reminderId, plannedTime, date, actionTime, statusRu);
    }

    public ArrayList<HistoryRecord> getAllHistorySortedByDate() {
        ArrayList<HistoryRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, " +
                        "COALESCE(date,'') as date, " +
                        "COALESCE(planned_time,'') as planned_time, " +
                        "COALESCE(action_time,'') as action_time, " +
                        "COALESCE(status,'') as status, " +
                        "COALESCE(medicines,'') as medicines " +
                        "FROM history " +
                        "ORDER BY date DESC, planned_time DESC, action_time DESC",
                null
        );

        if (c.moveToFirst()) {
            do {
                list.add(new HistoryRecord(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5)
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }

    // ========================= REMINDERS (EVENT) =========================

    public void updateReminderStatus(long reminderId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("reminders", cv, "id=?", new String[]{String.valueOf(reminderId)});
        db.close();
    }

    public void updateReminderTime(long reminderId, long newTimestamp, String newTimeText) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("timestamp", newTimestamp);
        cv.put("time", newTimeText);
        db.update("reminders", cv, "id=?", new String[]{String.valueOf(reminderId)});
        db.close();
    }

    // ✅ создать событие (одно время)
    public long insertReminderEvent(long timestamp, String timeText, String days, String schedule) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("timestamp", timestamp);
        cv.put("time", timeText);
        cv.put("days", days);
        cv.put("schedule", schedule);
        cv.put("status", "none");

        // legacy поля
        cv.put("drug_id", 0);
        cv.put("drug_name", "");
        cv.put("dose", "");
        cv.put("form", "");
        cv.put("repeat_minutes", 0);
        cv.put("times_per_day", 1);

        long id = db.insert("reminders", null, cv);
        db.close();
        return id;
    }

    // ✅ добавить лекарство внутрь напоминания
    public long addReminderItem(long reminderId, long drugId, String dose, int courseDays) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("reminder_id", reminderId);
        cv.put("drug_id", drugId);
        cv.put("dose", dose == null ? "" : dose);
        cv.put("course_days", courseDays <= 0 ? 30 : courseDays);
        long id = db.insert("reminder_items", null, cv);
        db.close();
        return id;
    }

    public long addReminderItem(long reminderId, long drugId, String dose) {
        return addReminderItem(reminderId, drugId, dose, 30);
    }

    // ✅ текст "список лекарств + дозы" для попапа/главной/истории
    public String getReminderItemsText(long reminderId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT d.name, COALESCE(ri.dose,'') " +
                        "FROM reminder_items ri " +
                        "JOIN drugs d ON d.id = ri.drug_id " +
                        "WHERE ri.reminder_id = ? " +
                        "ORDER BY d.name ASC",
                new String[]{String.valueOf(reminderId)}
        );

        StringBuilder sb = new StringBuilder();
        while (c.moveToNext()) {
            String name = c.getString(0);
            String dose = c.getString(1);

            if (sb.length() > 0) sb.append("\n");

            if (dose != null && !dose.trim().isEmpty()) {
                sb.append("• ").append(name).append(" — ").append(dose.trim());
            } else {
                sb.append("• ").append(name);
            }
        }

        c.close();
        db.close();
        return sb.toString();
    }

    // ✅ запрос на главную: одно время -> список лекарств внутри
    public Cursor getTodaysGroupedReminders(long start, long end) {
        SQLiteDatabase db = getReadableDatabase();

        String sql =
                "SELECT r.id, r.time, r.timestamp, " +
                        "GROUP_CONCAT(d.name || CASE WHEN COALESCE(ri.dose,'')='' THEN '' ELSE (' — ' || ri.dose) END, '\n') as items " +
                        "FROM reminders r " +
                        "LEFT JOIN reminder_items ri ON ri.reminder_id = r.id " +
                        "LEFT JOIN drugs d ON d.id = ri.drug_id " +
                        "WHERE (r.status IS NULL OR r.status='none') " +
                        "AND r.timestamp BETWEEN ? AND ? " +
                        "GROUP BY r.id, r.time, r.timestamp " +
                        "ORDER BY r.time ASC";

        return db.rawQuery(sql, new String[]{String.valueOf(start), String.valueOf(end)});
    }

    // ========================= COMPATIBILITY (старый код) =========================

    // старый метод insertReminder(...) -> создаём event + 1 item
    public long insertReminder(long drugId, long timestamp, String days, String time,
                               String drugName, String form, String schedule) {

        long eventId = insertReminderEvent(timestamp, time, days, schedule);

        // привязка лекарства
        addReminderItem(eventId, drugId, ""); // дозу старый код не передавал нормально

        // legacy поля (если где-то показываешь reminders.drug_name/time)
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("drug_id", drugId);
        cv.put("drug_name", drugName == null ? "" : drugName);
        cv.put("form", form == null ? "" : form);
        cv.put("schedule", schedule == null ? "" : schedule);
        db.update("reminders", cv, "id=?", new String[]{String.valueOf(eventId)});
        db.close();

        return eventId;
    }

    // старый мастер insertReminderWizard(...) -> event + 1 item с dose
    public long insertReminderWizard(long drugId, long timestamp, String days, String time,
                                     String drugName, String form, String schedule,
                                     String dose, int repeatMinutes, int timesPerDay) {

        long eventId = insertReminderEvent(timestamp, time, days, schedule);

        addReminderItem(eventId, drugId, dose);

        // legacy поля (если где-то нужны)
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("drug_id", drugId);
        cv.put("drug_name", drugName == null ? "" : drugName);
        cv.put("form", form == null ? "" : form);
        cv.put("schedule", schedule == null ? "" : schedule);
        cv.put("dose", dose == null ? "" : dose);
        cv.put("repeat_minutes", repeatMinutes);
        cv.put("times_per_day", timesPerDay);
        db.update("reminders", cv, "id=?", new String[]{String.valueOf(eventId)});
        db.close();

        return eventId;
    }

    // удаление напоминаний конкретного лекарства за день (для режима редактирования “только сегодня”)
    public int deleteRemindersForDrugInRange(long drugId, long dayStart, long dayEnd) {
        SQLiteDatabase db = getWritableDatabase();

        // найти event'ы где есть это лекарство в items
        Cursor c = db.rawQuery(
                "SELECT r.id " +
                        "FROM reminders r " +
                        "JOIN reminder_items ri ON ri.reminder_id = r.id " +
                        "WHERE ri.drug_id = ? AND r.timestamp>=? AND r.timestamp<=?",
                new String[]{String.valueOf(drugId), String.valueOf(dayStart), String.valueOf(dayEnd)}
        );

        ArrayList<Long> reminderIds = new ArrayList<>();
        while (c.moveToNext()) reminderIds.add(c.getLong(0));
        c.close();

        int deleted = 0;
        for (Long rid : reminderIds) {
            deleted += db.delete("reminders", "id=?", new String[]{String.valueOf(rid)});
        }

        db.close();
        return deleted;
    }

    // удалить только одно событие по id в диапазоне дня (твой старый метод deleteReminderForDay)
    public void deleteReminderForDay(long reminderId, long dayStart, long dayEnd) {
        SQLiteDatabase db = getWritableDatabase();
        int del = db.delete(
                "reminders",
                "id = ? AND timestamp >= ? AND timestamp <= ?",
                new String[]{String.valueOf(reminderId), String.valueOf(dayStart), String.valueOf(dayEnd)}
        );
        db.close();
        Log.d("DatabaseHelper", "🗑️ Deleted reminders=" + del + " for ID=" + reminderId);
    }

    // ========================= UI HELPERS (MedicineEditorFragment) =========================

    public static class DrugData {
        public long id;
        public String name;
        public String description;
        public String country;
        public String manufacturer;
        public String form;
        public String drugType;
        public String course;
    }

    public DrugData getDrugDataByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, name, description, country, manufacturer, form, drug_type, course " +
                        "FROM drugs WHERE name = ? LIMIT 1",
                new String[]{name}
        );

        DrugData d = null;
        if (c.moveToFirst()) {
            d = new DrugData();
            d.id = c.getLong(0);
            d.name = c.getString(1);
            d.description = c.getString(2);
            d.country = c.getString(3);
            d.manufacturer = c.getString(4);
            d.form = c.getString(5);
            d.drugType = c.getString(6);
            d.course = c.getString(7);
        }

        c.close();
        db.close();
        return d;
    }

    public long insertDrugFull(DrugData d) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("name", d.name);
        cv.put("description", d.description);
        cv.put("country", d.country);
        cv.put("manufacturer", d.manufacturer);
        cv.put("form", d.form);
        cv.put("drug_type", d.drugType);
        cv.put("course", d.course);

        // старые поля
        cv.put("dosage", "");
        cv.put("active_substance", "");
        cv.put("indication", "");

        long id = db.insert("drugs", null, cv);
        db.close();
        return id;
    }

    public void updateDrugFull(long drugId, DrugData d) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("name", d.name);
        cv.put("description", d.description);
        cv.put("country", d.country);
        cv.put("manufacturer", d.manufacturer);
        cv.put("form", d.form);
        cv.put("drug_type", d.drugType);
        cv.put("course", d.course);

        db.update("drugs", cv, "id=?", new String[]{String.valueOf(drugId)});
        db.close();
    }

    // оставляем метод, чтобы старый код не падал
    public void updateRemindersDrugName(long drugId, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put("drug_name", newName);
            db.update("reminders", cv, "drug_id=?", new String[]{String.valueOf(drugId)});
        } catch (Exception ignored) {}
        db.close();
    }

    // для списков
    public ArrayList<String> getAllDrugNames() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM drugs ORDER BY name ASC", null);
        if (c.moveToFirst()) {
            do list.add(c.getString(0));
            while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public ArrayList<String> searchDrugNames(String query) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT name FROM drugs WHERE LOWER(name) LIKE ? ORDER BY name ASC",
                new String[]{"%" + query.toLowerCase(Locale.ROOT) + "%"}
        );
        if (c.moveToFirst()) {
            do list.add(c.getString(0));
            while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // если где-то ещё используется
    public Cursor getReminderById(long reminderId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT id, timestamp, time, status, schedule FROM reminders WHERE id=? LIMIT 1",
                new String[]{String.valueOf(reminderId)}
        );
    }


    public void deleteReminderItems(long reminderId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("reminder_items", "reminder_id=?", new String[]{String.valueOf(reminderId)});
        db.close();
    }

    public void updateReminderSchedule(long reminderId, String schedule) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("schedule", schedule == null ? "" : schedule);
        db.update("reminders", cv, "id=?", new String[]{String.valueOf(reminderId)});
        db.close();
    }

    // ✅ reminders: timestamp + schedule
    public Cursor getReminderByIdFull(long reminderId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT id, timestamp, time, schedule FROM reminders WHERE id=? LIMIT 1",
                new String[]{String.valueOf(reminderId)}
        );
    }

    // ✅ items: список лекарств + дозы по reminder_id
    public Cursor getReminderItemsForReminder(long reminderId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT ri.drug_id, d.name, COALESCE(ri.dose,'') AS dose " +
                        "FROM reminder_items ri " +
                        "JOIN drugs d ON d.id = ri.drug_id " +
                        "WHERE ri.reminder_id = ? " +
                        "ORDER BY d.name ASC",
                new String[]{String.valueOf(reminderId)}
        );
    }

    public String getDrugCourseByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT course FROM drugs WHERE name = ? LIMIT 1",
                new String[]{name}
        );

        String res = "";
        if (c.moveToFirst()) res = c.getString(0);

        c.close();
        db.close();

        return res == null ? "" : res;
    }

    // ========================= TEMPLATES =========================

    // ✅ сохранить шаблон из draft (один шаблон = набор лекарств + расписание)
    public long saveTemplateFromDraft(String title, com.example.pills.ui.main.ReminderDraft d) {
        if (d == null) return -1;

        SQLiteDatabase db = getWritableDatabase();

        if (title == null) title = "";
        title = title.trim();
        if (title.isEmpty()) title = "Курс";

        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("schedule", d.schedule == null ? "" : d.schedule);
        cv.put("times_per_day", d.timesPerDay <= 0 ? 1 : d.timesPerDay);
        cv.put("repeat_minutes", d.repeatMinutes < 0 ? 0 : d.repeatMinutes);
        cv.put("created_at", System.currentTimeMillis());

        long templateId = db.insert("reminder_templates", null, cv);

        // items
        if (d.items != null) {
            for (com.example.pills.ui.main.ReminderDraft.Item it : d.items) {
                if (it == null || it.drugId <= 0) continue;

                ContentValues ci = new ContentValues();
                ci.put("template_id", templateId);
                ci.put("drug_id", it.drugId);
                ci.put("dose", it.getDoseText() == null ? "" : it.getDoseText());
                ci.put("course_days", it.courseDays <= 0 ? 30 : it.courseDays);
                db.insert("template_items", null, ci);
            }
        }

        db.close();
        return templateId;
    }

    public java.util.ArrayList<com.example.pills.ui.main.TemplateRecord> getAllTemplates() {
        java.util.ArrayList<com.example.pills.ui.main.TemplateRecord> list = new java.util.ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, title, schedule, times_per_day, repeat_minutes, created_at " +
                        "FROM reminder_templates " +
                        "ORDER BY created_at DESC",
                null
        );

        while (c.moveToNext()) {
            list.add(new com.example.pills.ui.main.TemplateRecord(
                    c.getLong(0),
                    c.getString(1),
                    c.getString(2),
                    c.getInt(3),
                    c.getInt(4),
                    c.getLong(5)
            ));
        }

        c.close();
        db.close();
        return list;
    }

    // ✅ собрать draft по шаблону (для "скопировать")
    public com.example.pills.ui.main.ReminderDraft buildDraftFromTemplate(long templateId) {

        com.example.pills.ui.main.ReminderDraft d = new com.example.pills.ui.main.ReminderDraft();

        SQLiteDatabase db = getReadableDatabase();

        Cursor ct = db.rawQuery(
                "SELECT schedule, times_per_day, repeat_minutes " +
                        "FROM reminder_templates WHERE id=? LIMIT 1",
                new String[]{String.valueOf(templateId)}
        );

        if (ct.moveToFirst()) {
            d.schedule = ct.getString(0);
            d.timesPerDay = ct.getInt(1);
            d.repeatMinutes = ct.getInt(2);
            d.scheduleConfigured = true;
        }
        ct.close();

        Cursor ci = db.rawQuery(
                "SELECT ti.drug_id, d2.name, COALESCE(ti.dose,''), COALESCE(ti.course_days,30) " +
                        "FROM template_items ti " +
                        "JOIN drugs d2 ON d2.id = ti.drug_id " +
                        "WHERE ti.template_id=? " +
                        "ORDER BY d2.name ASC",
                new String[]{String.valueOf(templateId)}
        );

        while (ci.moveToNext()) {
            long drugId = ci.getLong(0);
            String name = ci.getString(1);
            String dose = ci.getString(2);
            int courseDays = ci.getInt(3);

            // dose "500 мг" -> value/unit
            String val = "";
            String unit = "мг";
            if (dose != null && dose.contains(" ")) {
                String[] p = dose.split(" ", 2);
                val = p[0];
                unit = p[1];
            } else if (dose != null && !dose.trim().isEmpty()) {
                val = dose.trim();
            }

            d.items.add(new com.example.pills.ui.main.ReminderDraft.Item(drugId, name, val, unit, courseDays));
        }

        ci.close();
        db.close();

        return d;
    }

    public void deleteTemplate(long templateId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("reminder_templates", "id=?", new String[]{String.valueOf(templateId)});
        db.close();
    }


}
