import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.ScheduleRoom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoomTest {

    @Test
    public void custom_data_test() {
        ScheduleRoom room = new ScheduleRoom(40);
        room.putData("Integer", 56);
        room.putData("String", "String");
        room.putData("Day", Day.SUNDAY);

        Assertions.assertTrue(room.getData("Integer") instanceof Integer);
        Assertions.assertTrue(room.getData("String") instanceof String);
        Assertions.assertTrue(room.getData("Day") instanceof Day);

        Assertions.assertEquals(Day.SUNDAY, room.getData("Day"));
    }

}
