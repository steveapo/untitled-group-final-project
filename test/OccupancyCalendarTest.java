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

    // ── clearMaintenanceRange ────────────────────────────────────────────

    @Test
    @DisplayName("Clear single day from middle of a 7-day maintenance block splits it into two")
    void clearOneDayFromMiddleSplitsBlock() {
        Room room = availableRoom("R101");
        LocalDate mon = LocalDate.of(2026, 5, 4);   // Monday
        LocalDate nextMon = mon.plusDays(7);        // exclusive end
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, mon, nextMon, "MAINTENANCE"));

        // User clears only Wednesday (day index 2)
        LocalDate wed = mon.plusDays(2);
        int days = OccupancyCalendar.clearMaintenanceRange(bookings, room, wed, wed.plusDays(1));

        assertEquals(1, days, "should report one day cleared");
        assertEquals(2, bookings.size(), "original block must be split into two");
        // Monday through Tuesday (exclusive Wed)
        assertEquals(fmt(mon),                bookings.get(0).getCheckIn());
        assertEquals(fmt(wed),                bookings.get(0).getCheckOut());
        // Thursday through Sunday (exclusive next Mon)
        assertEquals(fmt(wed.plusDays(1)),    bookings.get(1).getCheckIn());
        assertEquals(fmt(nextMon),            bookings.get(1).getCheckOut());
    }

    @Test
    @DisplayName("Clear sweeping past the end trims the right edge only")
    void clearPastEndTrimsRight() {
        Room room = availableRoom("R101");
        LocalDate mon = LocalDate.of(2026, 5, 4);
        LocalDate nextMon = mon.plusDays(7);
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, mon, nextMon, "MAINTENANCE"));

        // Clear Sat through next Tue (extends beyond the block)
        LocalDate sat = mon.plusDays(5);
        LocalDate nextTue = mon.plusDays(9); // exclusive
        int days = OccupancyCalendar.clearMaintenanceRange(bookings, room, sat, nextTue);

        assertEquals(2, days, "Sat + Sun are cleared (Mon/Tue of next week weren't maintenance)");
        assertEquals(1, bookings.size());
        assertEquals(fmt(mon), bookings.get(0).getCheckIn());
        assertEquals(fmt(sat), bookings.get(0).getCheckOut());
    }

    @Test
    @DisplayName("Clear range fully containing a block removes it entirely")
    void clearFullyContainingRangeRemovesBlock() {
        Room room = availableRoom("R101");
        LocalDate mon = LocalDate.of(2026, 5, 4);
        LocalDate wed = mon.plusDays(2);
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, mon, wed, "MAINTENANCE"));

        // Sweep covers the entire block and then some
        int days = OccupancyCalendar.clearMaintenanceRange(bookings, room,
                mon.minusDays(1), wed.plusDays(2));

        assertEquals(2, days);
        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Clear range never touches non-MAINTENANCE bookings sharing the sweep")
    void clearPreservesRealBookings() {
        Room room = availableRoom("R101");
        LocalDate mon = LocalDate.of(2026, 5, 4);
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, mon,            mon.plusDays(2), "MAINTENANCE")); // Mon-Tue
        bookings.add(booking(room, mon.plusDays(2), mon.plusDays(4), "CONFIRMED"));  // Wed-Thu real stay
        bookings.add(booking(room, mon.plusDays(4), mon.plusDays(6), "MAINTENANCE")); // Fri-Sat

        // Sweep Mon through Sun inclusive
        int days = OccupancyCalendar.clearMaintenanceRange(bookings, room, mon, mon.plusDays(7));

        assertEquals(4, days, "both maintenance segments wiped (2 + 2 days)");
        // Confirmed booking survives, untouched
        assertEquals(1, bookings.size());
        assertEquals("CONFIRMED", bookings.get(0).getStatus());
        assertEquals(fmt(mon.plusDays(2)), bookings.get(0).getCheckIn());
        assertEquals(fmt(mon.plusDays(4)), bookings.get(0).getCheckOut());
    }

    @Test
    @DisplayName("Clear range with no overlapping maintenance returns zero")
    void clearNoOverlapIsNoOp() {
        Room room = availableRoom("R101");
        LocalDate mon = LocalDate.of(2026, 5, 4);
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(booking(room, mon, mon.plusDays(2), "MAINTENANCE"));

        int days = OccupancyCalendar.clearMaintenanceRange(bookings, room,
                mon.plusDays(5), mon.plusDays(7));

        assertEquals(0, days);
        assertEquals(1, bookings.size()); // untouched
    }
}
