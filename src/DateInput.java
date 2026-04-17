import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

/**
 * Handles date input and room-availability filtering for bookings.
 * Accepts dates in dd-MM-yyyy format and validates them against current and existing bookings.
 */
public class DateInput {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final String LINE_SUFFIX = " - Line: ";

    private final Scanner scanner;

    /** Construct with the shared input scanner. */
    public DateInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompt the user for a check-in date; re-prompts on invalid input or past dates.
     * Returns null if the user types 'e' to cancel.
     */
    public LocalDate checkInDate() {
        Files file = new Files();
        LocalDate date = CLI.promptUntilValid(
            "Enter the check in date (dd-MM-yyyy, Esc to go back): ", scanner,
            s -> {
                try {
                    LocalDate parsed = LocalDate.parse(s, DATE_FORMATTER);
                    if (parsed.isBefore(LocalDate.now())) {
                        file.writeErrors("Check-In Date cannot be prior to current date. - " + getClass()
                                + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                        return CLI.Result.err("[ERR_DATE_PAST] Check-in date cannot be prior to current date.");
                    }
                    return CLI.Result.ok(parsed);
                } catch (DateTimeParseException _) {
                    file.writeErrors("Invalid date format - " + getClass()
                            + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                    return CLI.Result.err("[ERR_DATE_FMT] Invalid date or format. Please use dd-MM-yyyy.");
                }
            });
        if (date != null) System.out.println(CLI.success("Date selected."));
        return date;
    }

    /**
     * Prompt the user for a check-out date; re-prompts on invalid input or past dates.
     * Returns null if the user types 'e' to cancel.
     */
    public LocalDate checkOutDate() {
        Files file = new Files();
        LocalDate date = CLI.promptUntilValid(
            "Enter the check out date (dd-MM-yyyy, Esc to go back): ", scanner,
            s -> {
                try {
                    LocalDate parsed = LocalDate.parse(s, DATE_FORMATTER);
                    if (parsed.isBefore(LocalDate.now())) {
                        file.writeErrors("Check-Out Date cannot be prior to current date. - " + getClass()
                                + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                        return CLI.Result.err("[ERR_DATE_PAST] Check-out date cannot be prior to current date.");
                    }
                    return CLI.Result.ok(parsed);
                } catch (DateTimeParseException _) {
                    file.writeErrors("Invalid date format - " + getClass()
                            + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                    return CLI.Result.err("[ERR_DATE_FMT] Invalid date or format. Please use dd-MM-yyyy.");
                }
            });
        if (date != null) System.out.println(CLI.success("Date selected."));
        return date;
    }

    /**
     * Generic date prompt. Re-prompts on invalid input and on dates earlier than
     * {@code minInclusive} (pass {@code null} to skip the floor check).
     * Returns {@code null} if the user pressed Esc.
     */
    public LocalDate promptDate(String label, LocalDate minInclusive, String minErrorMsg) {
        Files file = new Files();
        return CLI.promptUntilValid(label, scanner, s -> {
            try {
                LocalDate parsed = LocalDate.parse(s, DATE_FORMATTER);
                if (minInclusive != null && parsed.isBefore(minInclusive)) {
                    file.writeErrors("Date before minimum - " + getClass()
                            + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                    return CLI.Result.err(minErrorMsg);
                }
                return CLI.Result.ok(parsed);
            } catch (DateTimeParseException _) {
                file.writeErrors("Invalid date format - " + getClass()
                        + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                return CLI.Result.err("[ERR_DATE_FMT] Invalid date or format. Please use dd-MM-yyyy.");
            }
        });
    }

    /**
     * Filter the rooms list to those with no active booking that overlaps the requested date range.
     * Uses the in-memory bookings vector rather than re-reading from disk, keeping data access
     * centralised in the Files class.
     */
    public List<Room> checkBookingsDate(LocalDate requestedStart, LocalDate requestedEnd,
                                           List<Room> available, List<Room> rooms,
                                           List<Bookings> bookings) {
        List<String> processedRoomNumbers = new ArrayList<>();

        for (Room room : rooms) {
            if (processedRoomNumbers.contains(room.getRoomNumber())) continue;

            boolean isAvailable = true;
            for (Bookings booking : bookings) {
                if (!room.getRoomNumber().equals(booking.getRoom().getRoomNumber())) continue;
                if (booking.getStatus().equals("CANCELLED")) continue;

                LocalDate bookedStart = LocalDate.parse(booking.getCheckIn(),  DATE_FORMATTER);
                LocalDate bookedEnd   = LocalDate.parse(booking.getCheckOut(), DATE_FORMATTER);

                // Overlap: the requested range is not entirely before or after the booked range.
                // Check-out day is not an occupied night, so a new check-in on the same day is allowed.
                boolean overlaps = requestedStart.isBefore(bookedEnd) && requestedEnd.isAfter(bookedStart);
                if (overlaps) {
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable) {
                available.add(room);
                processedRoomNumbers.add(room.getRoomNumber());
            }
        }

        return available;
    }
}
