package com.raf.sk.specification.model.time;

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
 *
 * @see Time
 * @see Day
 */
public class ReservedTime implements Time<LocalDate> {

    private Day day;
    private String startTime, endTime;
    private LocalDate startDate;
    private LocalDate endDate;

    public ReservedTime(Day day, String startTime, String endTime, LocalDate startDate, LocalDate endDate) {
        this.day = day;
        this.startTime = startTime.contains(":") ? startTime : startTime + ":00";
        this.endTime = endTime.contains(":") ? endTime : endTime + ":00";
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ReservedTime(String startTime, String endTime, LocalDate date) {
        this.day = Day.values()[date.getDayOfWeek().getValue() - 1];
        this.startTime = startTime.contains(":") ? startTime : startTime + ":00";
        this.endTime = endTime.contains(":") ? endTime : endTime + ":00";
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
    public String getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override
    public String getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(String endTime) {
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
        return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime) && day == that.day && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
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
