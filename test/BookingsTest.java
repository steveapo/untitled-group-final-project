import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bookings")
class BookingsTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("R101", 2, 90.0, "Double", "AVAILABLE");
    }

    @Test
    @DisplayName("constructor sets all fields correctly")
    void constructorSetsAllFields() {
        Bookings booking = new Bookings(room, "01-05-2026", "05-05-2026", "alice", "CONFIRMED");

        assertSame(room, booking.getRoom());
        assertEquals("01-05-2026", booking.getCheckIn());
        assertEquals("05-05-2026", booking.getCheckOut());
        assertEquals("alice", booking.getUsername());
        assertEquals("CONFIRMED", booking.getStatus());
    }

    @Test
    @DisplayName("setters update fields correctly")
    void settersUpdateFields() {
        Bookings booking = new Bookings(room, "01-05-2026", "05-05-2026", "alice", "CONFIRMED");

        Room newRoom = new Room("R202", 3, 130.0, "Triple", "AVAILABLE");
        booking.setRoom(newRoom);
        booking.setCheckIn("10-06-2026");
        booking.setCheckOut("15-06-2026");
        booking.setUsername("bob");
        booking.setStatus("CANCELLED");

        assertSame(newRoom, booking.getRoom());
        assertEquals("10-06-2026", booking.getCheckIn());
        assertEquals("15-06-2026", booking.getCheckOut());
        assertEquals("bob", booking.getUsername());
        assertEquals("CANCELLED", booking.getStatus());
    }

    @Test
    @DisplayName("toString produces correct CSV format")
    void toStringProducesCSV() {
        Bookings booking = new Bookings(room, "01-05-2026", "05-05-2026", "alice", "CONFIRMED");
        assertEquals("R101,01-05-2026,05-05-2026,alice,CONFIRMED", booking.toString());
    }

    @Test
    @DisplayName("all booking statuses are representable")
    void allStatusesAreValid() {
        String[] statuses = {"CONFIRMED", "CANCELLED", "CHECKED_IN", "CHECKED_OUT"};
        for (String status : statuses) {
            Bookings booking = new Bookings(room, "01-05-2026", "05-05-2026", "user", status);
            assertEquals(status, booking.getStatus());
        }
    }

    @Test
    @DisplayName("toString reflects room number from associated Room object")
    void toStringUsesRoomNumber() {
        Room suite = new Room("R555", 5, 500.0, "Suite", "AVAILABLE");
        Bookings booking = new Bookings(suite, "01-06-2026", "10-06-2026", "vip", "CONFIRMED");

        assertTrue(booking.toString().startsWith("R555,"));
    }

    @Test
    @DisplayName("status transition from CONFIRMED to CHECKED_IN")
    void statusTransitionCheckIn() {
        Bookings booking = new Bookings(room, "01-05-2026", "05-05-2026", "alice", "CONFIRMED");
        booking.setStatus("CHECKED_IN");
        assertEquals("CHECKED_IN", booking.getStatus());
    }

    @Test
    @DisplayName("status transition from CHECKED_IN to CHECKED_OUT")
    void statusTransitionCheckOut() {
        Bookings booking = new Bookings(room, "01-05-2026", "05-05-2026", "alice", "CHECKED_IN");
        booking.setStatus("CHECKED_OUT");
        assertEquals("CHECKED_OUT", booking.getStatus());
    }
}
