package com.raf.sk.specification.model.time;

import com.raf.sk.specification.model.Day;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a time that is free for a specific day.
 * <p>
 * This class is used to represent a time that is free for a specific day.
 * It contains the day, the start and end time, and the start date.
 * <p>
 * This class implements the {@link Time} interface.
 * <p>
 * This class is immutable.
 *
 * @see Time
 * @see Day
 */
public class FreeTime implements Time<LocalDate> {

    private Day day;
    private int startTime, endTime;
    private LocalDate date;

    public FreeTime(Day day, int startTime, int endTime, LocalDate startDate) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = startDate;
    }

    @Override
    public Day getDay() {
        return day;
    }

    @Override
    public void setDay(Day day) {
        this.day = day;
    }

    @Override
    public int getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    @Override
    public int getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Unusable method, but it's here to make the code compile.
     */
    @Override
    public LocalDate getStartDate() {
        return null;
    }

    /**
     * Unusable method, but it's here to make the code compile.
     */
    @Override
    public void setStartDate(LocalDate startDate) {

    }

    /**
     * Unusable method, but it's here to make the code compile.
     */
    @Override
    public LocalDate getEndDate() {
        return null;
    }

    /**
     * Unusable method, but it's here to make the code compile.
     */
    @Override
    public void setEndDate(LocalDate endDate) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FreeTime that = (FreeTime) o;
        return startTime == that.startTime && endTime == that.endTime && day == that.day && Objects.equals(date, that.date);
    }

    @Override
    public String toString() {
        return "FreeTime{" +
                "day=" + day +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startDate=" + date +
                '}';
    }

}
