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
        while (true) {
            System.out.println("Enter the check in date (dd-MM-yyyy): ");
            String userDate = scanner.nextLine();
            try {
                userDateFormattedCheckIn = LocalDate.parse(userDate, formatter);
                LocalDate currentDate = LocalDate.now();
                if (userDateFormattedCheckIn.isBefore(currentDate)) {
                    System.out.println("Check-In Date cannot be prior to current date.");
                } else {
                    System.out.println("Date has been selected");
                    break;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date or invalid format. Please use the following format dd-MM-yyyy");
            }
        }
        return userDateFormattedCheckIn;

    }

    public LocalDate checkOutDate(){
        while(true) {
            System.out.println("Enter the check out date (dd-MM-yyyy): ");
            String userDate = scanner.nextLine();
            try {
                userDateFormattedCheckOut = LocalDate.parse(userDate, formatter);
                LocalDate currentDate = LocalDate.now();
                if (userDateFormattedCheckOut.isBefore(currentDate)) {
                    System.out.println("Check-Out Date cannot be prior to current date.");
                } else {
                    System.out.println("Date has been selected");
                    break;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date or invalid format. Please use the following format dd-MM-yyyy");

            }
        }
        return userDateFormattedCheckOut;
    }
    public Vector<Room> checkBookingsDate(LocalDate date1, LocalDate date2, Vector<Room> available, Vector<Room> rooms){
        // Open the booking file and get the dates.
        Vector<String> added = new Vector<>();
        Vector<String> Bookings_Room = new Vector<>();
        try (Scanner scanner1 = new Scanner(new File("Bookings"))) {

            // First, read all bookings into a list
            Vector<String[]> allBookings = new Vector<>();
            while (scanner1.hasNextLine()) {
                String line = scanner1.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length != 3) continue; // skip malformed lines

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
            System.out.println("Could not open file");
        }

        return available;
    }

}
