package com.raf.sk.specification;

import com.raf.sk.specification.exception.AppointmentNotFoundException;
import com.raf.sk.specification.exception.AppointmentOverlapException;
import com.raf.sk.specification.exception.DifferentDataException;
import com.raf.sk.specification.exception.RoomNotFoundException;
import com.raf.sk.specification.model.*;
import com.raf.sk.specification.model.time.FreeTime;
import com.raf.sk.specification.model.time.Time;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface for managing schedule of appointments.
 * <p>
 * The schedule contains appointments in two dimensions, time and space, and each appointment may have associated data.
 * Appointments can be added, deleted, moved, and searched according to various criteria.
 * The schedule can be loaded from and saved to various file formats.
 *
 */
public abstract class Schedule {

    private List<Appointment> reservedAppointments;
    private List<Appointment> freeAppointments;
    private List<ScheduleRoom> rooms;

    /**
     * Default constructor for initializing the schedule. Creates empty lists for appointments and rooms.
     * <p>
     * Configuration file format is as follows:
     *  workingTime = "%d-%d"
     *  rooms = "%s-%d,%s-%d, ..."
     *
     * @param properties - Schedule configuration file
     */
    public Schedule(Properties properties) {
        initSchedule(properties);
    }

    private void initSchedule(Properties properties) {
        this.reservedAppointments = new ArrayList<>();
        this.freeAppointments = new ArrayList<>();
        this.rooms = new ArrayList<>();
        extractConfigurationData(properties);
    }

    private void extractConfigurationData(Properties properties){
        String roomData = properties.getProperty("rooms").replaceAll("\"", "");
        String timeData = properties.getProperty("workingTime").replaceAll("\"", "");

        String[] workingHours = timeData.split("-");
        int start = Integer.parseInt(workingHours[0]);
        int end = Integer.parseInt(workingHours[1]);
        String[] scheduleRooms = roomData.split(",");

        initFreeRooms(scheduleRooms, start, end);
    }

    private void initFreeRooms(String[] scheduleRooms, int start, int end) {
        Arrays.stream(scheduleRooms).forEach(room -> initFreeRoom(room, start, end));
    }

