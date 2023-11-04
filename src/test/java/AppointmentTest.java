import com.raf.sk.specification.model.Appointment;
import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.ScheduleRoom;
import com.raf.sk.specification.model.ScheduleTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class AppointmentTest {

    @Test
    public void custom_data_test() {
        ScheduleRoom room = new ScheduleRoom("RAF 20", 40);
        ScheduleTime time = new ScheduleTime(10, 12, LocalDate.now());
        Appointment appointment = new Appointment(time, room);
        appointment.putData("Integer", 56);
        appointment.putData("String", "String");
        appointment.putData("Day", Day.SUNDAY);

        Assertions.assertTrue(appointment.getData("Integer") instanceof Integer);
        Assertions.assertTrue(appointment.getData("String") instanceof String);
        Assertions.assertTrue(appointment.getData("Day") instanceof Day);

        Assertions.assertEquals(Day.SUNDAY, appointment.getData("Day"));
        Assertions.assertEquals(3, appointment.getAllData().size());
    }

}
