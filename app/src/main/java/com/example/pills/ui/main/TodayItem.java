package com.example.pills.ui.main;

public class TodayItem {
    public int reminderId; // id напоминания (reminders.id)
    public String name;
    public String dosage;
    public String time;
    public int drugId;
    public String status;

    public TodayItem(int reminderId, String name, String dosage, String time, int drugId, String status) {
        this.reminderId = reminderId;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.drugId = drugId;
        this.status = status;
    }
}
