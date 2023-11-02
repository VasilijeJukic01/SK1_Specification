import com.raf.sk.specification.Schedule;
import com.raf.sk.specification.exception.AppointmentNotFoundException;
import com.raf.sk.specification.exception.AppointmentOverlapException;
import com.raf.sk.specification.exception.DifferentDataException;
import com.raf.sk.specification.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

public class ScheduleTest {

    @Test
    public void add_room_test() throws NoSuchFieldException, IllegalAccessException {
        Schedule schedule = new TestSchedule(null);
        ScheduleRoom room = new ScheduleRoom("RAF 20", 60);

        Field field = Schedule.class.getDeclaredField("rooms");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ScheduleRoom> rooms = (List<ScheduleRoom>) field.get(schedule);

        Assertions.assertTrue(rooms.isEmpty());
        schedule.addRoom(null);
        Assertions.assertTrue(rooms.isEmpty());

        schedule.addRoom(room);
        Assertions.assertEquals(1, rooms.size());
    }

    @Test
    public void add_appointment_basic_test() throws NoSuchFieldException, IllegalAccessException {
        Schedule schedule = new TestSchedule(null);


        Field field = Schedule.class.getDeclaredField("schedule");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Appointment> appointments = (List<Appointment>) field.get(schedule);

        Assertions.assertTrue(appointments.isEmpty());
        schedule.addAppointment(null);
        Assertions.assertTrue(appointments.isEmpty());

        ScheduleTime t1 = new ScheduleTime(Day.MONDAY, 8, 10, LocalDate.of(2022, 1, 1));
        ScheduleRoom r1 = new ScheduleRoom("RAF 6", 50);
        Appointment a1 = new Appointment(t1, r1);

        schedule.addAppointment(a1);
        Assertions.assertEquals(1, appointments.size());
        Assertions.assertTrue(appointments.contains(a1));
    }

    @Test
    public void add_appointment_overlap_test() {
        Schedule schedule = new TestSchedule(null);

        ScheduleTime t1 = new ScheduleTime(Day.MONDAY, 8, 10, LocalDate.of(2022, 1, 1));
        ScheduleTime t2 = new ScheduleTime(Day.THUERSDAY, 8, 10, LocalDate.of(2021, 12, 30), LocalDate.of(2022, 1, 14));
        ScheduleTime t3 = new ScheduleTime(Day.THUERSDAY, 10, 15, LocalDate.of(2021, 12, 30), LocalDate.of(2022, 1, 14));
        ScheduleTime t4 = new ScheduleTime(Day.MONDAY, 9, 12, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 4));
        ScheduleTime t5 = new ScheduleTime(Day.THUERSDAY, 9, 16, LocalDate.of(2022, 1, 14), LocalDate.of(2022, 1, 15));

        ScheduleRoom r1 = new ScheduleRoom("RAF 6", 50);

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
        Schedule schedule = new TestSchedule(null);

        ScheduleTime t1 = new ScheduleTime(Day.MONDAY, 8, 10, LocalDate.of(2022, 1, 1));
        ScheduleTime t2 = new ScheduleTime(Day.THUERSDAY, 8, 10, LocalDate.of(2021, 12, 30), LocalDate.of(2022, 1, 14));
        ScheduleTime t3 = new ScheduleTime(Day.THUERSDAY, 10, 15, LocalDate.of(2021, 12, 30), LocalDate.of(2022, 1, 14));
        ScheduleTime t4 = new ScheduleTime(Day.MONDAY, 9, 12, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 4));
        ScheduleTime t5 = new ScheduleTime(Day.THUERSDAY, 9, 16, LocalDate.of(2022, 1, 14), LocalDate.of(2022, 1, 15));

        ScheduleRoom r1 = new ScheduleRoom("RAF 6", 50);

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);
        Appointment a4 = new Appointment(t4, r1);
        Appointment a5 = new Appointment(t5, r1);

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);

        Assertions.assertThrows(AppointmentNotFoundException.class, () -> schedule.changeAppointment(a5, a2));
        // Assertions.assertThrows(AppointmentOverlapException.class, () -> schedule.changeAppointment(a1, a4));

        a1.putData("Paprika", 45);
        a4.putData("Paprika", 43);

        Assertions.assertThrows(DifferentDataException.class, () -> schedule.changeAppointment(a1, a4));
    }

    @Test
    public void free_test(){
        Properties properties = new Properties();

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream("src/main/java/com/raf/sk/specification/test.config");
            properties.load(fileInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Schedule schedule = new TestSchedule(properties);


    }

}
