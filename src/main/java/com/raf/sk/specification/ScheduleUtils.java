package com.raf.sk.specification;

import com.opencsv.CSVWriter;
import com.raf.sk.specification.model.Appointment;
import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.ScheduleRoom;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public List<Appointment> findAppointmentsByDate(LocalDate date, List<Appointment> appointments) {
        return findAppointmentsByCriteria(a -> a.getTime().getStartDate().equals(date), appointments);
    }

    public List<Appointment> findAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, int startTime, int endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getDay().equals(day)
                && a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= endTime;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, int startTime, int endTime, List<Appointment> appointments) {
        Predicate<Appointment> predicate = a -> a.getTime().getStartDate().isBefore(endDate)
                && a.getTime().getEndDate().isAfter(startDate)
                && a.getTime().getStartTime() <= startTime
                && a.getTime().getEndTime() >= endTime;
        return findAppointmentsByCriteria(predicate, appointments);
    }

    public List<Appointment> findAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, int startTime, int duration, List<Appointment> appointments) {
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
    public void saveToCSV(List<Appointment> appointments, String path) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            for (Appointment appointment : appointments) {
                String[] data = {String.valueOf(appointment.getTime().getDay()),
                        String.valueOf(appointment.getTime().getStartTime()),
                        String.valueOf(appointment.getTime().getEndTime()),
                        String.valueOf(appointment.getTime().getStartDate()),
                        String.valueOf(appointment.getTime().getEndDate()),
                        appointment.getScheduleRoom().getName(),
                };
                writer.writeNext(data);
            }
        }
    }

    public void saveToJSON(List<Appointment> appointments, String path) throws IOException {
        // TODO
    }

}
