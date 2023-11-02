package com.raf.sk.specification.model;

import com.raf.sk.specification.Schedule;

import java.util.Properties;

public class TestSchedule extends Schedule {

    /**
     * Default constructor for initializing the schedule. Creates empty lists for appointments and rooms.
     *
     * @param properties
     */
    public TestSchedule(Properties properties) {
        super(properties);
    }

    @Override
    public void loadScheduleFromFile(String path) {

    }

    @Override
    public void saveScheduleToFile(String path, String format) {

    }
}
