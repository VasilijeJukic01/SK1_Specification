package com.raf.sk.specification.model;

import java.util.*;

/**
 * Represents model for room.
 */
public class ScheduleRoom {

    private final String name;
    private final int capacity;
    private List<Equipment> equipment = new ArrayList<>();

    public ScheduleRoom(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public ScheduleRoom(String name, int capacity, List<Equipment> equipment) {
        this.name = name;
        this.capacity = capacity;
        this.equipment = equipment;
    }

    public void addEquipment(Equipment equipment) {
        this.equipment.add(equipment);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Equipment> getEquipment() {
        return Collections.unmodifiableList(equipment);
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleRoom that = (ScheduleRoom) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "ScheduleRoom{" +
                "name='" + name + '\'' +
                ", capacity=" + capacity +
                ", equipment=" + equipment +
                '}';
    }
}
