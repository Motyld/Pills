package com.example.pills.ui.main;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ModernCalendarAdapter extends RecyclerView.Adapter<ModernCalendarAdapter.DayViewHolder> {

    private final ArrayList<Date> dates;
    private final OnDateClickListener listener;
    private int selectedPosition = -1;

    private final SimpleDateFormat formatDay = new SimpleDateFormat("d", Locale.getDefault());
    private final SimpleDateFormat formatWeek = new SimpleDateFormat("EE", new Locale("ru"));

    public interface OnDateClickListener {
        void onDateClick(Date date, int position);
    }

    public ModernCalendarAdapter(ArrayList<Date> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_modern, parent, false);

        // Задаем фиксированную ширину, чтобы буквы и числа выравнивались
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
        lp.width = (int) (60 * parent.getContext().getResources().getDisplayMetrics().density);
        v.setLayoutParams(lp);

        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Date date = dates.get(position);

        holder.tvWeekDay.setText(formatWeek.format(date).replace(".", "")); // убираем точку, если есть
        holder.tvDayNumber.setText(formatDay.format(date));

        boolean isSelected = position == selectedPosition;

        holder.bgSelected.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        holder.tvDayNumber.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
        holder.tvWeekDay.setTextColor(isSelected ? Color.BLACK : Color.parseColor("#6E6E6E"));

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            int old = selectedPosition;
            selectedPosition = currentPos;

            if (old != -1) notifyItemChanged(old);
            notifyItemChanged(currentPos);

            listener.onDateClick(dates.get(currentPos), currentPos);
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public void setSelectedPosition(int pos) {
        selectedPosition = pos;
        notifyItemChanged(pos);
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekDay, tvDayNumber;
        View bgSelected;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekDay = itemView.findViewById(R.id.tvWeekDay);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            bgSelected = itemView.findViewById(R.id.bgSelected);
        }
    }
}
