package com.raf.sk.specification;

import com.raf.sk.specification.model.Appointment;
import com.raf.sk.specification.model.ScheduleRoom;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for managing schedule of appointments.
 * <p>
 * The schedule contains appointments in two dimensions, time and space, and each appointment may have associated data.
 * Appointments can be added, deleted, moved, and searched according to various criteria.
 * The schedule can be loaded from and saved to various file formats.
 *
 */
public abstract class Schedule {

    private List<Appointment> appointments;
    private List<ScheduleRoom> rooms;

    public Schedule() {
        initSchedule();
    }

    /**
     * Initializes the schedule.
     */
    private void initSchedule() {
        this.appointments = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }

    /**
     * Adds a room with specific attributes to the schedule.
     *
     * @param scheduleRoom - Room to be added to the schedule
     */
    public void addRoom(ScheduleRoom scheduleRoom) {
        if (this.rooms == null || scheduleRoom == null) return;
        this.rooms.add(scheduleRoom);
    }

    /**
     * Adds a new appointment to the schedule.
     *
     * @param appointment - Appointment to be added to the schedule
     */
    public void addAppointment(Appointment appointment) {
        if (this.appointments == null || appointment == null) return;
        if (isAppointmentFree(appointment))
            this.appointments.add(appointment);
        else throw new IllegalArgumentException("Appointment is not free");
    }

    private boolean isAppointmentFree(Appointment appointment) {
        return appointments.stream()
                .filter(a -> a.getScheduleRoom().equals(appointment.getScheduleRoom()) && !a.equals(appointment))
                .noneMatch(a -> isDateOverlap(a, appointment) && isTimeOverlap(a, appointment));
    }

    private boolean isDateOverlap(Appointment appointment1, Appointment appointment2) {
        LocalDate startDate1 = appointment1.getTime().getStartDate();
        LocalDate endDate1 = appointment1.getTime().getEndDate();
        LocalDate startDate2 = appointment2.getTime().getStartDate();
        LocalDate endDate2 = appointment2.getTime().getEndDate();

        return startDate1.isBefore(endDate2) && endDate1.isAfter(startDate2);
    }

    private boolean isTimeOverlap(Appointment appointment1, Appointment appointment2) {
        int startTime1 = appointment1.getTime().getStartTime();
        int endTime1 = appointment1.getTime().getEndTime();
        int startTime2 = appointment2.getTime().getStartTime();
        int endTime2 = appointment2.getTime().getEndTime();

        return !(endTime1 <= startTime2 || endTime2 <= startTime1);
    }

    /**
     * Deletes the given appointment from the schedule.
     *
     * @param appointment - Appointment to be deleted from the schedule
     */
    public void deleteAppointment(Appointment appointment) {
        if (this.appointments == null || appointment == null) return;
        this.appointments.remove(appointment);
    }

    /**
     * Moves the old appointment to a new position with the same associated data.
     *
     * @param oldAppointment - Old appointment to be moved
     * @param newAppointment - New appointment to which the old appointment is moved
     */
    public void switchAppointment(Appointment oldAppointment, Appointment newAppointment) {
        if (this.appointments == null || oldAppointment == null || newAppointment == null) return;
        if (!appointments.contains(oldAppointment) || !checkAppointmentData(oldAppointment, newAppointment)) return;
        if (!isAppointmentFree(newAppointment)) return;
        this.appointments.remove(oldAppointment);
        this.appointments.add(newAppointment);
    }

    private boolean checkAppointmentData(Appointment app1, Appointment app2) {
        if (app1.getData().size() != app2.getData().size()) return false;
        return app1.getData().entrySet().stream()
                .allMatch(entry1 -> {
                    String key = entry1.getKey();
                    Object value1 = entry1.getValue();
                    Object value2 = app2.getData().get(key);
                    return value1.equals(value2);
                });
    }

    /**
     * Finds available appointments based on the given request.
     *
     * @param request - Request used for searching available appointments
     * @return - List of available appointments matching the request
     */
    public abstract List<Appointment> findFreeAppointments(Request request);

    /**
     * Finds occupied appointments based on the given request.
     *
     * @param request - Request used for searching occupied appointments
     * @return - List of occupied appointments matching the request
     */
    public abstract List<Appointment> findTakenAppointments(Request request);

    /**
     * Loads the schedule from a file in the specified format.
     *
     * @param path - Path to the file from which the schedule is loaded
     */
    public abstract void loadScheduleFromFile(String path);

    /**
     * Saves the schedule to a file in the specified format.
     *
     * @param path - Path to the file to which the schedule is saved
     * @param format - File format (e.g., JSON, CSV)
     */
    public abstract void saveScheduleToFile(String path, String format);

}
