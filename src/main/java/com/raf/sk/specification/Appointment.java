package com.raf.sk.specification;

public class Appointment<U, V> {

    private String time;
    private ScheduleRoom<U> scheduleRoom;
    private V data;

    public Appointment(String time, ScheduleRoom<U> scheduleRoom, V data) {
        this.time = time;
        this.scheduleRoom = scheduleRoom;
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public ScheduleRoom<U> getScheduleRoom() {
        return scheduleRoom;
    }

    public V getData() {
        return data;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setScheduleRoom(ScheduleRoom<U> scheduleRoom) {
        this.scheduleRoom = scheduleRoom;
    }

    public void setData(V data) {
        this.data = data;
    }
}
