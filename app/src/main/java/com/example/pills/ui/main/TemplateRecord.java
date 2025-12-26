package com.example.pills.ui.main;

public class TemplateRecord {
    public long id;
    public String title;
    public String schedule;
    public int timesPerDay;
    public int repeatMinutes;
    public long createdAt;

    public TemplateRecord(long id, String title, String schedule, int timesPerDay, int repeatMinutes, long createdAt) {
        this.id = id;
        this.title = title;
        this.schedule = schedule;
        this.timesPerDay = timesPerDay;
        this.repeatMinutes = repeatMinutes;
        this.createdAt = createdAt;
    }
}
