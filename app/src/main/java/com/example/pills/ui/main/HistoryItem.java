package com.example.pills.ui.main;

public class HistoryItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public int type;
    public String title;        // для заголовков
    public String name;         // для элементов
    public String time;         // для элементов
    public String status;       // для элементов

    // Конструктор для ЗАГОЛОВКА
    public HistoryItem(String title) {
        this.type = TYPE_HEADER;
        this.title = title;
    }

    // Конструктор для ЭЛЕМЕНТА
    public HistoryItem(String name, String time, String status) {
        this.type = TYPE_ITEM;
        this.name = name;
        this.time = time;
        this.status = status;
    }
}
