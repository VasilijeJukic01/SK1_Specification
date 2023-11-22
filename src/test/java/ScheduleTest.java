import com.raf.sk.specification.Schedule;
import com.raf.sk.specification.exception.AppointmentNotFoundException;
import com.raf.sk.specification.exception.AppointmentOverlapException;
import com.raf.sk.specification.exception.DifferentDataException;
import com.raf.sk.specification.model.*;
import com.raf.sk.specification.model.time.ReservedTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ScheduleTest {

    @Test
    public void add_room_test() {
        Schedule schedule = new ScheduleClass(getProperties());
        ScheduleRoom room = new ScheduleRoom("RAF20", 60);

        List<ScheduleRoom> rooms = schedule.getRooms();

        Assertions.assertFalse(rooms.isEmpty());

        schedule.addRoom(room);
        Assertions.assertEquals(30, rooms.size());
    }

    @Test
    public void add_appointment_basic_test() {
        Schedule schedule = new ScheduleClass(getProperties());

        List<Appointment> freeAppointments = schedule.getFreeAppointments();
        List<Appointment> reservedAppointments = schedule.getReservedAppointments();

        int freeAppointmentsSize = freeAppointments.size();

        Assertions.assertTrue(reservedAppointments.isEmpty());
        schedule.addAppointment(null);
        Assertions.assertTrue(reservedAppointments.isEmpty());

        ReservedTime t1 = new ReservedTime("8", "10:00", LocalDate.of(2023, 1, 1));
        ScheduleRoom r1 = schedule.getRoomByName("Raf04 (u)");
        Appointment a1 = new Appointment(t1, r1);

        schedule.addAppointment(a1);
        Assertions.assertEquals(1, reservedAppointments.size());
        Assertions.assertTrue(reservedAppointments.contains(a1));

        Assertions.assertEquals(freeAppointmentsSize, freeAppointments.size());
    }

    @Test
    public void add_appointment_overlap_test() {
        Schedule schedule = new ScheduleClass(getProperties());

        ReservedTime t1 = new ReservedTime("8:00", "10:00", LocalDate.of(2023, 1, 2));
        ReservedTime t2 = new ReservedTime(Day.MONDAY, "8:00", "10:10", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 13));
        ReservedTime t3 = new ReservedTime(Day.THURSDAY, "10:00", "15:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12));

        ReservedTime t4 = new ReservedTime(Day.MONDAY, "10:00", "12:00", LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 4));
        ReservedTime t5 = new ReservedTime(Day.MONDAY, "9:00", "13:00", LocalDate.of(2023, 1, 2), LocalDate.of(2023, 1, 8));

        ScheduleRoom r1 = schedule.getRoomByName("Raf04 (u)");

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);
        Appointment a4 = new Appointment(t4, r1);
        Appointment a5 = new Appointment(t5, r1);

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);

        Assertions.assertThrows(AppointmentOverlapException.class, () -> schedule.addAppointment(a4));
        Assertions.assertThrows(AppointmentOverlapException.class, () -> schedule.addAppointment(a5));
    }

    @Test
    public void change_appointment_test() {
        Schedule schedule = new ScheduleClass(getProperties());

        ReservedTime t1 = new ReservedTime("8:00", "10:00", LocalDate.of(2023, 1, 2));
        ReservedTime t2 = new ReservedTime(Day.MONDAY, "8:00", "10:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 13));
        ReservedTime t3 = new ReservedTime(Day.THURSDAY, "10:00", "15:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12));
        ReservedTime t4 = new ReservedTime(Day.MONDAY, "9:00", "12:00", LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 4));
        ReservedTime t5 = new ReservedTime(Day.WEDNESDAY, "12:00", "13:00", LocalDate.of(2023, 12, 5), LocalDate.of(2023, 12, 8));

        ScheduleRoom r1 = schedule.getRoomByName("Raf04 (u)");

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);
        Appointment a4 = new Appointment(t4, r1);
        Appointment a5 = new Appointment(t5, r1);

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);

        Assertions.assertThrows(AppointmentNotFoundException.class, () -> schedule.changeAppointment(a5, a2));
        Assertions.assertThrows(AppointmentOverlapException.class, () -> schedule.changeAppointment(a1, a4));

        a1.putData("Paprika", 45);
        a4.putData("Paprika", 43);

        Assertions.assertThrows(DifferentDataException.class, () -> schedule.changeAppointment(a1, a4));

        List<Appointment> reservedAppointments = schedule.getReservedAppointments();
        Assertions.assertEquals(3, reservedAppointments.size());
    }

    @Test
    public void free_appointment_test(){
        Schedule schedule = new ScheduleClass(getProperties());

        ReservedTime t1 = new ReservedTime("8:35", "10:00", LocalDate.of(2023, 1, 2));
        ReservedTime t2 = new ReservedTime(Day.THURSDAY, "8:35", "10:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 13));
        ReservedTime t3 = new ReservedTime(Day.THURSDAY, "10:00", "15:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12));
        ReservedTime t4 = new ReservedTime(Day.THURSDAY, "19:00", "21:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 10));
        ReservedTime t5 = new ReservedTime(Day.THURSDAY, "12:00", "13:00", LocalDate.of(2023, 12, 5), LocalDate.of(2023, 12, 8));

        ScheduleRoom r1 = schedule.getRoomByName("Raf04 (u)");

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);
        Appointment a4 = new Appointment(t4, r1);
        Appointment a5 = new Appointment(t5, r1);

        Assertions.assertEquals(7337, schedule.getFreeAppointments().size());

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);
        schedule.addAppointment(a4);
        schedule.addAppointment(a5);

        Assertions.assertEquals(7340, schedule.getFreeAppointments().size());
    }

    @Test
    public void reserved_appointments_search_test() {
        Schedule schedule = new ScheduleClass(getProperties());

        ReservedTime t1 = new ReservedTime("8:00", "10:00", LocalDate.of(2023, 1, 2));
        ReservedTime t2 = new ReservedTime(Day.THURSDAY, "8:00", "10:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 13));
        ReservedTime t3 = new ReservedTime(Day.THURSDAY, "10:00", "15:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12));
        ReservedTime t4 = new ReservedTime(Day.WEDNESDAY, "12:00", "13:00", LocalDate.of(2023, 12, 5), LocalDate.of(2023, 12, 8));

        ScheduleRoom r1 = schedule.getRoomByName("Raf04 (u)");

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);
        Appointment a4 = new Appointment(t4, r1);

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);
        schedule.addAppointment(a4);

        List<Appointment> reservedAppointments = schedule.getReservedAppointments();

        Assertions.assertEquals(4, reservedAppointments.size());

        List<Appointment> appointmentsByDate = schedule.findReservedAppointmentsByDate(LocalDate.of(2023, 1, 3));
        Assertions.assertEquals(2, appointmentsByDate.size());

        List<Appointment> appointmentsByDayAndPeriod = schedule.findReservedAppointmentsByDayAndPeriod(Day.THURSDAY, LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12), "10:00", "15:00");
        Assertions.assertEquals(1, appointmentsByDayAndPeriod.size());

        List<Appointment> appointmentsByDateTime = schedule.findReservedAppointmentsByDateTime(LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12), "10:00", "15:00");
        Assertions.assertEquals(1, appointmentsByDateTime.size());

        List<Appointment> appointmentsByDateTimeDuration = schedule.findReservedAppointmentsByDateTimeDuration(LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12), "10:00", "5:00");
        Assertions.assertEquals(1, appointmentsByDateTimeDuration.size());

        List<Appointment> appointmentsByRoom = schedule.findReservedAppointmentsByRoom(r1);
        Assertions.assertEquals(4, appointmentsByRoom.size());

        a1.putData("Computer", "Windows");
        a1.putData("Lab", "L1");

        Map<String, Object> data = new HashMap<>(a1.getAllData());

        List<Appointment> appointmentsByData = schedule.findReservedAppointmentsByData("Computer", "Lab");
        Assertions.assertEquals(1, appointmentsByData.size());

        List<Appointment> appointmentsByAllData = schedule.findReservedAppointmentsByData(data);
        Assertions.assertEquals(1, appointmentsByAllData.size());
    }

    @Test
    public void free_appointments_search_test() {
        Schedule schedule = new ScheduleClass(getProperties());

        ReservedTime t1 = new ReservedTime("8:00", "10:00", LocalDate.of(2023, 1, 2));
        ReservedTime t2 = new ReservedTime(Day.THURSDAY, "8:00", "10:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 13));
        ReservedTime t3 = new ReservedTime(Day.THURSDAY, "10:00", "15:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12));
        ReservedTime t4 = new ReservedTime(Day.WEDNESDAY, "12:00", "13:00", LocalDate.of(2023, 12, 5), LocalDate.of(2023, 12, 8));

        ScheduleRoom r1 = schedule.getRoomByName("Raf04 (u)");

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);
        Appointment a4 = new Appointment(t4, r1);

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);
        schedule.addAppointment(a4);

        List<Appointment> appointmentsByDate = schedule.findFreeAppointmentsByDate(LocalDate.of(2023, 1, 3));
        Assertions.assertEquals(29, appointmentsByDate.size());

        List<Appointment> appointmentsByDayAndPeriod = schedule.findFreeAppointmentsByDayAndPeriod(Day.THURSDAY, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31), "10:00", "15:00");
        Assertions.assertEquals(1448, appointmentsByDayAndPeriod.size());

        List<Appointment> appointmentsByDateTime = schedule.findFreeAppointmentsByDateTime(LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12), "10:00", "15:00");
        Assertions.assertEquals(173, appointmentsByDateTime.size());

        List<Appointment> appointmentsByRoom = schedule.findFreeAppointmentsByRoom(r1);
        Assertions.assertEquals(256, appointmentsByRoom.size());
    }

    private Properties getProperties() {
        try (FileInputStream fileInputStream = new FileInputStream("src/test/resources/test.config")) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
