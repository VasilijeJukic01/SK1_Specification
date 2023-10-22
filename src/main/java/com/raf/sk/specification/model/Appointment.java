package com.raf.sk.specification.model;

import java.util.HashMap;
import java.util.Map;

public class Appointment extends ScheduleObject {

    private ScheduleTime time;
    private ScheduleRoom scheduleRoom;

    public Appointment(ScheduleTime time, ScheduleRoom scheduleRoom) {
        super.data = new HashMap<>();
        this.time = time;
        this.scheduleRoom = scheduleRoom;
    }

    public Appointment(ScheduleTime time, ScheduleRoom scheduleRoom, Map<String, Object> data) {
        super.data = data;
        this.time = time;
        this.scheduleRoom = scheduleRoom;
    }

    // Getters and Setters
    public ScheduleTime getTime() {
        return time;
    }

    public ScheduleRoom getScheduleRoom() {
        return scheduleRoom;
    }

    public void setTime(ScheduleTime time) {
        this.time = time;
    }

    public void setScheduleRoom(ScheduleRoom scheduleRoom) {
        this.scheduleRoom = scheduleRoom;
    }
}