    private void initFreeRoom(String room, int start, int end) {
        String[] roomInfo = room.split("-");
        ScheduleRoom scheduleRoom = new ScheduleRoom(roomInfo[0], Integer.parseInt(roomInfo[1]));
        rooms.add(scheduleRoom);

        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            FreeTime time = new FreeTime(ScheduleUtils.getInstance().getDayFromDate(currentDate), start, end, currentDate);
            Appointment appointment = new Appointment(time, scheduleRoom);
            freeAppointments.add(appointment);
            currentDate = currentDate.plusDays(1);
        }
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
     * Searches for a room with given name.
     *
     * @param name - Name of the room to be searched for
     */
    public ScheduleRoom getRoomByName(String name) {
        return this.rooms.stream()
                .filter(room -> room.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));
    }

    /**
     * Adds a new appointment to the schedule.
     *
     * @param appointment - Appointment to be added to the schedule
     */
    public void addAppointment(Appointment appointment) {
        if (this.reservedAppointments == null || appointment == null) return;
        if (isAppointmentFree(appointment)) {
            this.reservedAppointments.add(appointment);
            updateFreeAppointments(appointment);
        }
        else throw new AppointmentOverlapException("Appointment cannot be added due overlapping with another appointment");
    }

    public void updateFreeAppointments(Appointment reservedAppointment) {
        List<Appointment> updateList = freeAppointments.stream()
                .filter(freeAppointment -> freeAppointment.getScheduleRoom().equals(reservedAppointment.getScheduleRoom())
                        && freeAppointment.getTime().getDate().isAfter(reservedAppointment.getTime().getStartDate().minusDays(1))
                        && freeAppointment.getTime().getDate().isBefore(reservedAppointment.getTime().getEndDate().plusDays(1))
                        && freeAppointment.getTime().getDay().equals(reservedAppointment.getTime().getDay()))
                .collect(Collectors.toList());

        updateList.forEach(freeAppointment -> updateFreeAppointmentTime(freeAppointment, reservedAppointment));
    }

    private void updateFreeAppointmentTime(Appointment freeAppointment, Appointment reservedAppointment) {
        if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameTime(freeAppointment, reservedAppointment)) {
            freeAppointments.remove(freeAppointment);
        }
        else if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameStartTime(freeAppointment, reservedAppointment)) {
            freeAppointment.getTime().setStartTime(reservedAppointment.getTime().getEndTime());
        }
        else if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameEndTime(freeAppointment, reservedAppointment)) {
            freeAppointment.getTime().setEndTime(reservedAppointment.getTime().getStartTime());
        }
        else if (ScheduleUtils.getInstance().isOneAppointmentTimeContainsAnother(freeAppointment, reservedAppointment)) {
            Time<LocalDate> t1 = freeAppointment.getTime();
            Time<LocalDate>  t2 = reservedAppointment.getTime();

            FreeTime s1 = new FreeTime(t1.getDay(), t1.getStartTime(), t2.getStartTime(), t1.getDate());
            FreeTime s2 = new FreeTime(t1.getDay(), t2.getEndTime(), t1.getEndTime(), t1.getDate());

            Appointment appointment1 = new Appointment(s1, reservedAppointment.getScheduleRoom());
            Appointment appointment2 = new Appointment(s2, reservedAppointment.getScheduleRoom());
            freeAppointments.add(appointment1);
            freeAppointments.add(appointment2);
            freeAppointments.remove(freeAppointment);
        }
    }

    /**
     * Checks if an appointment can be added to the schedule without overlapping with existing appointments.
     *
     * @param appointment - The appointment to be checked for availability
     * @return - True if the appointment time and room are available, false if there's an overlap
     */
    public boolean isAppointmentFree(Appointment appointment) {
        return reservedAppointments.stream()
                .filter(a -> a.getScheduleRoom().equals(appointment.getScheduleRoom())
                        && a.getTime().getDay().equals(appointment.getTime().getDay())
                        && !a.equals(appointment))
                .noneMatch(a -> isDateOverlap(a, appointment) && isTimeOverlap(a, appointment));
    }

    private boolean isDateOverlap(Appointment appointment1, Appointment appointment2) {
        LocalDate startDate1 = appointment1.getTime().getStartDate();
        LocalDate endDate1 = appointment1.getTime().getEndDate();
        LocalDate startDate2 = appointment2.getTime().getStartDate();
        LocalDate endDate2 = appointment2.getTime().getEndDate();

        if (startDate1.equals(startDate2) || endDate1.equals(startDate2)) return true;

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
        if (this.reservedAppointments == null || appointment == null) return;
        this.reservedAppointments.remove(appointment);
    }

    /**
     * Moves the old appointment to a new position with the same associated data.
     *
     * @param oldAppointment - Old appointment to be moved
     * @param newAppointment - New appointment to which the old appointment is moved
     * @throws AppointmentNotFoundException if the oldAppointment does not exist
     * @throws DifferentDataException if appointments have different data
     */
    public void changeAppointment(Appointment oldAppointment, Appointment newAppointment) {
        if (this.reservedAppointments == null || oldAppointment == null || newAppointment == null) return;
        if (!reservedAppointments.contains(oldAppointment)) throw new AppointmentNotFoundException("Appointment not found");
        if (!checkAppointmentData(oldAppointment, newAppointment)) throw new DifferentDataException("Appointments have different data");
        this.reservedAppointments.remove(oldAppointment);
        if (!isAppointmentFree(newAppointment)) {
            this.reservedAppointments.add(oldAppointment);
            throw new AppointmentOverlapException("Appointment cannot be replaced due overlapping with another appointment");
        }
        this.reservedAppointments.add(newAppointment);
    }

    private boolean checkAppointmentData(Appointment app1, Appointment app2) {
        if (app1.getAllData().size() != app2.getAllData().size()) return false;
        return app1.getAllData().entrySet().stream()
                .allMatch(entry1 -> {
                    String key = entry1.getKey();
                    Object value1 = entry1.getValue();
                    Object value2 = app2.getAllData().get(key);
                    return value1.equals(value2);
                });
    }

    /**
     * Finds free appointments based on the given date.
     *
     * @param date - The date for the search.
     * @return - A list of free appointments matching the query.
     */
    public List<Appointment> findFreeAppointmentsByDate(LocalDate date) {
        return ScheduleUtils.getInstance().findFreeAppointmentsByDate(date, freeAppointments);
    }

    /**
     * Finds free appointments based on the specified day, start date, end date, start time, and end time.
     *
     * @param day - The day of the week to search for.
     * @param startDate - The start date of the appointment range.
     * @param endDate - The end date of the appointment range.
     * @param startTime - The start time of the appointment.
     * @param endTime - The end time of the appointment.
     * @return - A list of free appointments matching the query.
     */
    public List<Appointment> findFreeAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, int startTime, int endTime) {
        return ScheduleUtils.getInstance().findFreeAppointmentsByDayAndPeriod(day, startDate, endDate, startTime, endTime, freeAppointments);
    }

    /**
     * Finds free appointments based on the specified start date, end date, start time, and end time.
     *
     * @param startDate - The start date of the appointment range.
     * @param endDate - The end date of the appointment range.
     * @param startTime - The start time of the appointment.
     * @param endTime - The end time of the appointment.
     * @return - A list of free appointments matching the query.
     */
    public List<Appointment> findFreeAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, int startTime, int endTime) {
        return ScheduleUtils.getInstance().findFreeAppointmentsByDateTime(startDate, endDate, startTime, endTime, freeAppointments);
    }

    /**
     * Finds free appointments based on the specified start date, end date, start time, and duration.
     *
     * @param startDate - The start date of the appointment range.
     * @param endDate - The end date of the appointment range.
     * @param startTime - The start time of the appointment.
     * @param duration - The duration of the appointment in minutes.
     * @return - A list of free appointments matching the query.
     */
    public List<Appointment> findFreeAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, int startTime, int duration) {
        return ScheduleUtils.getInstance().findFreeAppointmentsByDateTimeDuration(startDate, endDate, startTime, duration, freeAppointments);
    }

    /**
     * Finds free appointments based on the specified room.
     *
     * @param room - The room to search for.
     * @return - A list of free appointments held in the specified room.
     * @throws RoomNotFoundException if the room does not exist.
     */
    public List<Appointment> findFreeAppointmentsByRoom(ScheduleRoom room) {
        if (!rooms.contains(room)) throw new RoomNotFoundException("Room does not exist");
        return ScheduleUtils.getInstance().findAppointmentsByRoom(room, freeAppointments);
    }

    /**
     * Finds free appointments based on the specified additional data.
     *
     * @param data - A map containing keys and values of additional data for the search.
     * @return - A list of free appointments that contain all the specified keys and values in the additional data.
     */
    public List<Appointment> findFreeAppointmentsByData(Map<String, Object> data) {
        return ScheduleUtils.getInstance().findAppointmentsByData(data, freeAppointments);
    }

    /**
     * Finds free appointments based on the specified keys in the additional data.
     * <p>
     * This method searches for appointments that contain all the specified keys in their additional data.
     * The method returns a list of occupied appointments that match the criteria.
     *
     * @param keys - An array of keys to search for in the additional data of appointments.
     * @return - A list of free appointments that contain all the specified keys in their additional data.
     * @throws IllegalArgumentException if the 'data' array is empty.
     */
    public List<Appointment> findFreeAppointmentsByData(String ... keys) {
        return ScheduleUtils.getInstance().findAppointmentsByData(freeAppointments, keys);
    }

    /**
     * Finds occupied appointments based on the given date.
     *
     * @param date - The date for the search.
     * @return - A list of occupied appointments matching the query.
     */
    public List<Appointment> findReservedAppointmentsByDate(LocalDate date) {
        return ScheduleUtils.getInstance().findReservedAppointmentsByDate(date, reservedAppointments);
    }

    /**
     * Finds occupied appointments based on the specified day, start date, end date, start time, and end time.
     *
     * @param day - The day of the week to search for.
     * @param startDate - The start date of the appointment range.
     * @param endDate - The end date of the appointment range.
     * @param startTime - The start time of the appointment.
     * @param endTime - The end time of the appointment.
     * @return - A list of occupied appointments matching the query.
     */
    public List<Appointment> findReservedAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, int startTime, int endTime) {
        return ScheduleUtils.getInstance().findReservedAppointmentsByDayAndPeriod(day, startDate, endDate, startTime, endTime, reservedAppointments);
    }

    /**
     * Finds occupied appointments based on the specified start date, end date, start time, and end time.
     *
     * @param startDate - The start date of the appointment range.
     * @param endDate - The end date of the appointment range.
     * @param startTime - The start time of the appointment.
     * @param endTime - The end time of the appointment.
     * @return - A list of occupied appointments matching the query.
     */
    public List<Appointment> findReservedAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, int startTime, int endTime) {
        return ScheduleUtils.getInstance().findReservedAppointmentsByDateTime(startDate, endDate, startTime, endTime, reservedAppointments);
    }

    /**
     * Finds occupied appointments based on the specified start date, end date, start time, and duration.
     *
     * @param startDate - The start date of the appointment range.
     * @param endDate - The end date of the appointment range.
     * @param startTime - The start time of the appointment.
     * @param duration - The duration of the appointment in minutes.
     * @return - A list of occupied appointments matching the query.
     */
    public List<Appointment> findReservedAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, int startTime, int duration) {
        return ScheduleUtils.getInstance().findReservedAppointmentsByDateTimeDuration(startDate, endDate, startTime, duration, reservedAppointments);
    }

    /**
     * Finds occupied appointments based on the specified room.
     *
     * @param room - The room to search for.
     * @return - A list of occupied appointments held in the specified room.
     * @throws RoomNotFoundException if the room does not exist.
     */
    public List<Appointment> findReservedAppointmentsByRoom(ScheduleRoom room) {
        if (!rooms.contains(room)) throw new RoomNotFoundException("Room does not exist");
        return ScheduleUtils.getInstance().findAppointmentsByRoom(room, reservedAppointments);
    }

    /**
     * Finds occupied appointments based on the specified additional data.
     *
     * @param data - A map containing keys and values of additional data for the search.
     * @return - A list of occupied appointments that contain all the specified keys and values in the additional data.
     */
    public List<Appointment> findReservedAppointmentsByData(Map<String, Object> data) {
        return ScheduleUtils.getInstance().findAppointmentsByData(data, reservedAppointments);
    }

    /**
     * Finds occupied appointments based on the specified keys in the additional data.
     * <p>
     * This method searches for appointments that contain all the specified keys in their additional data.
     * The method returns a list of occupied appointments that match the criteria.
     *
     * @param keys - An array of keys to search for in the additional data of appointments.
     * @return - A list of occupied appointments that contain all the specified keys in their additional data.
     * @throws IllegalArgumentException if the 'data' array is empty.
     */
    public List<Appointment> findReservedAppointmentsByData(String ... keys) {
        return ScheduleUtils.getInstance().findAppointmentsByData(reservedAppointments, keys);
    }

    /**
     * Loads the schedule from a file in the specified format.
     *
     * @param path - Path to the file from which the schedule is loaded
     */
    public void loadScheduleFromFile(String path) {
        // TODO: Implement this method
    }

    /**
     * Saves the schedule to a file in the specified format.
     *
     * @param path - Path to the file to which the schedule is saved
     * @param format - File format (e.g., JSON, CSV)
     */
    public void saveScheduleToFile(String path, String format) throws IOException {
        if (format.equals("CSV")) ScheduleUtils.getInstance().saveToCSV(reservedAppointments, path);
        else if (format.equals("JSON")) ScheduleUtils.getInstance().saveToJSON(reservedAppointments, path);
    }

    /**
     * Returns the list of reserved appointments.
     *
     * @return - List of reserved appointments
     */
    public List<Appointment> getReservedAppointments() {
        return Collections.unmodifiableList(reservedAppointments);
    }

    /**
     * Returns the list of free appointments.
     *
     * @return - List of free appointments
     */
    public List<Appointment> getFreeAppointments() {
        return Collections.unmodifiableList(freeAppointments);
    }

    /**
     * Returns the list of rooms.
     *
     * @return - List of rooms
     */
    public List<ScheduleRoom> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

}
