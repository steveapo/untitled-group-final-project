import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.Vector;

/**
 * Handles date input and room-availability filtering for bookings.
 * Accepts dates in dd-MM-yyyy format and validates them against current and existing bookings.
 */
public class DateInput {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT);
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
        while (true) {
            System.out.print(CLI.prompt("Enter the check in date (dd-MM-yyyy, Esc to go back): "));
            String userInput = CLI.readLine(scanner);
            if (userInput == null) return null;
            try {
                LocalDate parsedCheckIn = LocalDate.parse(userInput, DATE_FORMATTER);
                if (parsedCheckIn.isBefore(LocalDate.now())) {
                    System.out.println(CLI.warning("Check-in date cannot be prior to current date."));
                    file.writeErrors("Check-In Date cannot be prior to current date. - " + getClass()
                            + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                } else {
                    System.out.println(CLI.success("Date selected."));
                    return parsedCheckIn;
                }
            } catch (DateTimeParseException _) {
                System.out.println(CLI.warning("Invalid date or format. Please use dd-MM-yyyy."));
                file.writeErrors("Invalid date format - " + getClass()
                        + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
            }
        }
    }

    /**
     * Prompt the user for a check-out date; re-prompts on invalid input or past dates.
     * Returns null if the user types 'e' to cancel.
     */
    public LocalDate checkOutDate() {
        Files file = new Files();
        while (true) {
            System.out.print(CLI.prompt("Enter the check out date (dd-MM-yyyy, Esc to go back): "));
            String userInput = CLI.readLine(scanner);
            if (userInput == null) return null;
            try {
                LocalDate parsedCheckOut = LocalDate.parse(userInput, DATE_FORMATTER);
                if (parsedCheckOut.isBefore(LocalDate.now())) {
                    System.out.println(CLI.warning("Check-out date cannot be prior to current date."));
                    file.writeErrors("Check-Out Date cannot be prior to current date. - " + getClass()
                            + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
                } else {
                    System.out.println(CLI.success("Date selected."));
                    return parsedCheckOut;
                }
            } catch (DateTimeParseException _) {
                System.out.println(CLI.warning("Invalid date or format. Please use dd-MM-yyyy."));
                file.writeErrors("Invalid date format - " + getClass()
                        + LINE_SUFFIX + Thread.currentThread().getStackTrace()[1].getLineNumber());
            }
        }
    }

    /**
     * Filter the rooms list to those with no active booking that overlaps the requested date range.
     * Uses the in-memory bookings vector rather than re-reading from disk, keeping data access
     * centralised in the Files class.
     */
    public Vector<Room> checkBookingsDate(LocalDate requestedStart, LocalDate requestedEnd,
                                           Vector<Room> available, Vector<Room> rooms,
                                           Vector<Bookings> bookings) {
        Vector<String> processedRoomNumbers = new Vector<>();

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
