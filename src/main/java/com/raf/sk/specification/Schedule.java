package com.raf.sk.specification;

import com.opencsv.CSVReader;
import com.raf.sk.specification.exception.*;
import com.raf.sk.specification.model.*;
import com.raf.sk.specification.model.time.FreeTime;
import com.raf.sk.specification.model.time.ReservedTime;
import com.raf.sk.specification.model.time.Time;

import java.io.FileReader;
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
     * workingTime = "${startHour}-${endHour}"
     * startDate = "${year}-${month}-${day}"
     * endDate = "${year}-${month}-${day}"
     * freeDays = "${day1},${day2},..."
     * holidays = "${month1}.{day1},${month2}.${day2},..."
     * rooms = "${room1}-${capacity1},${room2}-${capacity2},..."
     * equipment = "${room1}-${equipmentName1}-${amount1},${room2}-${equipmentName2}-${amount2},..."
     * roomData = "???"
     * csvHeader = "${ON/OFF}"
     * columns = "${column1},${column2},..."
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
        String startDateData = properties.getProperty("startDate").replaceAll("\"", "");
        String endDateData = properties.getProperty("endDate").replaceAll("\"", "");
        String freeDaysData = properties.getProperty("freeDays").replaceAll("\"", "");
        String holidaysData = properties.getProperty("holidays").replaceAll("\"", "").replaceAll("\\.", "-");
        String equipmentData = properties.getProperty("equipment").replaceAll("\"", "");

        String[] workingHours = timeData.split("-");
        LocalDate startDate = LocalDate.parse(startDateData);
        LocalDate endDate = LocalDate.parse(endDateData);
        String[] scheduleRooms = roomData.split(",");
        String[] freeDays = freeDaysData.split(",");
        String[] holidays = holidaysData.split(",");
        String[] equipment = equipmentData.split(",");

        initConfiguration(scheduleRooms, freeDays, holidays, workingHours[0], workingHours[1], startDate, endDate, equipment);
    }

    private void initConfiguration(String[] scheduleRooms, String[] freeDays, String[] holidays, String startTime, String endTime, LocalDate startDate, LocalDate endDate, String[] equipment) {
        Arrays.stream(scheduleRooms).forEach(room -> initFreeRoom(room, freeDays, holidays, startTime, endTime, startDate, endDate, equipment));
    }

    private void initFreeRoom(String room, String[] freeDays, String[] holidays, String startTime, String endTime, LocalDate startDate, LocalDate endDate, String[] equipment) {
        if (room.isEmpty()) return;
        String[] roomInfo = room.split("-");
        ScheduleRoom scheduleRoom = new ScheduleRoom(roomInfo[0], Integer.parseInt(roomInfo[1]));

        Arrays.stream(equipment)
                .map(s -> s.split("-"))
                .filter(data -> data[0].equals(roomInfo[0]))
                .forEach(data -> scheduleRoom.addEquipment(new Equipment(data[1], Integer.parseInt(data[2]))));

        rooms.add(scheduleRoom);
        initFreeAppointments(scheduleRoom, freeDays, holidays, startTime, endTime, startDate, endDate);
    }

    private void initFreeAppointments(ScheduleRoom scheduleRoom, String[] freeDays, String[] holidays, String startTime, String endTime, LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            Day day = ScheduleUtils.getInstance().getDayFromDate(currentDate);

            if (Arrays.stream(freeDays).anyMatch(freeDay -> freeDay.equals(day.toString()))) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            boolean isHoliday = false;
            for (String holiday : holidays) {
                holiday = currentDate.getYear() + "-" + holiday;
                LocalDate holidayDate = LocalDate.parse(holiday);
                if (holidayDate.equals(currentDate)) {
                    currentDate = currentDate.plusDays(1);
                    isHoliday = true;
                }
            }

            if (isHoliday) continue;
            FreeTime time = new FreeTime(day, startTime, endTime, currentDate);
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
        if (this.rooms.contains(scheduleRoom)) throw new RoomAlreadyExists("Room already exists");
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
            divideFreeAppointments(appointment);
        }
        else throw new AppointmentOverlapException("Appointment cannot be added due overlapping with another appointment");
    }

    private List<Appointment> getFreeAppointmentsByTarget(Appointment target) {
        return freeAppointments.stream()
                .filter(freeAppointment -> freeAppointment.getScheduleRoom().equals(target.getScheduleRoom())
                        && freeAppointment.getTime().getDate().isAfter(target.getTime().getStartDate().minusDays(1))
                        && freeAppointment.getTime().getDate().isBefore(target.getTime().getEndDate().plusDays(1))
                        && freeAppointment.getTime().getDay().equals(target.getTime().getDay()))
                .collect(Collectors.toList());
    }

    private void divideFreeAppointments(Appointment reservedAppointment) {
        List<Appointment> updateList = getFreeAppointmentsByTarget(reservedAppointment);
        updateList.forEach(freeAppointment -> divide(freeAppointment, reservedAppointment));
    }

    private void divide(Appointment freeAppointment, Appointment reservedAppointment) {
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
        String[] a1Start = appointment1.getTime().getStartTime().split(":");
        String[] a1End = appointment1.getTime().getEndTime().split(":");
        String[] a2Start = appointment2.getTime().getStartTime().split(":");
        String[] a2End = appointment2.getTime().getEndTime().split(":");
        int a1StartHours = Integer.parseInt(a1Start[0]);
        int a1StartMinutes = a1Start.length == 2 ? Integer.parseInt(a1Start[1]) : 0;
        int a1EndHours = Integer.parseInt(a1End[0]);
        int a1EndMinutes = a1End.length == 2 ? Integer.parseInt(a1End[1]) : 0;
        int a2StartHours = a2Start.length == 2 ? Integer.parseInt(a2Start[0]) : 0;
        int a2StartMinutes = Integer.parseInt(a2Start[1]);
        int a2EndHours = Integer.parseInt(a2End[0]);
        int a2EndMinutes = a2End.length == 2 ? Integer.parseInt(a2End[1]) : 0;

        if ((!(a1EndHours <= a2StartHours || a2EndHours <= a1StartHours))) return true;
        if (a1EndHours == a2StartHours && a1EndMinutes > a2StartMinutes) return true;
        return a2EndHours == a1StartHours && a2EndMinutes > a1StartMinutes;
    }

    /**
     * Deletes the given appointment from the schedule.
     *
     * @param appointment - Appointment to be deleted from the schedule
     */
    public void deleteAppointment(Appointment appointment) {
        if (this.reservedAppointments == null || appointment == null) return;
        if (reservedAppointments.contains(appointment)) {
            this.reservedAppointments.remove(appointment);
            mergeFreeAppointments(appointment);
        }
    }

    private void mergeFreeAppointments(Appointment deleteAppointment) {
        List<Appointment> candidates = getFreeAppointmentsByTarget(deleteAppointment);
        Map<LocalDate, List<Appointment>> candidatesByDate = new HashMap<>();

        candidates.forEach(candidate -> {
            if (candidatesByDate.containsKey(candidate.getTime().getDate())) {
                candidatesByDate.get(candidate.getTime().getDate()).add(candidate);
            }
            else {
                List<Appointment> list = new ArrayList<>();
                list.add(candidate);
                candidatesByDate.put(candidate.getTime().getDate(), list);
            }
        });

        merge(candidatesByDate, deleteAppointment);
    }

    private void merge(Map<LocalDate, List<Appointment>> candidatesByDate, Appointment deleteAppointment) {
        for (LocalDate date : candidatesByDate.keySet()) {
            List<Appointment> candidates = candidatesByDate.get(date);

            Appointment changed = null;
            for (Appointment candidate : candidates) {
                if (candidate.getTime().getStartTime().equals(deleteAppointment.getTime().getEndTime())) {
                    candidate.getTime().setStartTime(deleteAppointment.getTime().getStartTime());
                    changed = candidate;
                    break;
                }
                else if (candidate.getTime().getEndTime().equals(deleteAppointment.getTime().getStartTime())) {
                    candidate.getTime().setEndTime(deleteAppointment.getTime().getEndTime());
                    changed = candidate;
                    break;
                }
            }

            if (changed == null) {
                FreeTime time = new FreeTime(deleteAppointment.getTime().getDay(), deleteAppointment.getTime().getStartTime(), deleteAppointment.getTime().getEndTime(), date);
                Appointment appointment = new Appointment(time, deleteAppointment.getScheduleRoom());
                freeAppointments.add(appointment);
                return;
            }
            for (Appointment candidate : candidates) {
                if (candidate.equals(changed)) continue;
                if (candidate.getTime().getEndTime().equals(changed.getTime().getStartTime())) {
                    candidate.getTime().setEndTime(changed.getTime().getEndTime());
                    freeAppointments.remove(changed);
                    break;
                }
                else if (candidate.getTime().getStartTime().equals(changed.getTime().getEndTime())) {
                    candidate.getTime().setStartTime(changed.getTime().getStartTime());
                    freeAppointments.remove(changed);
                    break;
                }
            }
        }
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
    public List<Appointment> findFreeAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, String startTime, String endTime) {
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
    public List<Appointment> findFreeAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, String startTime, String endTime) {
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
    public List<Appointment> findFreeAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, String startTime, String duration) {
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
    public List<Appointment> findReservedAppointmentsByDayAndPeriod(Day day, LocalDate startDate, LocalDate endDate, String startTime, String endTime) {
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
    public List<Appointment> findReservedAppointmentsByDateTime(LocalDate startDate, LocalDate endDate, String startTime, String endTime) {
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
    public List<Appointment> findReservedAppointmentsByDateTimeDuration(LocalDate startDate, LocalDate endDate, String startTime, String duration) {
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
    public void loadScheduleFromFile(String path, Properties properties) throws IOException {
        if (path.endsWith(".csv")) loadFromCSV(path, properties);
    }

    private void loadFromCSV(String path, Properties properties) throws IOException {
        String startDateData = properties.getProperty("startDate").replaceAll("\"", "");
        String endDateData = properties.getProperty("endDate").replaceAll("\"", "");
        String freeDaysData = properties.getProperty("freeDays").replaceAll("\"", "");
        String holidaysData = properties.getProperty("holidays").replaceAll("\"", "").replaceAll("\\.", "-");

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] row, header = reader.readNext();
            while ((row = reader.readNext()) != null) {
                Map<String, Object> data = getDataFromColumn(row, header);
                List<String> columnsList = Arrays.asList(header);
                if (columnsList.contains("START_DATE") && columnsList.contains("END_DATE")) {
                    int indexOfStartDate = columnsList.indexOf("START_DATE");
                    int indexOfEndDate = columnsList.indexOf("END_DATE");
                    classicDistribution(row, header, data, indexOfStartDate, indexOfEndDate);
                }
                else {
                    dayDistribution(row, header, data, LocalDate.parse(startDateData), LocalDate.parse(endDateData));
                }

            }
        }
    }

    private Map<String, Object> getDataFromColumn(String[] row, String[] columns) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (int i = 0; i < columns.length-3; i++) {
            data.put(columns[i], row[i]);
        }
        return data;
    }

    private void classicDistribution(String[] row, String[] columns, Map<String, Object> data, int indexOfStartDate, int indexOfEndDate) {
        String[] time = row[columns.length-2].split("-");

        Optional<ScheduleRoom> roomOptional = this.rooms.stream()
                .filter(room -> room.getName().equals(row[columns.length-1]))
                .findFirst();

        ScheduleRoom scheduleRoom = roomOptional.orElse(null);
        if (scheduleRoom == null) {
            scheduleRoom = new ScheduleRoom(row[columns.length-1], 0);
            this.rooms.add(scheduleRoom);
        }

        Appointment appointment = new Appointment(
                new ReservedTime(
                        Day.valueOf(row[columns.length-3]),
                        time[0],
                        time[1],
                        LocalDate.parse(row[indexOfStartDate]),
                        LocalDate.parse(row[indexOfEndDate])
                ),
                scheduleRoom,
                data
        );
        addAppointment(appointment);
    }

    private void dayDistribution(String[] row, String[] header, Map<String, Object> data, LocalDate startDate, LocalDate endDate) {
        String[] time = row[header.length-2].split("-");

        Optional<ScheduleRoom> roomOptional = this.rooms.stream()
                .filter(room -> room.getName().equals(row[header.length-1]))
                .findFirst();

        ScheduleRoom scheduleRoom = roomOptional.orElse(null);
        if (scheduleRoom == null) {
            scheduleRoom = new ScheduleRoom(row[header.length-1], 0);
            this.rooms.add(scheduleRoom);
        }

        ReservedTime reservedTime = new ReservedTime(Day.valueOf(row[header.length-3]), time[0], time[1], startDate, endDate);
        Appointment appointment = new Appointment(
                reservedTime,
                scheduleRoom,
                data
        );
        addAppointment(appointment);
    }

    private boolean isDaySkip(Day day, String[] row, String[] columns, LocalDate currentDate, String[] freeDays, String[] holidays) {
        if (!day.equals(Day.valueOf(row[columns.length-3]))) return true;
        if (Arrays.stream(freeDays).anyMatch(freeDay -> freeDay.equals(day.toString()))) return true;

        return Arrays.stream(holidays)
                .map(holiday -> currentDate.getYear() + "-" + holiday)
                .map(LocalDate::parse)
                .anyMatch(holidayDate -> holidayDate.isEqual(currentDate));
    }

    /**
     * Saves the schedule to a file in the specified format.
     *
     * @param path - Path to the file to which the schedule is saved
     * @param format - File format (e.g., JSON, CSV)
     * @param configuration - Configuration file
     */
    public void saveScheduleToFile(String path, String format, Properties configuration) throws IOException {
        if (format.equals("CSV")) ScheduleUtils.getInstance().saveToCSV(reservedAppointments, path, configuration);
        else if (format.equals("JSON")) ScheduleUtils.getInstance().saveToJSON(reservedAppointments, path, configuration);
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
