package com.example.pills.ui.main;

import android.Manifest;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.notifications.NotificationScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddMedicationActivity extends AppCompatActivity {

    private EditText etDrugName, etDosage, etDescription;
    private NumberPicker npHour, npMinute;
    private CheckBox dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;
    private Button btnSaveMedication;
    private DatabaseHelper db;

    private ActivityResultLauncher<String> requestNotificationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        db = new DatabaseHelper(this);

        etDrugName = findViewById(R.id.etDrugName);
        etDosage = findViewById(R.id.etDosage);
        etDescription = findViewById(R.id.etDescription);

        npHour = findViewById(R.id.npHour);
        npMinute = findViewById(R.id.npMinute);
        npHour.setMinValue(0);
        npHour.setMaxValue(23);
        npMinute.setMinValue(0);
        npMinute.setMaxValue(59);

        dayMon = findViewById(R.id.dayMon);
        dayTue = findViewById(R.id.dayTue);
        dayWed = findViewById(R.id.dayWed);
        dayThu = findViewById(R.id.dayThu);
        dayFri = findViewById(R.id.dayFri);
        daySat = findViewById(R.id.daySat);
        daySun = findViewById(R.id.daySun);

        btnSaveMedication = findViewById(R.id.btnSaveMedication);

        requestNotificationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {
                        Toast.makeText(this, "Разрешение на уведомления не получено", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnSaveMedication.setOnClickListener(v -> saveMedication());

        checkPermissions();
    }

    private void checkPermissions() {
        // Разрешение на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Проверка точных будильников (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Toast.makeText(this, "Разрешите точные будильники", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void saveMedication() {
        String drugName = etDrugName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (drugName.isEmpty()) {
            Toast.makeText(this, "Введите название лекарства", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues drug = new ContentValues();
        drug.put("name", drugName);
        drug.put("dosage", dosage);
        drug.put("description", description);

        long drugId = db.getWritableDatabase().insert("drugs", null, drug);
        if (drugId <= 0) {
            Toast.makeText(this, "Ошибка сохранения лекарства", Toast.LENGTH_SHORT).show();
            return;
        }

        int hour = npHour.getValue();
        int minute = npMinute.getValue();

        List<Integer> days = new ArrayList<>();
        if (dayMon.isChecked()) days.add(1);
        if (dayTue.isChecked()) days.add(2);
        if (dayWed.isChecked()) days.add(3);
        if (dayThu.isChecked()) days.add(4);
        if (dayFri.isChecked()) days.add(5);
        if (daySat.isChecked()) days.add(6);
        if (daySun.isChecked()) days.add(7);

        ContentValues reminder = new ContentValues();
        reminder.put("drug_id", drugId);
        reminder.put("time", String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        reminder.put("days", days.isEmpty() ? null : days.toString());
        reminder.put("status", "none");

        long reminderId = db.getWritableDatabase().insert("reminders", null, reminder);
        if (reminderId <= 0) {
            Toast.makeText(this, "Ошибка сохранения напоминания", Toast.LENGTH_SHORT).show();
            return;
        }

        if (days.isEmpty()) {
            NotificationScheduler.scheduleOneTime(this, drugName, hour, minute, reminderId);
        } else {
            NotificationScheduler.scheduleRepeating(this, drugName, hour, minute, days, reminderId);
        }

        Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
