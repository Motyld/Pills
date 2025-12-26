package com.example.pills.ui.main;

import java.util.Calendar;

public class Reminder {

    private long id;
    private String time;       // "HH:mm"
    private String name;       // теперь это itemsText (список лекарств)
    private long timestamp;

    public Reminder(long id, String time, String name, long timestamp) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public String getTime() { return time; }

    // ✅ ВАЖНО: теперь возвращаем текст-список лекарств
    public String getName() { return name; }

    public long getTimestamp() { return timestamp; }

    // helpers (если где-то нужно)
    private long startOfDay(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long endOfDay(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }
}
