package com.raf.sk.specification;

public final class Manager {

    private static Schedule schedule;

    public static void setSchedule(Schedule s) {
        schedule = s;
    }

    public static Schedule getSchedule() {
        return schedule;
    }

}
