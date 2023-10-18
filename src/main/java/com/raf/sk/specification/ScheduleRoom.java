package com.raf.sk.specification;

import java.util.ArrayList;
import java.util.List;

public class ScheduleRoom<T> {

    private int capacity;
    private List<Equipment> equipments = new ArrayList<>();
    private T data;

    public ScheduleRoom(int capacity, T data) {
        this.capacity = capacity;
        this.data = data;
    }

    public ScheduleRoom(int capacity, List<Equipment> equipments, T data) {
        this.capacity = capacity;
        this.equipments = equipments;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Equipment> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<Equipment> equipments) {
        this.equipments = equipments;
    }

}
