import com.raf.sk.specification.model.Day;
import com.raf.sk.specification.model.Equipment;
import com.raf.sk.specification.model.ScheduleRoom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class ScheduleRoomTest {

    @Test
    public void equipment_test() {
        Equipment equipment1 = new Equipment("Projector", 1);
        Equipment equipment2 = new Equipment("Whiteboard", 1);
        List<Equipment> equipmentList = Arrays.asList(equipment1, equipment2);

        ScheduleRoom room = new ScheduleRoom("RAF 20", 40);
        room.setEquipment(equipmentList);

        Assertions.assertEquals(equipmentList, room.getEquipment());
    }

    @Test
    public void custom_data_test() {
        ScheduleRoom room = new ScheduleRoom("RAF 20", 40);
        room.putData("Integer", 56);
        room.putData("String", "String");
        room.putData("Day", Day.SUNDAY);

        Assertions.assertTrue(room.getData("Integer") instanceof Integer);
        Assertions.assertTrue(room.getData("String") instanceof String);
        Assertions.assertTrue(room.getData("Day") instanceof Day);

        Assertions.assertEquals(Day.SUNDAY, room.getData("Day"));
        Assertions.assertEquals(3, room.getAllData().size());
    }

}
