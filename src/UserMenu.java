import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.format.ResolverStyle;
import java.util.Scanner;

public class UserMenu {

    public static void show(Scanner scanner, Account account,
                            List<Room> rooms, List<Bookings> bookings,
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
                                  List<Room> rooms, List<Bookings> bookings,
                                  Files file) throws Exception {
        // ── Step 1: number of guests ──────────────────────────────────────
        CLI.clearScreen();
        CLI.printBanner("BOOK A ROOM");
        System.out.println();
        Integer guests = CLI.promptChoiceInRange(
            "Number of guests (1-9, Esc to go back): ", scanner, 9);
        if (guests == null) return;

        // ── Step 2: visual date + room picker ─────────────────────────────
        OccupancyCalendar.BookingSelection sel =
            OccupancyCalendar.pickDates(scanner, rooms, bookings, guests);
        if (sel == null) return;   // user cancelled

        // ── Step 3: confirmation screen ───────────────────────────────────
        CLI.clearScreen();
        CLI.printBanner("CONFIRM BOOKING");
        System.out.println();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        final String checkInStr  = sel.checkIn.format(fmt);
        final String checkOutStr = sel.checkOut.format(fmt);
        long nights = sel.checkIn.until(sel.checkOut).getDays();
        double total = nights * sel.room.getPrice();

        System.out.println("  " + CLI.dim("Room    : ") + CLI.bold(sel.room.getRoomNumber())
                + "  " + sel.room.getType() + "  (capacity " + sel.room.getCapacity() + ")");
        System.out.println("  " + CLI.dim("Check-in: ") + CLI.cyan(checkInStr));
        System.out.println("  " + CLI.dim("Check-out: ") + CLI.cyan(checkOutStr));
        System.out.println("  " + CLI.dim("Nights  : ") + nights);
        System.out.println("  " + CLI.dim("Total   : ") + CLI.yellow(String.format("$%.2f", total)));
        System.out.println();

        boolean confirmed = false;
        while (!confirmed) {
            System.out.print(CLI.prompt("Confirm? (yes/no, Esc to cancel): "));
            String confirm = CLI.readLine(scanner);
            if (confirm == null) {
                System.out.println(CLI.dim("Booking cancelled."));
                Main.pause(scanner);
                return;
            }
            if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
                confirmed = true;
            } else if (confirm.equalsIgnoreCase("no") || confirm.equalsIgnoreCase("n")) {
                System.out.println(CLI.dim("Booking cancelled."));
                Main.pause(scanner);
                return;
            } else {
                // Invalid input — show the error, wait for a key, then scrub the cycle.
                System.out.println(CLI.warning("Please enter 'yes' or 'no'."));
                CLI.waitForKey(scanner);
                // 1 (prompt+input) + 1 (error) + 1 (press-any-key) = 3 lines to clear.
                if (CLI.supportsAnsi()) {
                    System.out.print("\033[3A\r\033[J");
                    System.out.flush();
                }
            }
        }

        final Room chosen = sel.room;
        CLI.withSpinner("Saving booking", () -> {
            bookings.add(new Bookings(chosen, checkInStr, checkOutStr,
                    account.getUsername(), "CONFIRMED"));
            file.updateBookings(bookings);
            try { Thread.sleep(CLI.randomDelayMs()); }
            catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        });
        System.out.println(CLI.success("Booking confirmed!"));
        Main.pause(scanner);
    }

    private static void viewMyBookings(Account account, List<Bookings> bookings) {
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
                                       List<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CANCEL A BOOKING");
        System.out.println();

        List<Bookings> activeBookings = new ArrayList<>();
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
