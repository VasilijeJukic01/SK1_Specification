import com.raf.sk.specification.Schedule;
import com.raf.sk.specification.model.*;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;

public class LoadSaveTest {

    @Test
    public void csv_export_test() throws IOException {
        Schedule schedule = new TestSchedule(getProperties());

        ScheduleTime t1 = new ScheduleTime(8, 10, LocalDate.of(2023, 1, 2));
        ScheduleTime t2 = new ScheduleTime(Day.THURSDAY, 8, 10, LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 13));
        ScheduleTime t3 = new ScheduleTime(Day.THURSDAY, 10, 15, LocalDate.of(2023, 1, 3), LocalDate.of(2023, 1, 12));

        ScheduleRoom r1 = new ScheduleRoom("RAF1", 15);

        Appointment a1 = new Appointment(t1, r1);
        Appointment a2 = new Appointment(t2, r1);
        Appointment a3 = new Appointment(t3, r1);

        schedule.addAppointment(a1);
        schedule.addAppointment(a2);
        schedule.addAppointment(a3);

        schedule.saveScheduleToFile("src/test/resources/test.csv", "CSV");

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
