package com.raf.sk.specification;

import com.raf.sk.specification.exception.AppointmentNotFoundException;
import com.raf.sk.specification.exception.AppointmentOverlapException;
import com.raf.sk.specification.exception.DifferentDataException;
import com.raf.sk.specification.exception.RoomNotFoundException;
import com.raf.sk.specification.model.Appointment;
import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.ScheduleRoom;
import com.raf.sk.specification.model.ScheduleTime;

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

    // TODO: Document configuration file format
    /**
     * Default constructor for initializing the schedule. Creates empty lists for appointments and rooms.
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
        initFreeAppointments(properties);
    }

    private void initFreeAppointments(Properties properties){
        String roomData = properties.getProperty("rooms").replaceAll("\"", "");
        String timeData = properties.getProperty("workingTime").replaceAll("\"", "");

        String[] workingHours = timeData.split("-");
        int start = Integer.parseInt(workingHours[0]);
        int end = Integer.parseInt(workingHours[1]);
        String[] scheduleRooms = roomData.split(",");

        initFreeRooms(scheduleRooms, start, end);

    }

    private void initFreeRooms( String[] scheduleRooms, int start, int end) {
        for (String room : scheduleRooms) {
            String[] roomInfo = room.split("-");
            ScheduleRoom scheduleRoom = new ScheduleRoom(roomInfo[0], Integer.parseInt(roomInfo[1]));
            rooms.add(scheduleRoom);
            for (Day day : Day.values()) {
                ScheduleTime scheduleTime = new ScheduleTime(day, start, end, LocalDate.of(2023,1,1),LocalDate.of(2023,12,31));
                Appointment appointment = new Appointment(scheduleTime,scheduleRoom);
                freeAppointments.add(appointment);
            }
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
                        && freeAppointment.getTime().getDay().equals(reservedAppointment.getTime().getDay()))
                .collect(Collectors.toList());

        updateList.forEach(freeAppointment -> updateFreeAppointmentDate(freeAppointment, reservedAppointment));
    }

    private void updateFreeAppointmentDate(Appointment freeAppointment, Appointment reservedAppointment) {
        if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameDate(freeAppointment, reservedAppointment)) {
            freeAppointments.remove(freeAppointment);
            updateFreeAppointmentTime(freeAppointment, reservedAppointment);
        }
        else if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameStartDate(freeAppointment, reservedAppointment)) {
            updateSameStartDateAppointments(freeAppointment, reservedAppointment);
        }
        else if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameEndDate(freeAppointment, reservedAppointment)) {
            updateSameEndDateAppointments(freeAppointment, reservedAppointment);
        }
        else if (ScheduleUtils.getInstance().isOneAppointmentDateContainsAnother(freeAppointment, reservedAppointment)) {
            updateBetweenDatesAppointments(freeAppointment, reservedAppointment);
        }
    }

    private void updateSameStartDateAppointments(Appointment freeAppointment, Appointment reservedAppointment) {
        Appointment appointment;
        ScheduleTime t1 = freeAppointment.getTime();
        ScheduleTime t2 = reservedAppointment.getTime();

        ScheduleTime s = new ScheduleTime(t1.getDay(), t1.getStartTime(), t1.getEndTime(), t1.getStartDate(), t2.getEndDate());

        if (ScheduleUtils.getInstance().isAppointmentInOneDay(reservedAppointment)) {
            freeAppointment.getTime().setStartDate(reservedAppointment.getTime().getEndDate().plusDays(1));
        }
        else freeAppointment.getTime().setStartDate(reservedAppointment.getTime().getEndDate());

        appointment = new Appointment(s, freeAppointment.getScheduleRoom());
        updateFreeAppointmentTime(appointment, reservedAppointment);
    }

    private void updateSameEndDateAppointments(Appointment freeAppointment, Appointment reservedAppointment) {
        Appointment appointment;
        ScheduleTime t1 = freeAppointment.getTime();
        ScheduleTime t2 = reservedAppointment.getTime();

        ScheduleTime s = new ScheduleTime(t1.getDay(), t1.getStartTime(), t1.getEndTime(), t2.getStartDate(), t1.getEndDate());

        if (ScheduleUtils.getInstance().isAppointmentInOneDay(reservedAppointment)) {
            freeAppointment.getTime().setEndDate(reservedAppointment.getTime().getStartDate().minusDays(1));
        }
        else freeAppointment.getTime().setEndDate(reservedAppointment.getTime().getStartDate());

        appointment = new Appointment(s, freeAppointment.getScheduleRoom());
        updateFreeAppointmentTime(appointment, reservedAppointment);
    }

    private void updateBetweenDatesAppointments(Appointment freeAppointment, Appointment reservedAppointment) {
        Appointment appointment1, appointment2;
        ScheduleTime t1 = freeAppointment.getTime();
        ScheduleTime t2 = reservedAppointment.getTime();

        if (ScheduleUtils.getInstance().isAppointmentInOneDay(reservedAppointment)) {
            ScheduleTime s1 = new ScheduleTime(t1.getDay(), t1.getStartTime(), t1.getEndTime(), t1.getStartDate(), t2.getStartDate().minusDays(1));
            ScheduleTime s2 = new ScheduleTime(t1.getDay(), t1.getStartTime(), t1.getEndTime(),t2.getEndDate().plusDays(1), t1.getEndDate());
            appointment1 = new Appointment(s1, freeAppointment.getScheduleRoom());
            appointment2 = new Appointment(s2, freeAppointment.getScheduleRoom());
        }
        else {
            ScheduleTime s3 = new ScheduleTime(t1.getDay(), t1.getStartTime(), t1.getEndTime(), t1.getStartDate(), t2.getStartDate());
            ScheduleTime s4 = new ScheduleTime(t1.getDay(), t1.getStartTime(), t1.getEndTime(), t2.getEndDate(), t1.getEndDate());
            appointment1 = new Appointment(s3, freeAppointment.getScheduleRoom());
            appointment2 = new Appointment(s4, freeAppointment.getScheduleRoom());
        }

        freeAppointments.add(appointment1);
        freeAppointments.add(appointment2);
        ScheduleTime s = new ScheduleTime(t1.getDay(), t1.getStartTime(), t1.getEndTime(), t2.getStartDate(), t2.getEndDate());
        Appointment appointment3 = new Appointment(s, freeAppointment.getScheduleRoom());
        updateFreeAppointmentTime(appointment3, reservedAppointment);
        freeAppointments.remove(freeAppointment);
    }

    private void updateFreeAppointmentTime(Appointment freeAppointment, Appointment reservedAppointment) {
        if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameTime(freeAppointment, reservedAppointment)) {
            freeAppointments.remove(freeAppointment);
        }
        else if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameStartTime(freeAppointment, reservedAppointment)) {
            freeAppointment.getTime().setStartTime(reservedAppointment.getTime().getEndTime());
            freeAppointments.add(freeAppointment);
        }
        else if (ScheduleUtils.getInstance().areTwoAppointmentsHaveSameEndTime(freeAppointment, reservedAppointment)) {
            freeAppointment.getTime().setEndTime(reservedAppointment.getTime().getStartTime());
            freeAppointments.add(freeAppointment);
        }
        else if (ScheduleUtils.getInstance().isOneAppointmentTimeContainsAnother(freeAppointment, reservedAppointment)) {
            ScheduleTime t1 = freeAppointment.getTime();
            ScheduleTime t2 = reservedAppointment.getTime();

            ScheduleTime s1 = new ScheduleTime(t2.getDay(), t1.getStartTime(), t2.getStartTime(), t2.getStartDate(), t2.getEndDate());
            ScheduleTime s2 = new ScheduleTime(t2.getDay(), t2.getEndTime(), t1.getEndTime(), t2.getStartDate(), t2.getEndDate());

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
     * Finds available appointments based on the given request.
     *
     * @return - List of available appointments matching the request
     */
    public List<Appointment> findFreeAppointments() {
        return null;
    }

    /**
     * Finds occupied appointments based on the given date.
     *
     * @param date - The date for the search.
     * @return - A list of occupied appointments matching the query.
     */
    public List<Appointment> findTakenAppointmentsByDate(LocalDate date) {
        return reservedAppointments.stream()
                .filter(appointment -> appointment.getTime().getStartDate().isEqual(date))
                .collect(Collectors.toList());
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
    public List<Appointment> findTakenAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, int startTime, int endTime) {
        return reservedAppointments.stream()
                .filter(appointment -> appointment.getTime().getDay().equals(day)
                        && appointment.getTime().getStartDate().equals(startDate)
                        && appointment.getTime().getEndDate().equals(endDate)
                        && appointment.getTime().getStartTime() >= startTime
                        && appointment.getTime().getEndTime() <= endTime)
                .collect(Collectors.toList());
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
    public List<Appointment> findTakenAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, int startTime, int endTime) {
        return reservedAppointments.stream()
                .filter(appointment -> appointment.getTime().getStartDate().equals(startDate)
                        && appointment.getTime().getEndDate().equals(endDate)
                        && appointment.getTime().getStartTime() >= startTime
                        && appointment.getTime().getEndTime() <= endTime)
                .collect(Collectors.toList());
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
    public List<Appointment> findTakenAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, int startTime, int duration) {
        return reservedAppointments.stream()
                .filter(appointment -> appointment.getTime().getStartDate().equals(startDate)
                        && appointment.getTime().getEndDate().equals(endDate)
                        && appointment.getTime().getStartTime() >= startTime
                        && appointment.getTime().getEndTime() <= startTime + duration)
                .collect(Collectors.toList());
    }

    /**
     * Finds occupied appointments based on the specified room.
     *
     * @param room - The room to search for.
     * @return - A list of occupied appointments held in the specified room.
     * @throws RoomNotFoundException if the room does not exist.
     */
    public List<Appointment> findTakenAppointmentsByRoom(ScheduleRoom room) {
        if (!rooms.contains(room)) throw new RoomNotFoundException("Room does not exist");
        return reservedAppointments.stream()
                .filter(appointment -> appointment.getScheduleRoom().equals(room))
                .collect(Collectors.toList());
    }

    /**
     * Finds occupied appointments based on the specified additional data.
     *
     * @param data - A map containing keys and values of additional data for the search.
     * @return - A list of occupied appointments that contain all the specified keys and values in the additional data.
     */
    public List<Appointment> findTakenAppointmentsByData(Map<String, Object> data) {
        return reservedAppointments.stream()
                .filter(appointment -> data.entrySet().stream().allMatch(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    return appointment.getAllData().containsKey(key) && appointment.getAllData().get(key).equals(value);
                }))
                .collect(Collectors.toList());
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
    public List<Appointment> findTakenAppointmentsByData(String ... keys) {
        if (keys == null || keys.length == 0)
            throw new IllegalArgumentException("At least one key must be provided for the search.");

        return reservedAppointments.stream()
                .filter(appointment -> Arrays.stream(keys).allMatch(key -> appointment.getAllData().containsKey(key)))
                .collect(Collectors.toList());
    }

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
