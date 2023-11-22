package com.raf.sk.specification;

import com.raf.sk.specification.model.Day;

import java.time.LocalDate;

final class Configuration {

    private String[] workingTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private Day[] freeDays;
    private String[] holidays;
    private String[] rooms;
    private String[] equipment;
    private boolean csvHeader;
    private String columns;

    private Configuration() {

    }

    public static class Builder {
        private String[] workingTime;
        private LocalDate startDate;
        private LocalDate endDate;
        private Day[] freeDays;
        private String[] holidays;
        private String[] rooms;
        private String[] equipment;
        private boolean csvHeader;
        private String columns;

        public Builder() {

        }

        public Builder workingTime(String[] workingTime) {
            this.workingTime = workingTime;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder freeDays(Day[] freeDays) {
            this.freeDays = freeDays;
            return this;
        }

        public Builder holidays(String[] holidays) {
            this.holidays = holidays;
            return this;
        }

        public Builder rooms(String[] rooms) {
            this.rooms = rooms;
            return this;
        }

        public Builder equipment(String[] equipment) {
            this.equipment = equipment;
            return this;
        }

        public Builder csvHeader(boolean csvHeader) {
            this.csvHeader = csvHeader;
            return this;
        }

        public Builder columns(String columns) {
            this.columns = columns;
            return this;
        }

        public Configuration build() {
            Configuration config = new Configuration();
            config.workingTime = this.workingTime;
            config.startDate = this.startDate;
            config.endDate = this.endDate;
            config.freeDays = this.freeDays;
            config.holidays = this.holidays;
            config.rooms = this.rooms;
            config.equipment = this.equipment;
            config.csvHeader = this.csvHeader;
            config.columns = this.columns;
            return config;
        }
    }

    public String[] getWorkingTime() {
        return workingTime;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Day[] getFreeDays() {
        return freeDays;
    }

    public String[] getHolidays() {
        return holidays;
    }

    public String[] getRooms() {
        return rooms;
    }

    public String[] getEquipment() {
        return equipment;
    }

    public boolean isCsvHeader() {
        return csvHeader;
    }

    public String getColumns() {
        return columns;
    }
}
