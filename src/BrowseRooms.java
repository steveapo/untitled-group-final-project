import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Vector;

public class BrowseRooms extends Menu {


    BrowseRooms() {
        opMessage = "Select category to browse by : 1.Date available 2.Size 3.Price 4.Idk";
        options.add("1");
        options.add("2");
        options.add("3");
        options.add("4");
        input = startMenu(opMessage);
        handleInput();
    }
    private void handleInput() {
        Scanner scanner = new Scanner(System.in);
        if (input.equals("1")) {
            new DateInput(scanner);
            // Show all rooms that match input
        }
        else if (input.equals("2")) {
            System.out.println("Type 1 for single room or Type 2 for double room");
            // Accept input
            // Show all rooms that match input
        }
        else if (input.equals("3")) {
            System.out.println("Lowest Price: ");
            System.out.println("Highest Price: ");
            // Accept input
            // Show all rooms that match input
        }
        else if (input.equals("4")) {
            System.out.println("Option 4");
            // Accept input
            // Show all rooms that match input
        }
    }

    private void searchByDate(Scanner scanner, Vector<Room> rooms) throws Exception {
        DateInput date = new DateInput(scanner);
        LocalDate checkIn  = date.checkInDate();
        LocalDate checkOut = date.checkOutDate();
        while (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            System.out.println("Check-out must be after check-in. Re-enter.");
            checkOut = date.checkOutDate();
        }
        Vector<Room> available = new Vector<>();
        date.checkBookingsDate(checkIn, checkOut, available, rooms);
        boolean found = false;
        for (Room r : available) {
            if (r.getStatus().equals("AVAILABLE")) {
                System.out.printf("%s  %-6s | %-8s | Capacity: %d | %s/night%n", r.getRoom_no(),
                        r.getType(),
                        r.getCapacity(), "$%.2f", r.getPrice());
                found = true;
            }
        }
        if (!found) System.out.println(("No rooms available for those dates."));
    }
}
