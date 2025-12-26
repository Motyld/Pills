package com.example.pills.ui.main;

public class HistoryRecord {
    public int id;

    public String date;        // "2025-12-26"
    public String plannedTime; // "09:00"
    public String actionTime;  // "09:07"
    public String status;      // "Принял"/"Пропустил"
    public String medicines;   // "• ...\n• ..."

    public HistoryRecord(int id, String date, String plannedTime, String actionTime, String status, String medicines) {
        this.id = id;
        this.date = date;
        this.plannedTime = plannedTime;
        this.actionTime = actionTime;
        this.status = status;
        this.medicines = medicines;
    }
}
