package com.example.pills.ui.main;

public class TodayItem {
    public int id;          // reminder ID
    public String drugName;
    public String dosage;
    public String time;     // "HH:mm"
    public int drugId;
    public String status;   // "none", "taken", "missed"
    public String date;

    public TodayItem(long id, String drugName, String dosage, String time, int drugId, String status, String date) {
        this.drugName = drugName;
        this.dosage = dosage;
        this.time = time;
        this.drugId = drugId;
        this.status = status;
        this.date = date;
    }
}
