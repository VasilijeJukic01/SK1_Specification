package com.raf.sk.specification.model;

import com.raf.sk.specification.model.Day;

import java.time.LocalDate;

public class ScheduleTime {

    private Day day;
    private int duration;
    private LocalDate startData;
    private LocalDate endDate;

    public ScheduleTime(Day day, int duration, LocalDate startData, LocalDate endDate) {
        this.day = day;
        this.duration = duration;
        this.startData = startData;
        this.endDate = endDate;
    }

    public ScheduleTime(Day day, int duration, LocalDate startData) {
        this.day = day;
        this.duration = duration;
        this.startData = startData;
    }

    public ScheduleTime(Day day, int duration) {
        this.day = day;
        this.duration = duration;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDate getStartData() {
        return startData;
    }

    public void setStartData(LocalDate startData) {
        this.startData = startData;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
