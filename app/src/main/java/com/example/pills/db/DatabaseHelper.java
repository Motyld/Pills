package com.example.pills.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "medapp.db";
    public static final int DB_VERSION = 6;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // --- Таблица пользователей ---
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

        // --- Таблица лекарств ---
        db.execSQL(
                "CREATE TABLE drugs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "dosage TEXT, " +
                        "description TEXT" +
                        ");"
        );

        // --- Таблица напоминаний ---
        db.execSQL(
                "CREATE TABLE reminders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "drug_id INTEGER, " +
                        "time TEXT, " +
                        "date TEXT, " +
                        "days TEXT, " +
                        "status TEXT DEFAULT 'none'," +
                        "FOREIGN KEY(drug_id) REFERENCES drugs(id)" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 6) {

            db.execSQL("ALTER TABLE users ADD COLUMN fullName TEXT;");
            db.execSQL("ALTER TABLE users ADD COLUMN email TEXT;");
            db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT;");

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
        }
    }

}
