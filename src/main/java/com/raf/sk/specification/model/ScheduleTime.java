package com.raf.sk.specification.model;

import com.raf.sk.specification.ScheduleUtils;

import java.time.LocalDate;
import java.util.Objects;

public class ScheduleTime {

    private Day day;
    private int startTime, endTime;
    private LocalDate startDate;
    private LocalDate endDate;

    public ScheduleTime(Day day, int startTime, int endTime, LocalDate startDate, LocalDate endDate) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ScheduleTime(int startTime, int endTime, LocalDate date) {
        this.day = ScheduleUtils.getInstance().getDayFromDate(date);
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = date;
        this.endDate = date;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleTime that = (ScheduleTime) o;
        return startTime == that.startTime && endTime == that.endTime && day == that.day && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public String toString() {
        return "ScheduleTime{" +
                "day=" + day +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
