package com.example.pills.ui.main;

import java.io.Serializable;
import java.util.ArrayList;

public class ReminderDraft implements Serializable {

    public String schedule = "";

    public int startYear;
    public int startMonth;  // 0..11
    public int startDay;
    public int startHour = -1;
    public int startMinute = -1;

    public int timesPerDay = 1;
    public int repeatMinutes = 0;

    public boolean editMode = false;
    public long editReminderId = -1;

    public long editDayStart = 0;
    public long editDayEnd = 0;

    public boolean scheduleConfigured = false;

    public ArrayList<Item> items = new ArrayList<>();

    public static class Item implements Serializable {

        public long drugId = -1;
        public String drugName = "";

        // ✅ дозировка: число + единица
        public String doseValue = "";
        public String doseUnit = "мг";

        // ✅ курс для конкретного лекарства
        public int courseDays = 30;

        public Item() {}

        // ✅ (drugId, name) — когда дозу ещё не ввели
        public Item(long drugId, String drugName) {
            this.drugId = drugId;
            this.drugName = safe(drugName);
            this.doseValue = "";
            this.doseUnit = "мг";
            this.courseDays = 30;
        }

        // ✅ (drugId, name, doseText) — поддержка старого кода ("500 мг" или "1 таб")
        public Item(long drugId, String drugName, String doseText) {
            this.drugId = drugId;
            this.drugName = safe(drugName);
            applyDoseText(doseText);
            this.courseDays = 30;
        }

        // ✅ (drugId, name, doseValue, doseUnit)
        public Item(long drugId, String drugName, String doseValue, String doseUnit) {
            this.drugId = drugId;
            this.drugName = safe(drugName);
            this.doseValue = safe(doseValue);
            this.doseUnit = normalizeUnit(doseUnit);
            this.courseDays = 30;
        }

        // ✅ (drugId, name, doseValue, doseUnit, courseDays)
        public Item(long drugId, String drugName, String doseValue, String doseUnit, int courseDays) {
            this.drugId = drugId;
            this.drugName = safe(drugName);
            this.doseValue = safe(doseValue);
            this.doseUnit = normalizeUnit(doseUnit);
            this.courseDays = courseDays > 0 ? courseDays : 30;
        }

        // ✅ удобно для БД/адаптеров
        public String getDoseText() {
            String v = safe(doseValue).trim();
            String u = safe(doseUnit).trim();
            if (v.isEmpty()) return "";
            if (u.isEmpty()) return v;
            return v + " " + u;
        }

        // ================= helpers =================

        private void applyDoseText(String doseText) {
            String s = safe(doseText).trim();

            if (s.isEmpty()) {
                this.doseValue = "";
                this.doseUnit = "мг";
                return;
            }

            // Если есть пробел -> "500 мг"
            if (s.contains(" ")) {
                String[] p = s.split("\\s+", 2);
                this.doseValue = safe(p[0]).trim();
                this.doseUnit = normalizeUnit(p.length > 1 ? p[1] : "мг");
                return;
            }

            // Если нет пробела -> "500мг" или "1таб" — попытаемся разрезать
            String digits = s.replaceAll("[^0-9.,]", "");
            String unit = s.replaceAll("[0-9.,]", "").trim();

            if (!digits.isEmpty()) {
                this.doseValue = digits;
                this.doseUnit = normalizeUnit(unit.isEmpty() ? "мг" : unit);
            } else {
                // если вообще без цифр (например "по необходимости")
                this.doseValue = s;
                this.doseUnit = "";
            }
        }

        private String normalizeUnit(String u) {
            u = safe(u).trim();
            if (u.isEmpty()) return "мг";
            return u;
        }

        private String safe(String s) {
            return s == null ? "" : s;
        }
    }
}
