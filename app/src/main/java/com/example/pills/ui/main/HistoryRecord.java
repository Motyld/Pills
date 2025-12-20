package com.example.pills.ui.main;

public class HistoryRecord {
    public int id;
    public String drugName;
    public String time;
    public String date;
    public String status;

    public HistoryRecord(int id, String drugName, String time, String date, String status) {
        this.id = id;
        this.drugName = drugName;
        this.time = time;
        this.date = date;
        this.status = status;
    }
}
