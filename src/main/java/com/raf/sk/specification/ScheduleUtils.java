package com.raf.sk.specification;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.raf.sk.specification.model.Appointment;
import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.ScheduleRoom;
import com.raf.sk.specification.model.time.FreeTime;
import com.raf.sk.specification.model.time.ReservedTime;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for schedule operations.
 */
public class ScheduleUtils {

    private static volatile ScheduleUtils instance;

    public static ScheduleUtils getInstance() {
        if (instance == null) {
            synchronized (ScheduleUtils.class) {
                if (instance == null)
                    instance = new ScheduleUtils();
            }
        }
        return instance;
    }

    public Day getDayFromDate(LocalDate date) {
        return Day.values()[date.getDayOfWeek().getValue() - 1];
    }

    // Appointment date checkers
    public boolean areTwoAppointmentsHaveSameDate(Appointment a1, Appointment a2) {
        return a1.getTime().getStartDate().equals(a2.getTime().getStartDate()) && a1.getTime().getEndDate().equals(a2.getTime().getEndDate());
    }

    public boolean areTwoAppointmentsHaveSameStartDate(Appointment a1, Appointment a2) {
        return a1.getTime().getStartDate().equals(a2.getTime().getStartDate());
    }

    public boolean areTwoAppointmentsHaveSameEndDate(Appointment a1, Appointment a2) {
        return a1.getTime().getEndDate().equals(a2.getTime().getEndDate());
    }

    public boolean isOneAppointmentDateContainsAnother(Appointment a1, Appointment a2) {
        return a1.getTime().getStartDate().isBefore(a2.getTime().getStartDate()) && a1.getTime().getEndDate().isAfter(a2.getTime().getEndDate());
    }

    public boolean isAppointmentInOneDay(Appointment a) {
        return a.getTime().getStartDate().equals(a.getTime().getEndDate());
    }

    // Appointment time checkers
    public boolean areTwoAppointmentsHaveSameTime(Appointment a1, Appointment a2) {
        return a1.getTime().getStartTime() == a2.getTime().getStartTime() && a1.getTime().getEndTime() == a2.getTime().getEndTime();
    }

    public boolean areTwoAppointmentsHaveSameStartTime(Appointment a1, Appointment a2) {
        return a1.getTime().getStartTime() == a2.getTime().getStartTime();
    }

    public boolean areTwoAppointmentsHaveSameEndTime(Appointment a1, Appointment a2) {
        return a1.getTime().getEndTime() == a2.getTime().getEndTime();
    }

    public boolean isOneAppointmentTimeContainsAnother(Appointment a1, Appointment a2) {
        return a1.getTime().getStartTime() < a2.getTime().getStartTime() && a1.getTime().getEndTime() > a2.getTime().getEndTime();
    }

    // Appointment Operations
    public List<Appointment> findAppointmentsByCriteria(Predicate<Appointment> predicate, List<Appointment> appointments) {
        return appointments.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<Appointment> findFreeAppointmentsByDate(LocalDate date, List<Appointment> appointments) {
        return findAppointmentsByCriteria(a -> a.getTime().getDate().equals(date), appointments);
    }

    public List<Appointment> findReservedAppointmentsByDate(LocalDate date, List<Appointment> appointments) {
        return findAppointmentsByCriteria(a -> a.getTime().getStartDate().equals(date), appointments);
    }

    public List<Appointment> findFreeAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, int startTime, int endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getDay().equals(day)
                && a.getTime().getDate().isBefore(endDate)
                && a.getTime().getDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= endTime;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findReservedAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, int startTime, int endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getDay().equals(day)
                && a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= endTime;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findFreeAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, int startTime, int endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getDate().isBefore(endDate)
                && a.getTime().getDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= endTime;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findReservedAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, int startTime, int endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= endTime;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findFreeAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, int startTime, int duration, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a ->  a.getTime().getDate().isBefore(endDate)
                && a.getTime().getDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= startTime + duration;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findReservedAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, int startTime, int duration, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a ->  a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= startTime + duration;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findAppointmentsByRoom(ScheduleRoom room, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getScheduleRoom().equals(room);
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findAppointmentsByData(Map<String, Object> data, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> data.entrySet().stream().allMatch(entry -> {
            String key = entry.getKey();
            Object value = entry.getValue();
            return a.getAllData().containsKey(key) && a.getAllData().get(key).equals(value);
        });
        return ScheduleUtils.getInstance().findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findAppointmentsByData(List<Appointment> appointments, String ... keys) {
        if (keys == null || keys.length == 0)
            throw new IllegalArgumentException("At least one key must be provided for the search.");

        Predicate<Appointment> predicate = appointment -> Arrays.stream(keys).allMatch(key -> appointment.getAllData().containsKey(key));
        return ScheduleUtils.getInstance().findAppointmentsByCriteria(predicate, appointments);
    }

    // Appointment data operations
    public void saveToCSV(List<Appointment> appointments, String path, Properties properties) throws IOException {
        String header = properties.getProperty("csvHeader").replaceAll("\"", "");
        String column = properties.getProperty("columns").replaceAll("\"", "");
        column = column + ",DAY,TIME,ROOM";
        String[] columns = column.split(",");

        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            if (header.equals("ON")) {
                writer.writeNext(columns);
            }
            String col = column;
            appointments.stream()
                    .map(appointment -> getCSVValues(appointment, col))
                    .forEach(values -> writer.writeNext(values.toArray(new String[0])));
        }
    }

    private List<String> getCSVValues(Appointment appointment, String column) {
        List<String> values = new ArrayList<>();

        if (column.contains("START_DATE")) {
            values.add(String.valueOf(appointment.getTime().getStartDate()));
        }
        if (column.contains("END_DATE")) {
            values.add(String.valueOf(appointment.getTime().getEndDate()));
        }

        appointment.getAllData().keySet().stream()
                .filter(s -> column.toUpperCase().contains(s.toUpperCase()))
                .map(s -> String.valueOf(appointment.getAllData().get(s)))
                .forEach(values::add);

        values.addAll(Arrays.asList(
                String.valueOf(appointment.getTime().getDay()),
                appointment.getTime().getStartTime() + "-" + appointment.getTime().getEndTime(),
                appointment.getScheduleRoom().getName()
        ));

        return values;
    }

    public void saveToJSON(List<Appointment> appointments, String path, Properties properties) throws IOException {

    }

}
