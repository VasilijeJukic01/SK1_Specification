package com.raf.sk.specification;

import com.raf.sk.specification.model.Appointment;
import com.raf.sk.specification.model.ScheduleRoom;

import java.util.ArrayList;
import java.util.HashSet;
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
    public abstract void addRoom(ScheduleRoom scheduleRoom);

    /**
     * Adds a new appointment to the schedule.
     *
     * @param appointment - Appointment to be added to the schedule
     */
    public abstract void addAppointment(Appointment appointment);

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
        if (!(new HashSet<>(this.appointments).containsAll(List.of(oldAppointment, newAppointment)))) return;
        // TODO: Finish this method
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
