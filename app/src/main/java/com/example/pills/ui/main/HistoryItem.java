package com.example.pills.ui.main;

public class HistoryItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public int type;

    // header
    public String title;

    // item
    public String plannedTime;   // "План: 09:00"
    public String actionTime;    // "Факт: 09:07"
    public String status;        // "Принял"/"Пропустил"
    public String medicines;     // список лекарств

    // Конструктор для ЗАГОЛОВКА
    public HistoryItem(String title) {
        this.type = TYPE_HEADER;
        this.title = title;
    }

    // Конструктор для ЭЛЕМЕНТА
    public HistoryItem(String plannedTime, String actionTime, String status, String medicines) {
        this.type = TYPE_ITEM;
        this.plannedTime = plannedTime;
        this.actionTime = actionTime;
        this.status = status;
        this.medicines = medicines;
    }
}
