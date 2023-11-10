import com.raf.sk.specification.Schedule;
import com.raf.sk.specification.model.*;
import com.raf.sk.specification.model.time.ReservedTime;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;

public class LoadSaveTest {

    @Test
    public void csv_export_test() throws IOException {
        Schedule schedule = new TestSchedule(getProperties());

        ReservedTime t1 = new ReservedTime("8:00", "10:00", LocalDate.of(2023, 1, 2));
        ReservedTime t2 = new ReservedTime(Day.THURSDAY, "8:00", "10:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 13));
        ReservedTime t3 = new ReservedTime(Day.THURSDAY, "10:00", "15:00", LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12));

        ScheduleRoom r1 = schedule.getRoomByName("Raf04 (u)");

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);

        schedule.saveScheduleToFile("src/test/resources/test.json", "JSON", getProperties());

    }

    @Test
    public void csv_import_test() throws IOException {
        Schedule schedule = new TestSchedule(getProperties());
        schedule.loadScheduleFromFile("src/test/resources/krasno.json", getProperties());
        System.out.println("Appointments:");
        for (Appointment a : schedule.getReservedAppointments()) {
            System.out.println(a);
        }
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
