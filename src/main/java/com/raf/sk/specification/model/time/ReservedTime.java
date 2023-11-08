package com.raf.sk.specification.model.time;

import com.raf.sk.specification.ScheduleUtils;
import com.raf.sk.specification.model.Day;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a time that is reserved for a specific day.
 * <p>
 * This class is used to represent a time that is reserved for a specific day.
 * It contains the day, the start and end time, and the start and end date.
 * <p>
 * This class implements the {@link Time} interface.
 * <p>
 * This class is immutable.
 *
 * @see Time
 * @see Day
 */
public class ReservedTime implements Time<LocalDate> {

    private Day day;
    private int startTime, endTime;
    private LocalDate startDate;
    private LocalDate endDate;

    public ReservedTime(Day day, int startTime, int endTime, LocalDate startDate, LocalDate endDate) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ReservedTime(int startTime, int endTime, LocalDate date) {
        this.day = ScheduleUtils.getInstance().getDayFromDate(date);
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = date;
        this.endDate = date;
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
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Unusable method, but it's here to make the code compile.
     */
    @Override
    public LocalDate getDate() {
        return null;
    }

    /**
     * Unusable method, but it's here to make the code compile.
     */
    @Override
    public void setDate(LocalDate date) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservedTime that = (ReservedTime) o;
        return startTime == that.startTime && endTime == that.endTime && day == that.day && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public String toString() {
        return "ReservedTime{" +
                "day=" + day +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
