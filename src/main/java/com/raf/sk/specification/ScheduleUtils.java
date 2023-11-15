package com.raf.sk.specification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import com.raf.sk.specification.model.Appointment;
import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.ScheduleRoom;
import com.raf.sk.specification.model.time.ReservedTime;
import com.raf.sk.specification.model.time.Time;
import com.raf.sk.specification.model.adapter.TimeAdapter;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for schedule operations.
 */
@SuppressWarnings("unused")
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

    public Configuration loadConfiguration(Properties properties) {
        return new Configuration.Builder()
                .workingTime(properties.getProperty("workingTime").replaceAll("\"", "").split("-"))
                .startDate(LocalDate.parse(properties.getProperty("startDate").replaceAll("\"", "")))
                .endDate(LocalDate.parse(properties.getProperty("endDate").replaceAll("\"", "")))
                .freeDays(Arrays.stream(properties.getProperty("freeDays").replaceAll("\"", "").split(","))
                        .map(dayString -> Day.valueOf(dayString.toUpperCase()))
                        .toArray(Day[]::new))
                .holidays(properties.getProperty("holidays").replaceAll("\"", "").replaceAll("\\.", "-").split(","))
                .rooms(properties.getProperty("rooms").replaceAll("\"", "").split(","))
                .equipment(properties.getProperty("equipment").replaceAll("\"", "").split(","))
                .csvHeader(properties.getProperty("csvHeader").equalsIgnoreCase("ON"))
                .columns(properties.getProperty("columns").replaceAll("\"", ""))
                .build();
    }

    public Day getDayFromDate(LocalDate date) {
        return Day.values()[date.getDayOfWeek().getValue() - 1];
    }

    private String timeAddition(String time, String duration) {
        String[] timeSplit = time.split(":");
        String[] durationSplit = duration.split(":");
        int hours = Integer.parseInt(timeSplit[0]) + Integer.parseInt(durationSplit[0]);
        int minutes = Integer.parseInt(timeSplit[1]) + Integer.parseInt(durationSplit[1]);
        if (minutes >= 60) {
            hours++;
            minutes -= 60;
        }
        return String.format("%02d:%02d", hours, minutes);
    }

    public int[] getTimeComponents(String time) {
        String[] timeParts = time.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = (timeParts.length == 2) ? Integer.parseInt(timeParts[1]) : 0;
        return new int[]{hours, minutes};
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
        return a1.getTime().getStartTime().equals(a2.getTime().getStartTime()) && a1.getTime().getEndTime().equals(a2.getTime().getEndTime());
    }

    public boolean areTwoAppointmentsHaveSameStartTime(Appointment a1, Appointment a2) {
        return a1.getTime().getStartTime().equals(a2.getTime().getStartTime());
    }

    public boolean areTwoAppointmentsHaveSameEndTime(Appointment a1, Appointment a2) {
        return a1.getTime().getEndTime().equals(a2.getTime().getEndTime());
    }

    public boolean isOneAppointmentTimeContainsAnother(Appointment a1, Appointment a2) {
        int[] a1Start = getTimeComponents(a1.getTime().getStartTime());
        int[] a1End = getTimeComponents(a1.getTime().getEndTime());
        int[] a2Start = getTimeComponents(a2.getTime().getStartTime());
        int[] a2End = getTimeComponents(a2.getTime().getEndTime());

        if (a1Start[0] < a2Start[0] && a1End[0] > a2End[0]) return true;
        else if (a1Start[0] == a2Start[0] && a1Start[1] <= a2Start[1] && a1End[0] > a2End[0]) return true;
        else if (a1Start[0] < a2Start[0] && a1End[0] == a2End[0] && a1End[1] >= a2End[1]) return true;
        return (a1Start[0] == a2Start[0] && a1Start[1] <= a2Start[1] && a1End[0] == a2End[0] && a1End[1] >= a2End[1]);
    }

    private boolean isOneTimeContainsAnother(Appointment a, String startTime, String endTime) {
        ReservedTime time = new ReservedTime(null, startTime, endTime, null, null);
        return isOneAppointmentTimeContainsAnother(a, new Appointment(time, null));
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

    public List<Appointment> findFreeAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, String startTime, String endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getDay().equals(day)
                && a.getTime().getDate().isBefore(endDate)
                && a.getTime().getDate().isAfter(startDate)
                && isOneTimeContainsAnother(a, String.valueOf(startTime), String.valueOf(endTime));
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findReservedAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, String startTime, String endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getDay().equals(day)
                && a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && isOneTimeContainsAnother(a, String.valueOf(startTime), String.valueOf(endTime));
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findFreeAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, String startTime, String endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getDate().isBefore(endDate)
                && a.getTime().getDate().isAfter(startDate)
                && isOneTimeContainsAnother(a, String.valueOf(startTime), String.valueOf(endTime));
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findReservedAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, String startTime, String endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && isOneTimeContainsAnother(a, String.valueOf(startTime), String.valueOf(endTime));
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findFreeAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, String startTime, String duration, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a ->  a.getTime().getDate().isBefore(endDate)
                && a.getTime().getDate().isAfter(startDate)
                && isOneTimeContainsAnother(a, String.valueOf(startTime), timeAddition(startTime, duration));
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findReservedAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, String startTime, String duration, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a ->  a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && isOneTimeContainsAnother(a, String.valueOf(startTime), timeAddition(startTime, duration));
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
    public void saveToCSV(List<Appointment> appointments, String path, Configuration config) throws IOException {
        boolean header = config.isCsvHeader();
        String column = config.getColumns();
        column = column + ",DAY,TIME,ROOM";
        String[] columns = column.split(",");

        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            if (header) writer.writeNext(columns);
            String h = column;
            appointments.stream()
                    .map(appointment -> getCSVValues(appointment, h))
                    .forEach(values -> writer.writeNext(values.toArray(new String[0])));
        }
    }

    public void saveToJSON(List<Appointment> appointments, String path) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Time.class, new TimeAdapter())
                .create();
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(appointments, writer);
        }
        catch (Exception ignored) {}
    }

    private List<String> getCSVValues(Appointment appointment, String header) {
        List<String> values = new ArrayList<>();

        if (header.contains("START_DATE")) {
            values.add(String.valueOf(appointment.getTime().getStartDate()));
        }
        if (header.contains("END_DATE")) {
            values.add(String.valueOf(appointment.getTime().getEndDate()));
        }

        appointment.getAllData().keySet().stream()
                .map(s -> String.valueOf(appointment.getAllData().get(s)))
                .forEach(values::add);

        values.addAll(Arrays.asList(
                String.valueOf(appointment.getTime().getDay()),
                appointment.getTime().getStartTime() + "-" + appointment.getTime().getEndTime(),
                appointment.getScheduleRoom().getName()
        ));

        return values;
    }

}
