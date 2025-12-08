package com.example.pills.ui.popup;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

public class ReminderPopupActivity extends AppCompatActivity {

    private long reminderId;
    private TextView tvTitle;
    private Button btnTaken, btnMissed, btnSnooze;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_popup);

        tvTitle = findViewById(R.id.tvTitle);
        btnTaken = findViewById(R.id.btnAccept);
        btnMissed = findViewById(R.id.btnSkip);
        btnSnooze = findViewById(R.id.btnSnooze);

        reminderId = getIntent().getLongExtra("reminderId", -1);
        String title = getIntent().getStringExtra("title");
        tvTitle.setText(title);

        btnTaken.setOnClickListener(v -> markStatus("taken"));
        btnMissed.setOnClickListener(v -> markStatus("missed"));
        btnSnooze.setOnClickListener(v -> {
            Toast.makeText(this, "Отложено на 5 минут", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void markStatus(String status) {
        DatabaseHelper dbh = new DatabaseHelper(this);
        SQLiteDatabase w = dbh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        w.update("reminders", cv, "id = ?", new String[]{String.valueOf(reminderId)});
        w.close();
        Toast.makeText(this, status.equals("taken") ? "Принято" : "Пропущено", Toast.LENGTH_SHORT).show();
        finish();
    }
}
