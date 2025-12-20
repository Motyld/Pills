package com.example.pills.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String drugName = intent.getStringExtra("drugName");
        Toast.makeText(context, "Напоминание: " + drugName, Toast.LENGTH_LONG).show();

        // Здесь можно вызвать NotificationManager для создания уведомления,
        // либо запуск AlarmReceiver через Intent
    }
}
