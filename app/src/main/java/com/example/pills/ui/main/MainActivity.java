package com.example.pills.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pills.R;
import com.example.pills.ui.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // ---------- АВТОВХОД ----------
        SharedPreferences pref = getSharedPreferences("auth", MODE_PRIVATE);
        boolean logged = pref.getBoolean("logged", false);

        if (!logged) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // ---------- ПРОСЬБА ВКЛЮЧИТЬ УВЕДОМЛЕНИЯ ----------
        askNotificationsPermissionOnce();

        createNotificationChannel();
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        fab = findViewById(R.id.fabAdd);

        // ---------- КНОПКА ДОБАВЛЕНИЯ ----------
        fab.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddMedicationActivity.class))
        );

        // ---------- НИЖНЕЕ МЕНЮ ----------
        nav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            Fragment fragment;

            if (id == R.id.nav_today) {
                fragment = new TodayFragment();

                fab.show();
                fab.animate().translationY(0).setDuration(150);

            } else if (id == R.id.nav_med) {
                fragment = new MedListFragment();

                fab.show();
                fab.animate().translationY(0).setDuration(150);

            } else if (id == R.id.nav_record) {
                fragment = new RecordFragment();

                fab.hide();

            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();

            return true;
        });

        nav.setSelectedItemId(R.id.nav_today);
    }

    // ------------------------------
    //  ОТКРЫТИЕ НАСТРОЕК УВЕДОМЛЕНИЙ 1 РАЗ
    // ------------------------------
    private void askNotificationsPermissionOnce() {

        SharedPreferences pref = getSharedPreferences("settings", MODE_PRIVATE);
        boolean opened = pref.getBoolean("notif_settings_opened", false);

        // Уже открывали – не беспокоим пользователя
        if (opened) return;

        // Android 13+: нужно разрешение
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                openNotificationSettings();
                pref.edit().putBoolean("notif_settings_opened", true).apply();
            }
        } else {
            // Для старых Android просто откроем настройки уведомлений
            openNotificationSettings();
            pref.edit().putBoolean("notif_settings_opened", true).apply();
        }
    }

    private void openNotificationSettings() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        }

        startActivity(intent);
    }

    // ------------------------------
    //  КАНАЛ УВЕДОМЛЕНИЙ
    // ------------------------------
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelId = "pill_reminders";
            String channelName = "Medication Reminders";
            String channelDesc = "Notifications for medication schedule";

            android.app.NotificationChannel channel =
                    new android.app.NotificationChannel(
                            channelId,
                            channelName,
                            android.app.NotificationManager.IMPORTANCE_HIGH
                    );
            channel.setDescription(channelDesc);

            android.app.NotificationManager manager =
                    getSystemService(android.app.NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }
}
