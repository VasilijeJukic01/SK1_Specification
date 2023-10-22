package com.raf.sk.specification.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleRoom extends ScheduleObject {

    private int capacity;
    private List<Equipment> equipment = new ArrayList<>();

    public ScheduleRoom(int capacity) {
        super.data = new HashMap<>();
        this.capacity = capacity;
    }

    public ScheduleRoom(int capacity, List<Equipment> equipment) {
        super.data = new HashMap<>();
        this.capacity = capacity;
        this.equipment = equipment;
    }

    public ScheduleRoom(int capacity, List<Equipment> equipment, Map<String, Object> data) {
        super.data = data;
        this.capacity = capacity;
        this.equipment = equipment;
    }

    // Getters and Setters
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

}
