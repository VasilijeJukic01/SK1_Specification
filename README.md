# Schedule API Component

## Description

This component allows the creation, management, searching, and saving of schedules with appointments scheduled in two dimensions - time and space. Each appointment can contain additional data, customized to the user's needs. The space in the schedule is described by capacity and other optional data.

## Features
- Allows the initialization of an empty schedule with configuration.
- Adding rooms with specified properties.
- Adding a new appointment with checks for availability.
- Enables the deletion of an occupied appointment from the schedule.
- Switching an appointment with the same related data.
- Possibility to list free appointments according to various criteria.
- Possibility to list reserved appointments according to various criteria.
- Implementation of operations to load schedule data from files of different formats, such as JSON and CSV.
- Save the schedule to CSV or JSON.

## Usage Examples
Initialization uses configuration file which is used to create free appointments and rooms. <br>
Configuration file example format:
```
workingTime = "9:00-21:00"
startDate = "2023-01-01"
endDate = "2023-12-31"
freeDays = "SATURDAY,SUNDAY"
holidays = "01.01,01.07,04.20,05.01,05.25,08.15,11.01,11.11,12.25,12.26"
rooms = "Room1-capacity1,Room2-capacity2..."
equipment = "Room1-PC-30,Room1-Projector-1,Room2-PC-40..."
csvHeader = ON
columns = "SUBJECT,TYPE,PROFESSOR,GROUPS"
```
Create a new schedule
``` 
Schedule schedule = new ConcreteSchedule(configuration);
 ```

Add a room to the schedule
```
ScheduleRoom room = new ScheduleRoom("Room101", 30);
schedule.addRoom(room);
```

Add an appointment to the schedule
```
Appointment appointment = new Appointment(time, room);
schedule.addAppointment(appointment);
```

Find free appointments on a specific date
```
List<Appointment> result = schedule.findFreeAppointmentsByDate(LocalDate.parse("2023-10-01"));
```

Find free appointments on a specific day and time period
```
List<Appointment> result = schedule.findFreeAppointmentsByDayAndPeriod(Day.MONDAY, LocalDate.parse("2023-10-01"), LocalDate.parse("2023-10-01"), "10:00", "12:00");
```

Find free appointments based on room
```
List<Appointment> result = schedule.findFreeAppointmentsByRoom(room);
```

Find free appointments based on additional data
```
Map<String, Object> searchData = new HashMap<>();
searchData.put("Subject", "Mathematics");
List<Appointment> result = schedule.findFreeAppointmentsByData(searchData);
```

Delete an appointment from the schedule
```
schedule.deleteAppointment(appointment);
```

Change an appointment in the schedule
```
Appointment oldAppointment = new Appointment(time1, room1);
Appointment newAppointment = new Appointment(tim2e, room2);
schedule.changeAppointment(oldAppointment, newAppointment);
```

Save the schedule to a file
```
schedule.saveScheduleToFile(path, "JSON");
schedule.saveScheduleToFile(path, "CSV");
```

Load the schedule from a file
```
schedule.loadScheduleFromFile(path);
```

Get the list of rooms
```
List<ScheduleRoom> rooms = schedule.getRooms(); 
```
