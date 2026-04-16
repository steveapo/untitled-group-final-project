import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.Vector;

public class ReceptionMenu {

    public static void show(Scanner scanner, Account account,
                            Vector<Room> rooms, Vector<Bookings> bookings,
                            Vector<Account> users, Files file) throws Exception {
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("RECEPTION MENU");
            System.out.println(CLI.dim("  Logged in as: ") + CLI.bold(account.getUsername()) + "\n");
            CLI.printMenuItem("1", "View all rooms");
            CLI.printMenuItem("2", "Search available rooms by dates");
            CLI.printMenuItem("3", "Create booking for a guest");
            CLI.printMenuItem("4", "View all bookings");
            CLI.printMenuItem("5", "Cancel a booking");
            CLI.printMenuItem("6", "Check in guest");
            CLI.printMenuItem("7", "Check out guest");
            CLI.printMenuItem("8", "View all guests");
            CLI.printMenuItem("9", "Mark room maintenance / available");
            CLI.printFooter("Logout");
            String choice = CLI.readChoice(scanner);

            switch (choice) {
                case "1":  viewAllRooms(rooms); Main.pause(scanner);                              break;
                case "2":  searchAvailableRooms(scanner, rooms, bookings); Main.pause(scanner);    break;
                case "3":  createBooking(scanner, rooms, bookings, users, file);                   break;
                case "4":  viewAllBookings(bookings); Main.pause(scanner);                         break;
                case "5":  cancelBooking(scanner, bookings, file);                                 break;
                case "6":  checkIn(scanner, bookings, file);                                       break;
                case "7":  checkOut(scanner, bookings, file);                                      break;
                case "8":  viewAllGuests(users); Main.pause(scanner);                              break;
                case "9":  setRoomStatus(scanner, rooms, file);                                    break;
                case "ESC": return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option. Enter 1–9."));
                    Main.pause(scanner);
            }
        }
    }

    private static void viewAllRooms(Vector<Room> rooms) {
        CLI.randomSpinner("Loading rooms");
        CLI.clearScreen();
        CLI.printBanner("ALL ROOMS");
        System.out.println();
        for (Room r : rooms) {
            String dot = r.getStatus().equals("AVAILABLE") ? CLI.green("●") : CLI.red("●");
            System.out.printf("  %s  %-6s | %-8s | %s/night | %s%n",
                    dot,
                    CLI.bold(r.getRoomNumber()),
                    r.getType(),

                    CLI.yellow(String.format("$%.2f", r.getPrice())),
                    UserMenu.statusColour(r.getStatus()));
        }
        CLI.printDivider();
    }

    private static void searchAvailableRooms(Scanner scanner, Vector<Room> rooms, Vector<Bookings> bookings) throws Exception {
        CLI.clearScreen();
        CLI.printBanner("SEARCH ROOMS BY DATE");
        System.out.println();
        DateInput date = new DateInput(scanner);
        LocalDate checkIn  = date.checkInDate();
        if (checkIn == null) return;
        LocalDate checkOut = promptCheckOutAfter(date, checkIn, scanner);
        if (checkOut == null) return;
        Vector<Room> available = new Vector<>();
        date.checkBookingsDate(checkIn, checkOut, available, rooms, bookings);
        CLI.randomSpinner("Searching");
        System.out.println("\n" + CLI.header("  Available Rooms") + "\n");
        boolean found = false;
        for (Room r : available) {
            if (r.getStatus().equals("AVAILABLE")) {
                System.out.printf("  %s  %-6s | %-8s | %s/night%n",
                        CLI.green("●"),
                        CLI.bold(r.getRoomNumber()),
                        r.getType(),
                        CLI.yellow(String.format("$%.2f", r.getPrice())));
                found = true;
            }
        }
        if (!found) System.out.println(CLI.dim("  No rooms available for those dates."));
        CLI.printDivider();
    }

    private static void createBooking(Scanner scanner, Vector<Room> rooms,
                                       Vector<Bookings> bookings, Vector<Account> users,
                                       Files file) throws Exception {
        CLI.clearScreen();
        CLI.printBanner("CREATE BOOKING");
        System.out.println();

        String guestUsername = CLI.promptUntilValid(
            "Guest username (Esc to go back): ", scanner,
            s -> {
                for (Account u : users) {
                    if (u.getUsername().equals(s) && u.getRole().equals("USER")) {
                        return CLI.Result.ok(s);
                    }
                }
                return CLI.Result.err("[ERR_NOT_FOUND] Guest not found. Register the guest first.");
            });
        if (guestUsername == null) return;

        DateInput date = new DateInput(scanner);
        LocalDate checkIn  = date.checkInDate();
        if (checkIn == null) return;
        LocalDate checkOut = promptCheckOutAfter(date, checkIn, scanner);
        if (checkOut == null) return;

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
        for (int i = 0; i < bookable.size(); i++) {
            Room r = bookable.get(i);
            System.out.printf("  %s. %-6s | %-8s | %s/night%n",
                    CLI.cyan(String.valueOf(i + 1)),
                    CLI.bold(r.getRoomNumber()),
                    r.getType(),
                    CLI.yellow(String.format("$%.2f", r.getPrice())));
        }
        Integer choice = CLI.promptChoiceInRange(
            "Choose room (1-" + bookable.size() + ", Esc to go back): ", scanner, bookable.size());
        if (choice == null) return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
        Room chosen = bookable.get(choice - 1);
        final String checkInStr  = checkIn.format(fmt);
        final String checkOutStr = checkOut.format(fmt);
        final String finalGuest  = guestUsername;
        CLI.withSpinner("Saving booking", () -> {
            bookings.add(new Bookings(chosen, checkInStr, checkOutStr, finalGuest, "CONFIRMED"));
            file.updateBookings(bookings);
            try { Thread.sleep(CLI.randomDelayMs()); } catch (InterruptedException _) { Thread.currentThread().interrupt(); }
        });
        System.out.println(CLI.success("Booking created for guest '" + guestUsername + "'."));
        Main.pause(scanner);
    }

    /** Prompt for a check-out date that is strictly after {@code checkIn}; re-prompts on error. */
    private static LocalDate promptCheckOutAfter(DateInput date, LocalDate checkIn, Scanner scanner) {
        while (true) {
            LocalDate checkOut = date.checkOutDate();
            if (checkOut == null) return null;
            if (checkOut.isAfter(checkIn)) return checkOut;
            System.out.println(CLI.warning("[ERR_DATE_ORDER] Check-out must be after check-in."));
            CLI.waitForKey(scanner);
        }
    }


    private static void viewAllBookings(Vector<Bookings> bookings) {
        CLI.randomSpinner("Loading bookings");
        CLI.clearScreen();
        CLI.printBanner("ALL BOOKINGS");
        System.out.println();
        int index = 1;
        for (Bookings b : bookings) {
            System.out.printf("  %s. Room %s | %-12s | %s → %s | Guest: %s%n",
                    CLI.cyan(String.valueOf(index++)),
                    CLI.bold(b.getRoom().getRoomNumber()),
                    UserMenu.statusColour(b.getStatus()),
                    b.getCheckIn(), b.getCheckOut(),
                    b.getUsername());
        }
        if (bookings.isEmpty()) System.out.println(CLI.dim("  No bookings found."));
        CLI.printDivider();
    }

    private static void cancelBooking(Scanner scanner, Vector<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CANCEL BOOKING");
        System.out.println();
        System.out.print(CLI.prompt("Guest username (Esc to go back): "));
        String username = CLI.readLine(scanner);
        if (username == null) return;

        Vector<Bookings> activeBookings = new Vector<>();
        for (Bookings b : bookings) {
            if (b.getUsername().equals(username) && b.getStatus().equals("CONFIRMED")) {
                activeBookings.add(b);
            }
        }
        if (activeBookings.isEmpty()) {
            System.out.println(CLI.warning("[ERR_NO_BOOKINGS] No active bookings for that guest."));
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
            "Choose (1-" + activeBookings.size() + ", Esc to go back): ", scanner, activeBookings.size());
        if (choice == null) return;
        activeBookings.get(choice - 1).setStatus("CANCELLED");
        file.updateBookings(bookings);
        CLI.randomSpinner("Cancelling booking");
        System.out.println(CLI.success("Booking cancelled."));
        Main.pause(scanner);
    }

    private static void checkIn(Scanner scanner, Vector<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CHECK IN");
        System.out.println();

        // Collect unique guests with CONFIRMED bookings
        Vector<String> guestNames = new Vector<>();
        for (Bookings b : bookings) {
            if (b.getStatus().equals("CONFIRMED") && !guestNames.contains(b.getUsername())) {
                guestNames.add(b.getUsername());
            }
        }
        if (guestNames.isEmpty()) {
            System.out.println(CLI.warning("[ERR_NO_BOOKINGS] No guests with confirmed bookings."));
            Main.pause(scanner);
            return;
        }

        // Arrow-key selector for guest
        int guestIdx = CLI.selectFromList(guestNames.toArray(new String[0]), "Select guest to check in", scanner);
        if (guestIdx == -1) return;
        String username = guestNames.get(guestIdx);

        // Collect that guest's CONFIRMED bookings
        Vector<Bookings> confirmed = new Vector<>();
        for (Bookings b : bookings) {
            if (b.getUsername().equals(username) && b.getStatus().equals("CONFIRMED")) {
                confirmed.add(b);
            }
        }

        // If only one booking, check in directly; otherwise let receptionist pick
        Bookings toCheckIn;
        if (confirmed.size() == 1) {
            toCheckIn = confirmed.get(0);
        } else {
            String[] bookingLabels = new String[confirmed.size()];
            for (int i = 0; i < confirmed.size(); i++) {
                Bookings b = confirmed.get(i);
                bookingLabels[i] = "Room " + b.getRoom().getRoomNumber() + " | " + b.getCheckIn() + " → " + b.getCheckOut();
            }
            int bookingIdx = CLI.selectFromList(bookingLabels, "Select booking", scanner);
            if (bookingIdx == -1) return;
            toCheckIn = confirmed.get(bookingIdx);
        }

        toCheckIn.setStatus("CHECKED_IN");
        file.updateBookings(bookings);
        CLI.randomSpinner("Checking in");
        System.out.println(CLI.success("Guest '" + username + "' checked in to room " + toCheckIn.getRoom().getRoomNumber() + "."));
        Main.pause(scanner);
    }

    private static void checkOut(Scanner scanner, Vector<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CHECK OUT");
        System.out.println();

        // Collect unique guests with CHECKED_IN bookings
        Vector<String> guestNames = new Vector<>();
        for (Bookings b : bookings) {
            if (b.getStatus().equals("CHECKED_IN") && !guestNames.contains(b.getUsername())) {
                guestNames.add(b.getUsername());
            }
        }
        if (guestNames.isEmpty()) {
            System.out.println(CLI.warning("[ERR_NO_BOOKINGS] No guests currently checked in."));
            Main.pause(scanner);
            return;
        }

        // Arrow-key selector for guest
        int guestIdx = CLI.selectFromList(guestNames.toArray(new String[0]), "Select guest to check out", scanner);
        if (guestIdx == -1) return;
        String username = guestNames.get(guestIdx);

        // Collect that guest's CHECKED_IN bookings
        Vector<Bookings> checkedIn = new Vector<>();
        for (Bookings b : bookings) {
            if (b.getUsername().equals(username) && b.getStatus().equals("CHECKED_IN")) {
                checkedIn.add(b);
            }
        }

        // If only one booking, check out directly; otherwise let receptionist pick
        Bookings toCheckOut;
        if (checkedIn.size() == 1) {
            toCheckOut = checkedIn.get(0);
        } else {
            String[] bookingLabels = new String[checkedIn.size()];
            for (int i = 0; i < checkedIn.size(); i++) {
                Bookings b = checkedIn.get(i);
                bookingLabels[i] = "Room " + b.getRoom().getRoomNumber() + " | " + b.getCheckIn() + " → " + b.getCheckOut();
            }
            int bookingIdx = CLI.selectFromList(bookingLabels, "Select booking", scanner);
            if (bookingIdx == -1) return;
            toCheckOut = checkedIn.get(bookingIdx);
        }

        toCheckOut.setStatus("CHECKED_OUT");
        file.updateBookings(bookings);
        CLI.randomSpinner("Checking out");
        System.out.println(CLI.success("Guest '" + username + "' checked out from room " + toCheckOut.getRoom().getRoomNumber() + "."));
        Main.pause(scanner);
    }

    private static void viewAllGuests(Vector<Account> users) {
        CLI.randomSpinner("Loading guests");
        CLI.clearScreen();
        CLI.printBanner("REGISTERED GUESTS");
        System.out.println();
        boolean found = false;
        for (Account u : users) {
            if (u.getRole().equals("USER")) {
                System.out.printf("  %-15s | %s %s | %s%n",
                        CLI.bold(u.getUsername()),
                        u.getFirstName(), u.getLastName(),
                        CLI.dim(u.getEmail()));
                found = true;
            }
        }
        if (!found) System.out.println(CLI.dim("  No registered guests."));
        CLI.printDivider();
    }

    private static void setRoomStatus(Scanner scanner, Vector<Room> rooms, Files file) {
        CLI.clearScreen();
        CLI.printBanner("SET ROOM STATUS");
        System.out.println();
        viewAllRooms(rooms);

        Room target = CLI.promptUntilValid(
            "Room number to update (Esc to go back): ", scanner,
            s -> {
                String upper = s.toUpperCase();
                if (!upper.matches("R\\d{3}")) {
                    return CLI.Result.err("[ERR_ROOM_FMT] Room number must follow R### format (e.g. R401).");
                }
                for (Room r : rooms) {
                    if (r.getRoomNumber().equalsIgnoreCase(upper)) return CLI.Result.ok(r);
                }
                return CLI.Result.err("[ERR_NOT_FOUND] Room " + upper + " not found.");
            });
        if (target == null) return;

        System.out.println("  Current: " + UserMenu.statusColour(target.getStatus()));
        System.out.println();
        String[] statuses = {"AVAILABLE", "MAINTENANCE"};
        int preselect = target.getStatus().equals("MAINTENANCE") ? 1 : 0;
        int choice = CLI.selectFromList(statuses, "New status", scanner, preselect);
        if (choice == -1) return;
        String newStatus = statuses[choice];
        target.setStatus(newStatus);
        file.updateRooms(rooms);
        CLI.randomSpinner("Updating status");
        System.out.println(CLI.success(target.getRoomNumber() + " set to " + newStatus + "."));
        Main.pause(scanner);
    }
}
