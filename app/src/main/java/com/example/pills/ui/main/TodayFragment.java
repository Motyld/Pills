package com.example.pills.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private RecyclerView rvCalendar, rvToday;
    private ModernCalendarAdapter calendarAdapter;
    private ReminderAdapter todayAdapter;
    private TextView titleDate;
    private DatabaseHelper db;
    private BroadcastReceiver refreshReceiver;

    private Date selectedDate;
    private static final int REQUEST_REMINDER = 100;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_today, container, false);

        db = new DatabaseHelper(requireContext());
        requestNotificationPermission();

        titleDate = v.findViewById(R.id.titleDate);
        rvCalendar = v.findViewById(R.id.rvCalendar);
        rvToday = v.findViewById(R.id.rvToday);

        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));

        setupCalendarAndLoad();
        setupBroadcastReceiver();

        return v;
    }

    private void setupBroadcastReceiver() {
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TodayFragment", "📡 REFRESH Broadcast received!");
                refreshCurrentList();
            }
        };

        IntentFilter filter = new IntentFilter("com.example.pills.REFRESH_REMINDERS");
        ContextCompat.registerReceiver(
                requireActivity(),
                refreshReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
        Log.d("TodayFragment", "✅ BroadcastReceiver registered");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshReceiver != null) {
            try {
                requireActivity().unregisterReceiver(refreshReceiver);
            } catch (Exception ignored) {}
        }
        if (db != null) db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCurrentList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_REMINDER && resultCode == Activity.RESULT_OK) {
            refreshCurrentList();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    private void setupCalendarAndLoad() {
        ArrayList<Date> days = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < 60; i++) {
            days.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        rvCalendar.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        calendarAdapter = new ModernCalendarAdapter(days, (date, pos) -> {
            selectedDate = date;
            titleDate.setText(formatTitle(date));
            loadRemindersForDate(date);
            calendarAdapter.setSelectedPosition(pos);
            smoothScrollTo(pos);
        });

        rvCalendar.setAdapter(calendarAdapter);
        new LinearSnapHelper().attachToRecyclerView(rvCalendar);

        selectedDate = days.get(0);
        calendarAdapter.setSelectedPosition(0);
        titleDate.setText(formatTitle(selectedDate));
        loadRemindersForDate(selectedDate);
    }

    public void refreshCurrentList() {
        if (selectedDate != null) loadRemindersForDate(selectedDate);
    }

    // ✅ ВАЖНО: теперь грузим сгруппировано (одно время -> список лекарств)
    public void loadRemindersForDate(Date date) {
        ArrayList<Reminder> list = new ArrayList<>();

        long start = startOfDay(date);
        long end = endOfDay(date);

        Cursor c = db.getTodaysGroupedReminders(start, end);
        if (c != null) {
            while (c.moveToNext()) {
                long reminderId = c.getLong(0);
                String time = c.getString(1);
                long ts = c.getLong(2);
                String items = c.getString(3); // много строк

                if (items == null) items = "";

                // ⚠️ Мы используем старый класс Reminder как контейнер:
                // name = items (список лекарств)
                list.add(new Reminder(reminderId, time, items, ts));
            }
            c.close();
        }

        todayAdapter = new ReminderAdapter(list, reminder -> {
            long reminderId = reminder.getId();

            Intent i = new Intent(requireContext(), AddMedicationActivity.class);
            i.putExtra(AddMedicationActivity.EXTRA_EDIT_REMINDER_ID, reminderId);
            startActivity(i);
        });

        rvToday.setAdapter(todayAdapter);
    }

    private long startOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long endOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    private String formatTitle(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar today = Calendar.getInstance();
        if (isSameDay(cal, today)) return "Сегодня";
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(cal, tomorrow)) return "Завтра";
        return new SimpleDateFormat("d MMM", new Locale("ru")).format(date);
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void smoothScrollTo(int pos) {
        LinearSmoothScroller scroller =
                new LinearSmoothScroller(getContext()) {
                    @Override
                    protected int getHorizontalSnapPreference() {
                        return SNAP_TO_START;
                    }
                };
        scroller.setTargetPosition(pos);
        ((LinearLayoutManager) rvCalendar.getLayoutManager()).startSmoothScroll(scroller);
    }
}
