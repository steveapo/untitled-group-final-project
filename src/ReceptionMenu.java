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
            CLI.printMenuItem(" 1", "View all rooms");
            CLI.printMenuItem(" 2", "Search available rooms by dates");
            CLI.printMenuItem(" 3", "Create booking for a guest");
            CLI.printMenuItem(" 4", "View all bookings");
            CLI.printMenuItem(" 5", "Cancel a booking");
            CLI.printMenuItem(" 6", "Check in guest");
            CLI.printMenuItem(" 7", "Check out guest");
            CLI.printMenuItem(" 8", "View all guests");
            CLI.printMenuItem(" 9", "Mark room maintenance / available");
            CLI.printMenuItem("10", "Logout");
            CLI.printDivider();
            System.out.print(CLI.prompt("Choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":  viewAllRooms(rooms); Main.pause(scanner);                               break;
                case "2":  searchAvailableRooms(scanner, rooms); Main.pause(scanner);              break;
                case "3":  createBooking(scanner, rooms, bookings, users, file);                   break;
                case "4":  viewAllBookings(bookings); Main.pause(scanner);                         break;
                case "5":  cancelBooking(scanner, bookings, file);                                 break;
                case "6":  checkIn(scanner, bookings, file);                                       break;
                case "7":  checkOut(scanner, bookings, file);                                      break;
                case "8":  viewAllGuests(users); Main.pause(scanner);                              break;
                case "9":  setRoomStatus(scanner, rooms, file);                                    break;
                case "10": return;
                default:
                    System.out.println(CLI.warning("Invalid option. Enter 1–10."));
                    Main.pause(scanner);
            }
        }
    }

    private static void viewAllRooms(Vector<Room> rooms) {
        CLI.clearScreen();
        CLI.printBanner("ALL ROOMS");
        System.out.println();
        for (Room r : rooms) {
            String dot = r.getStatus().equals("AVAILABLE") ? CLI.green("●") : CLI.red("●");
            System.out.printf("  %s  %-6s | %-8s | Capacity: %d | %s/night | %s%n",
                    dot,
                    CLI.bold(r.getRoom_no()),
                    r.getType(),
                    r.getCapacity(),
                    CLI.yellow(String.format("$%.2f", r.getPrice())),
                    UserMenu.statusColour(r.getStatus()));
        }
        CLI.printDivider();
    }

    private static void searchAvailableRooms(Scanner scanner, Vector<Room> rooms) throws Exception {
        CLI.clearScreen();
        CLI.printBanner("SEARCH ROOMS BY DATE");
        System.out.println();
        DateInput date = new DateInput(scanner);
        LocalDate checkIn  = date.checkInDate();
        LocalDate checkOut = date.checkOutDate();
        while (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            System.out.println(CLI.warning("Check-out must be after check-in. Re-enter."));
            checkOut = date.checkOutDate();
        }
        Vector<Room> available = new Vector<>();
        date.checkBookingsDate(checkIn, checkOut, available, rooms);
        System.out.println("\n" + CLI.header("  Available Rooms") + "\n");
        boolean found = false;
        for (Room r : available) {
            if (r.getStatus().equals("AVAILABLE")) {
                System.out.printf("  %s  %-6s | %-8s | Capacity: %d | %s/night%n",
                        CLI.green("●"),
                        CLI.bold(r.getRoom_no()),
                        r.getType(),
                        r.getCapacity(),
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
        System.out.print(CLI.prompt("Guest username (or 'e' to cancel): "));
        String guestUsername = scanner.nextLine().trim();
        if (guestUsername.equalsIgnoreCase("e")) return;

        boolean guestExists = false;
        for (Account u : users) {
            if (u.getUsername().equals(guestUsername) && u.getRole().equals("USER")) {
                guestExists = true;
                break;
            }
        }
        if (!guestExists) {
            System.out.println(CLI.warning("Guest not found. Register the guest first."));
            Main.pause(scanner);
            return;
        }

        DateInput date = new DateInput(scanner);
        LocalDate checkIn  = date.checkInDate();
        LocalDate checkOut = date.checkOutDate();
        while (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            System.out.println(CLI.warning("Check-out must be after check-in. Re-enter."));
            checkOut = date.checkOutDate();
        }

        Vector<Room> available = new Vector<>();
        date.checkBookingsDate(checkIn, checkOut, available, rooms);
        Vector<Room> bookable = new Vector<>();
        for (Room r : available) {
            if (r.getStatus().equals("AVAILABLE")) bookable.add(r);
        }

        if (bookable.isEmpty()) {
            System.out.println(CLI.warning("No rooms available for those dates."));
            Main.pause(scanner);
            return;
        }
        for (int i = 0; i < bookable.size(); i++) {
            Room r = bookable.get(i);
            System.out.printf("  %s. %-6s | %-8s | Capacity: %d | %s/night%n",
                    CLI.cyan(String.valueOf(i + 1)),
                    CLI.bold(r.getRoom_no()),
                    r.getType(),
                    r.getCapacity(),
                    CLI.yellow(String.format("$%.2f", r.getPrice())));
        }
        System.out.print(CLI.prompt("Choose room (1-" + bookable.size() + ", or 0 to cancel): "));
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(CLI.warning("Invalid input."));
            Main.pause(scanner);
            return;
        }
        if (choice == 0) return;
        if (choice < 1 || choice > bookable.size()) {
            System.out.println(CLI.warning("Invalid room number."));
            Main.pause(scanner);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
        Room chosen = bookable.get(choice - 1);
        final String checkInStr  = checkIn.format(fmt);
        final String checkOutStr = checkOut.format(fmt);
        final String finalGuest  = guestUsername;
        CLI.withSpinner("Saving booking", () -> {
            bookings.add(new Bookings(chosen, checkInStr, checkOutStr, finalGuest, "CONFIRMED"));
            file.updateBookings(bookings);
        });
        System.out.println(CLI.success("Booking created for guest '" + guestUsername + "'."));
        Main.pause(scanner);
    }

    private static void viewAllBookings(Vector<Bookings> bookings) {
        CLI.clearScreen();
        CLI.printBanner("ALL BOOKINGS");
        System.out.println();
        int index = 1;
        for (Bookings b : bookings) {
            System.out.printf("  %s. Room %s | %-12s | %s → %s | Guest: %s%n",
                    CLI.cyan(String.valueOf(index++)),
                    CLI.bold(b.getRoom().getRoom_no()),
                    UserMenu.statusColour(b.getStatus()),
                    b.getCheck_in(), b.getCheck_out(),
                    b.getUsername());
        }
        if (bookings.isEmpty()) System.out.println(CLI.dim("  No bookings found."));
        CLI.printDivider();
    }

    private static void cancelBooking(Scanner scanner, Vector<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CANCEL BOOKING");
        System.out.println();
        System.out.print(CLI.prompt("Guest username (or 'e' to cancel): "));
        String username = scanner.nextLine().trim();
        if (username.equalsIgnoreCase("e")) return;

        Vector<Bookings> activeBookings = new Vector<>();
        for (Bookings b : bookings) {
            if (b.getUsername().equals(username) && b.getStatus().equals("CONFIRMED")) {
                activeBookings.add(b);
            }
        }
        if (activeBookings.isEmpty()) {
            System.out.println(CLI.warning("No active bookings for that guest."));
            Main.pause(scanner);
            return;
        }
        for (int i = 0; i < activeBookings.size(); i++) {
            Bookings b = activeBookings.get(i);
            System.out.printf("  %s. Room %s | %s → %s%n",
                    CLI.cyan(String.valueOf(i + 1)),
                    CLI.bold(b.getRoom().getRoom_no()),
                    b.getCheck_in(), b.getCheck_out());
        }
        System.out.print(CLI.prompt("Choose (1-" + activeBookings.size() + ", or 0 to cancel): "));
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(CLI.warning("Invalid input."));
            Main.pause(scanner);
            return;
        }
        if (choice == 0) return;
        if (choice < 1 || choice > activeBookings.size()) {
            System.out.println(CLI.warning("Invalid selection."));
            Main.pause(scanner);
            return;
        }
        activeBookings.get(choice - 1).setStatus("CANCELLED");
        file.updateBookings(bookings);
        System.out.println(CLI.success("Booking cancelled."));
        Main.pause(scanner);
    }

    private static void checkIn(Scanner scanner, Vector<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CHECK IN");
        System.out.println();
        System.out.print(CLI.prompt("Guest username (or 'e' to cancel): "));
        String username = scanner.nextLine().trim();
        if (username.equalsIgnoreCase("e")) return;
        for (Bookings b : bookings) {
            if (b.getUsername().equals(username) && b.getStatus().equals("CONFIRMED")) {
                b.setStatus("CHECKED_IN");
                file.updateBookings(bookings);
                System.out.println(CLI.success("Guest '" + username + "' checked in to room " + b.getRoom().getRoom_no() + "."));
                Main.pause(scanner);
                return;
            }
        }
        System.out.println(CLI.warning("No CONFIRMED booking found for that guest."));
        Main.pause(scanner);
    }

    private static void checkOut(Scanner scanner, Vector<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("CHECK OUT");
        System.out.println();
        System.out.print(CLI.prompt("Guest username (or 'e' to cancel): "));
        String username = scanner.nextLine().trim();
        if (username.equalsIgnoreCase("e")) return;
        for (Bookings b : bookings) {
            if (b.getUsername().equals(username) && b.getStatus().equals("CHECKED_IN")) {
                b.setStatus("CHECKED_OUT");
                file.updateBookings(bookings);
                System.out.println(CLI.success("Guest '" + username + "' checked out from room " + b.getRoom().getRoom_no() + "."));
                Main.pause(scanner);
                return;
            }
        }
        System.out.println(CLI.warning("No CHECKED_IN booking found for that guest."));
        Main.pause(scanner);
    }

    private static void viewAllGuests(Vector<Account> users) {
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
        System.out.print(CLI.prompt("Room number to update (or 'e' to cancel): "));
        String roomNo = scanner.nextLine().trim();
        if (roomNo.equalsIgnoreCase("e")) return;
        for (Room r : rooms) {
            if (r.getRoom_no().equalsIgnoreCase(roomNo)) {
                System.out.println("Current status: " + UserMenu.statusColour(r.getStatus()));
                CLI.printMenuItem("1", "AVAILABLE");
                CLI.printMenuItem("2", "MAINTENANCE");
                System.out.print(CLI.prompt("Choice: "));
                String opt = scanner.nextLine().trim();
                if (opt.equals("1")) {
                    r.setStatus("AVAILABLE");
                    file.updateRooms(rooms);
                    System.out.println(CLI.success(roomNo + " set to AVAILABLE."));
                } else if (opt.equals("2")) {
                    r.setStatus("MAINTENANCE");
                    file.updateRooms(rooms);
                    System.out.println(CLI.yellow(roomNo + " set to MAINTENANCE."));
                } else {
                    System.out.println(CLI.warning("Invalid option."));
                }
                Main.pause(scanner);
                return;
            }
        }
        System.out.println(CLI.warning("Room not found."));
        Main.pause(scanner);
    }
}
