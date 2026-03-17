import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.Vector;

public class DateInput {
    private LocalDate userDateFormattedCheckIn;
    private LocalDate userDateFormattedCheckOut;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final Scanner scanner;

    public DateInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public LocalDate checkInDate() {
        int lineNumber;
        Files file = new Files();
        while (true) {
            System.out.println("Enter the check in date (dd-MM-yyyy): ");
            String userDate = scanner.nextLine();
            try {
                userDateFormattedCheckIn = LocalDate.parse(userDate, formatter);
                LocalDate currentDate = LocalDate.now();
                if (userDateFormattedCheckIn.isBefore(currentDate)) {
                    System.err.println("Check-In Date cannot be prior to current date.");
                    lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                    file.writeErrors("Check-In Date cannot be prior to current date. - " + getClass() + " - Line: " + lineNumber);
                } else {
                    System.out.println("Date has been selected");
                    break;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date or invalid format. Please use dd-MM-yyyy");
                lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                file.writeErrors("Invalid date format - " + getClass() + " - Line: " + lineNumber);
            }
        }
        return userDateFormattedCheckIn;
    }

    public LocalDate checkOutDate() {
        int lineNumber;
        Files file = new Files();
        while (true) {
            System.out.println("Enter the check out date (dd-MM-yyyy): ");
            String userDate = scanner.nextLine();
            try {
                userDateFormattedCheckOut = LocalDate.parse(userDate, formatter);
                LocalDate currentDate = LocalDate.now();
                if (userDateFormattedCheckOut.isBefore(currentDate)) {
                    System.err.println("Check-Out Date cannot be prior to current date.");
                    lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                    file.writeErrors("Check-Out Date cannot be prior to current date. - " + getClass() + " - Line: " + lineNumber);
                } else {
                    System.out.println("Date has been selected");
                    break;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date or invalid format. Please use dd-MM-yyyy");
                lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                file.writeErrors("Invalid date format - " + getClass() + " - Line: " + lineNumber);
            }
        }
        return userDateFormattedCheckOut;
    }

    public Vector<Room> checkBookingsDate(LocalDate date1, LocalDate date2,
                                           Vector<Room> available, Vector<Room> rooms) {
        Vector<String> added = new Vector<>();
        Vector<String> bookingsRoom = new Vector<>();
        int lineNumber;
        Files file = new Files();

        try (Scanner fileScanner = new Scanner(new File("Bookings"))) {
            Vector<String[]> allBookings = new Vector<>();
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length < 3) continue;
                allBookings.add(data);
                bookingsRoom.add(data[0].trim());
            }

            for (Room room1 : rooms) {
                if (added.contains(room1.getRoom_no())) continue;
                boolean isAvailable = true;
                for (String[] bookingData : allBookings) {
                    String bookedRoom = bookingData[0].trim();
                    if (!room1.getRoom_no().equals(bookedRoom)) continue;
                    // Skip cancelled bookings — they don't block availability
                    if (bookingData.length >= 5 && bookingData[4].trim().equals("CANCELLED")) continue;

                    LocalDate bookedStart = LocalDate.parse(bookingData[1].trim(), formatter);
                    LocalDate bookedEnd   = LocalDate.parse(bookingData[2].trim(), formatter);
                    boolean overlaps = !(date1.isBefore(bookedStart) && date2.isBefore(bookedStart)
                            || date1.isAfter(bookedEnd));
                    if (overlaps) {
                        isAvailable = false;
                        break;
                    }
                }
                if (isAvailable) {
                    available.add(room1);
                    added.add(room1.getRoom_no());
                }
            }

            for (Room v1 : rooms) {
                if (!bookingsRoom.contains(v1.getRoom_no()) && !added.contains(v1.getRoom_no())) {
                    available.add(v1);
                    added.add(v1.getRoom_no());
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Could not open Bookings file");
            lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
            file.writeErrors("Could not open Bookings file - " + getClass() + " - Line: " + lineNumber);
        }

        return available;
    }
}
