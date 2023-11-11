package com.raf.sk.specification.model;

import com.raf.sk.specification.model.time.Time;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Appointment class.
 * <p>
 * An appointment is a schedule object that has a time and a room.
 * </p>
 */
public class Appointment extends ScheduleObject {

    private final Time<LocalDate> time;
    private final ScheduleRoom scheduleRoom;

    public Appointment(Time<LocalDate> time, ScheduleRoom scheduleRoom) {
        super.data = new HashMap<>();
        this.time = time;
        this.scheduleRoom = scheduleRoom;
    }

    public Appointment(Time<LocalDate> time, ScheduleRoom scheduleRoom, Map<String, Object> data) {
        super.data = data;
        this.time = time;
        this.scheduleRoom = scheduleRoom;
    }

    // Getters and Setters
    public Time<LocalDate> getTime() {
        return time;
    }

    public ScheduleRoom getScheduleRoom() {
        return scheduleRoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(time, that.time) && Objects.equals(scheduleRoom, that.scheduleRoom);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "data=" + data +
                ", time=" + time +
                ", scheduleRoom=" + scheduleRoom.getName() +
                '}';
    }
}
