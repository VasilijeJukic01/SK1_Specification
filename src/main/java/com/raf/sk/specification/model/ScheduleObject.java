package com.raf.sk.specification.model;

import java.util.Map;

public class ScheduleObject {

    protected Map<String, Object> data;

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        if (data.containsKey(key)) {
            return (T) data.get(key);
        }
        else {
            return null;
        }
    }

    public <T> void putData(String key, T value) {
        if (key == null || value == null) return;
        this.data.put(key, value);
    }

    // Getters and Setters
    public Map<String, Object> getAllData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
