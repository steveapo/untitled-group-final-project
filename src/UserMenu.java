import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.Vector;

public class UserMenu {

    public static void show(Scanner scanner, Account account,
                            Vector<Room> rooms, Vector<Bookings> bookings,
                            Files file) throws Exception {
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("USER MENU");
            System.out.println(CLI.dim("  Welcome, ") + CLI.bold(account.getFirstName()) + "\n");
            CLI.printMenuItem("1", "Search and book a room");
            CLI.printMenuItem("2", "View my bookings");
            CLI.printMenuItem("3", "Cancel a booking");
            CLI.printMenuItem("4", "View my profile");
            CLI.printMenuItem("C", "Occupancy calendar");
            CLI.printFooter("Logout");
            String choice = CLI.readChoice(scanner);

            switch (choice) {
                case "1": bookRoom(scanner, account, rooms, bookings, file);            break;
                case "2": viewMyBookings(account, bookings); Main.pause(scanner);       break;
                case "3": cancelBooking(scanner, account, bookings, file);              break;
                case "4": viewProfile(account); Main.pause(scanner);                    break;
                case "C": OccupancyCalendar.show(scanner, rooms, bookings, false);     break;
                case "ESC": return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option. Enter 1–4 or C."));
                    Main.pause(scanner);
            }
        }
    }

    private static void bookRoom(Scanner scanner, Account account,
                                  Vector<Room> rooms, Vector<Bookings> bookings,
                                  Files file) throws Exception {
        CLI.clearScreen();
        CLI.printBanner("BOOK A ROOM");
        System.out.println();

        DateInput date = new DateInput(scanner);
        LocalDate checkIn  = date.checkInDate();
        if (checkIn == null) return;
        LocalDate checkOut;
        while (true) {
            checkOut = date.checkOutDate();
            if (checkOut == null) return;
            if (checkOut.isAfter(checkIn)) break;
            System.out.println(CLI.warning("[ERR_DATE_ORDER] Check-out must be after check-in."));
            CLI.waitForKey(scanner);
        }

        Vector<Room> available = new Vector<>();
        date.checkBookingsDate(checkIn, checkOut, available, rooms, bookings);

        Vector<Room> bookable = new Vector<>();
        for (Room r : available) {
            if (r.getStatus().equals("AVAILABLE")) bookable.add(r);
        }

        if (bookable.isEmpty()) {
            System.out.println(CLI.warning("[ERR_NO_ROOMS] No rooms available for those dates."));
            Main.pause(scanner);
            return;
        }

        System.out.println("\n" + CLI.header("  Available Rooms") + "\n");
        for (int i = 0; i < bookable.size(); i++) {
            Room r = bookable.get(i);
            System.out.printf("  %s  %-6s | %-8s | %s/night%n",
                    CLI.cyan((i + 1) + "."),
                    CLI.bold(r.getRoomNumber()),
                    r.getType(),
                    CLI.yellow(String.format("$%.2f", r.getPrice())));
        }

        Integer choice = CLI.promptChoiceInRange(
            "\nChoose room (1-" + bookable.size() + ", Esc to go back): ", scanner, bookable.size());
        if (choice == null) return;

        Room chosen = bookable.get(choice - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
        // Capture as effectively-final strings for use inside the lambda
        final String checkInStr  = checkIn.format(fmt);
        final String checkOutStr = checkOut.format(fmt);
        System.out.println("\nConfirm booking " + CLI.bold(chosen.getRoomNumber())
                + " from " + CLI.cyan(checkInStr) + " to " + CLI.cyan(checkOutStr) + "?");
        System.out.print(CLI.prompt("(yes/no, Esc to go back): "));
        String confirm = CLI.readLine(scanner);
        if (confirm == null) { System.out.println(CLI.dim("Booking cancelled.")); Main.pause(scanner); return; }
        if (confirm.equalsIgnoreCase("yes")) {
            CLI.withSpinner("Saving booking", () -> {
                Bookings newBooking = new Bookings(chosen, checkInStr,
                        checkOutStr, account.getUsername(), "CONFIRMED");
                bookings.add(newBooking);
                file.updateBookings(bookings);
                try { Thread.sleep(CLI.randomDelayMs()); } catch (InterruptedException _) { Thread.currentThread().interrupt(); }
            });
            System.out.println(CLI.success("Booking confirmed!"));
        } else {
            System.out.println(CLI.dim("Booking cancelled."));
        }
        Main.pause(scanner);
    }

    private static void viewMyBookings(Account account, Vector<Bookings> bookings) {
        CLI.randomSpinner("Loading bookings");
        CLI.clearScreen();
        CLI.printBanner("MY BOOKINGS");
        System.out.println();
        boolean found = false;
        int index = 1;
        for (Bookings b : bookings) {
            if (b.getUsername().equals(account.getUsername())) {
                String statusColour = statusColour(b.getStatus());
                System.out.printf("  %s. Room %s | %s → %s | %s%n",
                        CLI.cyan(String.valueOf(index++)),
                        CLI.bold(b.getRoom().getRoomNumber()),
                        b.getCheckIn(), b.getCheckOut(),
                        statusColour);
                found = true;
            }
        }
        if (!found) System.out.println(CLI.dim("  You have no bookings."));
        CLI.printDivider();
    }

    private static void cancelBooking(Scanner scanner, Account account,
                                       Vector<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CANCEL A BOOKING");
        System.out.println();

        Vector<Bookings> activeBookings = new Vector<>();
        for (Bookings b : bookings) {
            if (b.getUsername().equals(account.getUsername())
                    && b.getStatus().equals("CONFIRMED")) {
                activeBookings.add(b);
            }
        }
        if (activeBookings.isEmpty()) {
            System.out.println(CLI.dim("  No active bookings to cancel."));
            Main.pause(scanner);
            return;
        }
        for (int i = 0; i < activeBookings.size(); i++) {
            Bookings b = activeBookings.get(i);
            System.out.printf("  %s. Room %s | %s → %s%n",
                    CLI.cyan(String.valueOf(i + 1)),
                    CLI.bold(b.getRoom().getRoomNumber()),
                    b.getCheckIn(), b.getCheckOut());
        }
        Integer choice = CLI.promptChoiceInRange(
            "\nChoose booking to cancel (1-" + activeBookings.size() + ", Esc to go back): ",
            scanner, activeBookings.size());
        if (choice == null) return;
        activeBookings.get(choice - 1).setStatus("CANCELLED");
        file.updateBookings(bookings);
        CLI.randomSpinner("Cancelling booking");
        System.out.println(CLI.success("Booking cancelled."));
        Main.pause(scanner);
    }

    private static void viewProfile(Account account) {
        CLI.randomSpinner("Loading profile");
        CLI.clearScreen();
        CLI.printBanner("MY PROFILE");
        System.out.println();
        System.out.println("  " + CLI.dim("Username : ") + CLI.bold(account.getUsername()));
        System.out.println("  " + CLI.dim("Name     : ") + account.getFirstName() + " " + account.getLastName());
        System.out.println("  " + CLI.dim("Email    : ") + account.getEmail());
        System.out.println("  " + CLI.dim("Role     : ") + CLI.cyan(account.getRole()));
        CLI.printDivider();
    }

    static String statusColour(String status) {
        switch (status) {
            case "CONFIRMED":   return CLI.green(status);
            case "CHECKED_IN":  return CLI.cyan(status);
            case "CHECKED_OUT": return CLI.dim(status);
            case "CANCELLED":   return CLI.red(status);
            default:            return status;
        }
    }
}
