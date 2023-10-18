package com.raf.sk.specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for managing schedule of appointments.
 * <p>
 * The schedule contains appointments in two dimensions, time and space, and each appointment may have associated data.
 * Appointments can be added, deleted, moved, and searched according to various criteria.
 * The schedule can be loaded from and saved to various file formats.
 *
 * @param <U> - Type of the appointment
 * @param <V> - Type of the room
 */
public abstract class Schedule<U, V> {

    private final List<Appointment<U, V>> appointments = new ArrayList<>();
    private final List<ScheduleRoom<U>> rooms = new ArrayList<>();

    /**
     * Initializes the schedule.
     */
    abstract void initSchedule();

    /**
     * Adds a room with specific attributes to the schedule.
     *
     * @param scheduleRoom - Room to be added to the schedule
     */
    abstract void addRoom(ScheduleRoom<U> scheduleRoom);

    /**
     * Adds a new appointment to the schedule.
     *
     * @param appointment - Appointment to be added to the schedule
     */
    abstract void addAppointment(Appointment<U, V> appointment);

    /**
     * Deletes the given appointment from the schedule.
     *
     * @param appointment - Appointment to be deleted from the schedule
     */
    abstract void deleteAppointment(Appointment<U, V> appointment);

    /**
     * Moves the old appointment to a new position with the same associated data.
     *
     * @param oldAppointment - Old appointment to be moved
     * @param newAppointment - New appointment to which the old appointment is moved
     */
    abstract void switchAppointment(Appointment<U, V> oldAppointment, Appointment<U, V> newAppointment);

    /**
     * Finds available appointments based on the given request.
     *
     * @param request - Request used for searching available appointments
     * @return - List of available appointments matching the request
     */
    abstract List<Appointment<U, V>> findFreeAppointments(Request request);

    /**
     * Finds occupied appointments based on the given request.
     *
     * @param request - Request used for searching occupied appointments
     * @return - List of occupied appointments matching the request
     */
    abstract List<Appointment<U, V>> findTakenAppointments(Request request);

    /**
     * Loads the schedule from a file in the specified format.
     *
     * @param path - Path to the file from which the schedule is loaded
     */
    abstract void loadScheduleFromFile(String path);

    /**
     * Saves the schedule to a file in the specified format.
     *
     * @param path - Path to the file to which the schedule is saved
     * @param format - File format (e.g., JSON, CSV)
     */
    abstract void saveScheduleToFile(String path, String format);

}
