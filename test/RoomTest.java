import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Room")
class RoomTest {

    @Test
    @DisplayName("constructor sets all fields correctly")
    void constructorSetsAllFields() {
        Room room = new Room("R101", 2, 90.0, "Double", "AVAILABLE");

        assertEquals("R101", room.getRoomNumber());
        assertEquals(2, room.getCapacity());
        assertEquals(90.0, room.getPrice());
        assertEquals("Double", room.getType());
        assertEquals("AVAILABLE", room.getStatus());
    }

    @Test
    @DisplayName("setters update fields correctly")
    void settersUpdateFields() {
        Room room = new Room("R101", 1, 70.0, "Single", "AVAILABLE");

        room.setRoomNumber("R999");
        room.setCapacity(5);
        room.setPrice(500.0);
        room.setType("Suite");
        room.setStatus("MAINTENANCE");

        assertEquals("R999", room.getRoomNumber());
        assertEquals(5, room.getCapacity());
        assertEquals(500.0, room.getPrice());
        assertEquals("Suite", room.getType());
        assertEquals("MAINTENANCE", room.getStatus());
    }

    @Test
    @DisplayName("toString produces correct CSV format")
    void toStringProducesCSV() {
        Room room = new Room("R201", 3, 130.0, "Triple", "AVAILABLE");
        assertEquals("R201,3,130.0,Triple,AVAILABLE", room.toString());
    }

    @Test
    @DisplayName("toString reflects updated values after setters")
    void toStringReflectsUpdates() {
        Room room = new Room("R101", 1, 70.0, "Single", "AVAILABLE");
        room.setPrice(75.0);
        room.setStatus("MAINTENANCE");

        assertEquals("R101,1,75.0,Single,MAINTENANCE", room.toString());
    }

    @Test
    @DisplayName("room types cover all expected values")
    void roomTypesAreValid() {
        String[] validTypes = {"Single", "Double", "Triple", "Quad", "Suite"};
        for (String type : validTypes) {
            Room room = new Room("R100", 1, 50.0, type, "AVAILABLE");
            assertEquals(type, room.getType());
        }
    }

    @Test
    @DisplayName("room statuses cover expected values")
    void roomStatusesAreValid() {
        Room available = new Room("R100", 1, 50.0, "Single", "AVAILABLE");
        Room maintenance = new Room("R101", 1, 50.0, "Single", "MAINTENANCE");

        assertEquals("AVAILABLE", available.getStatus());
        assertEquals("MAINTENANCE", maintenance.getStatus());
    }

    @Test
    @DisplayName("price handles decimal precision")
    void priceHandlesDecimals() {
        Room room = new Room("R100", 1, 99.99, "Single", "AVAILABLE");
        assertEquals(99.99, room.getPrice(), 0.001);
    }
}
