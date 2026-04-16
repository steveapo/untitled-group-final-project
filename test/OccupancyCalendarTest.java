import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OccupancyCalendar.cellFor")
class OccupancyCalendarTest {

    // ── Helpers ──────────────────────────────────────────────────────────

    /** dd-MM-yyyy string from a LocalDate, matching the project's date format. */
    private static String fmt(LocalDate d) {
        return String.format("%02d-%02d-%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    private static Room availableRoom(String number) {
        return new Room(number, 2, 100.0, "Double", "AVAILABLE");
    }

    private static Room maintenanceRoom(String number) {
        return new Room(number, 2, 100.0, "Double", "MAINTENANCE");
    }

    private static Bookings booking(Room room, LocalDate checkIn, LocalDate checkOut, String status) {
        return new Bookings(room, fmt(checkIn), fmt(checkOut), "guest", status);
    }

    // ── Tests ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("No bookings → AVAILABLE")
    void noBookings() {
        Room room = availableRoom("R101");
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellFor(room, LocalDate.now(), new Vector<>()));
    }

    @Test
    @DisplayName("CONFIRMED booking spans the day → CONFIRMED")
    void confirmedBookingSpansDay() {
        Room room = availableRoom("R101");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, today.minusDays(1), today.plusDays(2), "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.CONFIRMED,
                OccupancyCalendar.cellFor(room, today, bookings));
    }

    @Test
    @DisplayName("CHECKED_IN booking spans the day → CHECKED_IN")
    void checkedInBookingSpansDay() {
        Room room = availableRoom("R101");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, today.minusDays(1), today.plusDays(1), "CHECKED_IN"));
        assertEquals(OccupancyCalendar.CellStatus.CHECKED_IN,
                OccupancyCalendar.cellFor(room, today, bookings));
    }

    @Test
    @DisplayName("CHECKED_OUT booking spans the day → CHECKED_OUT")
    void checkedOutBookingSpansDay() {
        Room room = availableRoom("R101");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, today.minusDays(2), today.plusDays(1), "CHECKED_OUT"));
        assertEquals(OccupancyCalendar.CellStatus.CHECKED_OUT,
                OccupancyCalendar.cellFor(room, today, bookings));
    }

    @Test
    @DisplayName("CANCELLED booking spans the day → AVAILABLE")
    void cancelledBookingIgnored() {
        Room room = availableRoom("R101");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, today.minusDays(1), today.plusDays(2), "CANCELLED"));
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellFor(room, today, bookings));
    }

    @Test
    @DisplayName("Room in MAINTENANCE with a booking on same day → MAINTENANCE")
    void maintenanceOverridesBooking() {
        Room room = maintenanceRoom("R102");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, today.minusDays(1), today.plusDays(2), "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.MAINTENANCE,
                OccupancyCalendar.cellFor(room, today, bookings));
    }

    @Test
    @DisplayName("Date equal to checkout day → AVAILABLE (exclusive end)")
    void checkoutDayIsExclusive() {
        Room room = availableRoom("R101");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        // checkOut IS today — should not match
        bookings.add(booking(room, today.minusDays(3), today, "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellFor(room, today, bookings));
    }

    @Test
    @DisplayName("Date equal to check-in day → matches booking status")
    void checkInDayIsInclusive() {
        Room room = availableRoom("R101");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        // checkIn IS today
        bookings.add(booking(room, today, today.plusDays(2), "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.CONFIRMED,
                OccupancyCalendar.cellFor(room, today, bookings));
    }

    @Test
    @DisplayName("Booking for different room does not affect query room")
    void differentRoomIgnored() {
        Room roomA = availableRoom("R101");
        Room roomB = availableRoom("R102");
        LocalDate today = LocalDate.now();
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(roomB, today.minusDays(1), today.plusDays(2), "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellFor(roomA, today, bookings));
    }
}
