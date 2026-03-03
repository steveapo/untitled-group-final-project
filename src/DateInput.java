import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.time.format.ResolverStyle;
import java.util.Vector;

public class DateInput {
    private LocalDate userDateFormattedCheckIn;
    private LocalDate userDateFormattedCheckOut;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT);

    Scanner scanner = new Scanner(System.in);

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
                    file.writeErrors("Check-In Date cannot be prior to current date." + " - " + getClass() + " - Line: " + lineNumber);
                } else {
                    System.out.println("Date has been selected");
                    break;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date or invalid format. Please use the following format dd-MM-yyyy");
                lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                file.writeErrors("Invalid date or invalid format. Please use the following format dd-MM-yyyy" + " - " + getClass() + " - Line: " + lineNumber);
            }

        }

        return userDateFormattedCheckIn;

    }

    public LocalDate checkOutDate(){

        int lineNumber;
        Files file = new Files();

        while(true) {

            System.out.println("Enter the check out date (dd-MM-yyyy): ");
            String userDate = scanner.nextLine();

            try {
                userDateFormattedCheckOut = LocalDate.parse(userDate, formatter);
                LocalDate currentDate = LocalDate.now();
                if (userDateFormattedCheckOut.isBefore(currentDate)) {
                    System.err.println("Check-Out Date cannot be prior to current date.");
                    lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                    file.writeErrors("Check-Out Date cannot be prior to current date." +  " - " + getClass() + " - Line: " + lineNumber);
                } else {
                    System.out.println("Date has been selected");
                    break;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date or invalid format. Please use the following format dd-MM-yyyy");
                lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
                file.writeErrors("Invalid date or invalid format. Please use the following format dd-MM-yyyy" + " - " + getClass() + " - Line: " + lineNumber);

            }

        }

        return userDateFormattedCheckOut;
    }

    public Vector<Room> checkBookingsDate(LocalDate date1, LocalDate date2, Vector<Room> available, Vector<Room> rooms){
        // Open the booking file and get the dates.
        Vector<String> added = new Vector<>();
        Vector<String> Bookings_Room = new Vector<>();
        int lineNumber;
        Files file = new Files();

        try (Scanner scanner1 = new Scanner(new File("Bookings"))) {

            // First, read all bookings into a list
            Vector<String[]> allBookings = new Vector<>();

            while (scanner1.hasNextLine()) {
                String line = scanner1.nextLine().trim();

                if (line.isEmpty()) continue; //Check if line is empty

                String[] data = line.split(",");
                if (data.length != 3) continue; // skip lines that don't contain the required number of attributes

                allBookings.add(data);
                Bookings_Room.add(data[0].trim());
            }

            // Now check each room
            for (Room room1 : rooms) {
                if (added.contains(room1.getRoom_no())) continue; // skip if already added

                boolean isAvailable = true;

                // Check all bookings for this room
                for (String[] bookingData : allBookings) {
                    String bookedRoom = bookingData[0].trim();
                    if (!room1.getRoom_no().equals(bookedRoom)) continue;

                    LocalDate bookedStart = LocalDate.parse(bookingData[1].trim(), formatter);
                    LocalDate bookedEnd = LocalDate.parse(bookingData[2].trim(), formatter);

                    // Your original overlap logic
                    boolean overlaps = !(date1.isBefore(bookedStart) && date2.isBefore(bookedStart)
                            || date1.isAfter(bookedEnd));
                    if (overlaps) {
                        isAvailable = false;
                        break; // room overlaps, no need to check other bookings
                    }
                }

                if (isAvailable) {
                    available.add(room1);
                    added.add(room1.getRoom_no());
                }
            }

            // Add rooms that are not in the bookings file at all
            for (Room v1 : rooms) {
                if (!Bookings_Room.contains(v1.getRoom_no()) && !added.contains(v1.getRoom_no())) {
                    available.add(v1);
                    added.add(v1.getRoom_no());
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Could not open file");
            lineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();
            file.writeErrors("Could not open file" + " - " + getClass() + " - Line: " + lineNumber);

        }

        return available;
    }

}
