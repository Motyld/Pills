package com.example.pills.ui.main;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private RecyclerView rvCalendar;
    private RecyclerView rvToday;
    private ModernCalendarAdapter calendarAdapter;
    private TodayAdapter todayAdapter;
    private TextView titleDate;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_today, container, false);

        db = new DatabaseHelper(requireContext());

        titleDate = v.findViewById(R.id.titleDate);
        rvCalendar = v.findViewById(R.id.rvCalendar);
        rvToday = v.findViewById(R.id.rvToday);

        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));

        setupCalendarAndLoad();

        return v;
    }

    private void setupCalendarAndLoad() {
        // Создаём список дней так, чтобы СЕГОДНЯ был ПЕРВЫМ (слева)
        ArrayList<Date> days = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        // устанавливаем время в 00:00:00 для корректного сравнения
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // добавляем сегодня и вперед (60 дней). Сегодня будет index=0
        for (int i = 0; i < 60; i++) {
            days.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        rvCalendar.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        calendarAdapter = new ModernCalendarAdapter(days, (date, pos) -> {
            // клик по дате — обновляем заголовок и список
            titleDate.setText(formatTitle(date));
            loadRemindersForDate(date);
            // помечаем выбранную позицию
            calendarAdapter.setSelectedPosition(pos);
            smoothScrollTo(pos);
        });

        rvCalendar.setAdapter(calendarAdapter);

        // Snap helper — центрирование элемента при скролле (если нужен центр — включи)
        LinearSnapHelper snap = new LinearSnapHelper();
        snap.attachToRecyclerView(rvCalendar);

        // По умолчанию выбираем сегодня (index 0)
        int todayPos = 0;
        calendarAdapter.setSelectedPosition(todayPos);
        titleDate.setText(formatTitle(days.get(todayPos)));
        loadRemindersForDate(days.get(todayPos));

        // плавный автоскролл к началу (сегодня слева)
        rvCalendar.post(() -> rvCalendar.smoothScrollToPosition(todayPos));
    }

    private String formatTitle(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Calendar today = Calendar.getInstance(); // now
        if (isSameDay(cal, today)) return "Сегодня";

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(cal, tomorrow)) return "Завтра";

        // иначе: "пт, 25" или "25 нояб." — здесь использую "d MMM" (на русском)
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM", new Locale("ru"));
        return sdf.format(date);
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void loadRemindersForDate(Date date) {
        ArrayList<TodayItem> list = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(date);

        // 1) одноразовые напоминания (date = выбранная дата) и статус = 'none'
        String sqlOneTime =
                "SELECT reminders.id AS r_id, drugs.name, drugs.dosage, reminders.time, reminders.status, reminders.drug_id " +
                        "FROM reminders JOIN drugs ON drugs.id = reminders.drug_id " +
                        "WHERE reminders.date = ? AND (reminders.status IS NULL OR reminders.status = 'none')";

        Cursor c1 = db.getReadableDatabase().rawQuery(sqlOneTime, new String[]{dateStr});
        while (c1.moveToNext()) {
            list.add(new TodayItem(
                    c1.getInt(0),     // reminder id (r_id)
                    c1.getString(1),  // drug name
                    c1.getString(2),  // dosage
                    c1.getString(3),  // time
                    c1.getInt(5),     // drug_id
                    c1.getString(4)   // status
            ));
        }
        c1.close();

        // 2) повторяющиеся расписания (days IS NOT NULL) — проверяем, есть ли в days текущий день недели
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            // Android: Calendar.MONDAY = 2 ... Sunday = 1. Но у тебя в базе, судя по коду, дни вероятно 1..7 (понедельник..воскресенье).
            // В ранних частях проекта ты использовал 1=Mon..7=Sun. Здесь согласую: возьмем ISO-like: 1=Mon ... 7=Sun
            int dowIso; // 1..7 with 1=Mon
            int javaDow = cal.get(Calendar.DAY_OF_WEEK); // SUN=1, MON=2 ...
            // преобразование Java -> ISO (Mon=1..Sun=7)
            dowIso = (javaDow == Calendar.SUNDAY) ? 7 : (javaDow - Calendar.MONDAY + 1);

            String sqlRepeating =
                    "SELECT reminders.id AS r_id, drugs.name, drugs.dosage, reminders.time, reminders.days, reminders.status, reminders.drug_id " +
                            "FROM reminders JOIN drugs ON drugs.id = reminders.drug_id " +
                            "WHERE reminders.days IS NOT NULL AND (reminders.status IS NULL OR reminders.status = 'none')";

            Cursor c2 = db.getReadableDatabase().rawQuery(sqlRepeating, null);
            while (c2.moveToNext()) {
                String daysField = c2.getString(4); // example: "[1, 3, 5]" or "1,3,5"
                if (daysField == null) continue;

                // Нормализуем форму: оставим только цифры и разделители
                String normalized = daysField.replaceAll("[^0-9,]", "");
                String[] parts = normalized.split(",");
                for (String p : parts) {
                    if (p.trim().isEmpty()) continue;
                    try {
                        int d = Integer.parseInt(p.trim());
                        if (d == dowIso) {
                            // показываем
                            list.add(new TodayItem(
                                    c2.getInt(0),
                                    c2.getString(1),
                                    c2.getString(2),
                                    c2.getString(3),
                                    c2.getInt(6),
                                    c2.getString(5)
                            ));
                            break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            c2.close();
        } catch (Exception ignored) {}

        // Сортировка по времени (строка "HH:mm")
        list.sort((a, b) -> a.time.compareTo(b.time));

        // Устанавливаем адаптер
        todayAdapter = new TodayAdapter(list, db);
        rvToday.setAdapter(todayAdapter);

        // если список пуст, можно показать toast (опционально)
        if (list.isEmpty()) {
            // показывать не обязательно, но полезно при отладке
            // Toast.makeText(getContext(), "Напоминаний нет", Toast.LENGTH_SHORT).show();
        }
    }

    private void smoothScrollTo(int pos) {
        LinearSmoothScroller scroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                // SNAP_TO_START — чтобы элемент оказался слева (у тебя требование: сегодня слева)
                return SNAP_TO_START;
            }
        };
        scroller.setTargetPosition(pos);
        rvCalendar.getLayoutManager().startSmoothScroll(scroller);
    }
}
