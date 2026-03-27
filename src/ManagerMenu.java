import java.util.Scanner;
import java.util.Vector;

public class ManagerMenu {

    public static void show(Scanner scanner, Account account,
                            Vector<Room> rooms, Vector<Bookings> bookings,
                            Vector<Account> users, Files file) throws Exception {
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("MANAGER MENU");
            System.out.println(CLI.dim("  Logged in as: ") + CLI.bold(account.getUsername()) + "\n");
            CLI.printMenuItem("1", "Room Management");
            CLI.printMenuItem("2", "Staff Management");
            CLI.printMenuItem("3", "View all bookings");
            CLI.printMenuItem("4", "View statistics");
            CLI.printMenuItem("5", "Logout");
            CLI.printDivider();
            System.out.print(CLI.prompt("Choice: "));
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": roomManagement(scanner, rooms, file);            break;
                case "2": staffManagement(scanner, users, file);           break;
                case "3": viewAllBookings(bookings); Main.pause(scanner);  break;
                case "4": viewStats(bookings);       Main.pause(scanner);  break;
                case "5": return;
                default:
                    System.out.println(CLI.warning("Invalid option. Enter 1–5."));
                    Main.pause(scanner);
            }
        }
    }

    // ─── Room Management ───────────────────────────────────────────────
    private static void roomManagement(Scanner scanner, Vector<Room> rooms, Files file) {
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("ROOM MANAGEMENT");
            System.out.println();
            CLI.printMenuItem("1", "List all rooms");
            CLI.printMenuItem("2", "Add a room");
            CLI.printMenuItem("3", "Edit a room");
            CLI.printMenuItem("4", "Delete a room");
            CLI.printMenuItem("5", "Back");
            CLI.printDivider();
            System.out.print(CLI.prompt("Choice: "));
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listAllRooms(rooms); Main.pause(scanner);  break;
                case "2": addRoom(scanner, rooms, file);              break;
                case "3": editRoom(scanner, rooms, file);             break;
                case "4": deleteRoom(scanner, rooms, file);           break;
                case "5": return;
                default:
                    System.out.println(CLI.warning("Invalid option."));
                    Main.pause(scanner);
            }
        }
    }

    private static void listAllRooms(Vector<Room> rooms) {
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

    private static void addRoom(Scanner scanner, Vector<Room> rooms, Files file) {
        CLI.clearScreen();
        CLI.printBanner("ADD ROOM");
        System.out.println();
        System.out.print(CLI.prompt("Room number (e.g. R401, or 'e' to cancel): "));
        String roomNo = scanner.nextLine().trim();
        if (roomNo.equalsIgnoreCase("e")) return;

        for (Room r : rooms) {
            if (r.getRoomNumber().equalsIgnoreCase(roomNo)) {
                System.out.println(CLI.warning("Room already exists."));
                Main.pause(scanner);
                return;
            }
        }

        System.out.print(CLI.prompt("Capacity (or 'e' to cancel): "));
        String capInput = scanner.nextLine().trim();
        if (capInput.equalsIgnoreCase("e")) return;
        int capacity;
        try { capacity = Integer.parseInt(capInput); }
        catch (NumberFormatException e) {
            System.out.println(CLI.warning("Invalid capacity."));
            Main.pause(scanner);
            return;
        }

        System.out.print(CLI.prompt("Price per night (or 'e' to cancel): "));
        String priceInput = scanner.nextLine().trim();
        if (priceInput.equalsIgnoreCase("e")) return;
        double price;
        try { price = Double.parseDouble(priceInput); }
        catch (NumberFormatException e) {
            System.out.println(CLI.warning("Invalid price."));
            Main.pause(scanner);
            return;
        }

        System.out.print(CLI.prompt("Type (Single/Double/Triple/Quad/Suite, or 'e' to cancel): "));
        String type = scanner.nextLine().trim();
        if (type.equalsIgnoreCase("e")) return;

        rooms.add(new Room(roomNo, capacity, price, type, "AVAILABLE"));
        file.updateRooms(rooms);
        CLI.randomSpinner("Adding room");
        System.out.println(CLI.success("Room " + roomNo + " added."));
        Main.pause(scanner);
    }

    private static void editRoom(Scanner scanner, Vector<Room> rooms, Files file) {
        CLI.clearScreen();
        CLI.printBanner("EDIT ROOM");
        System.out.println();
        listAllRooms(rooms);
        System.out.print(CLI.prompt("Room number to edit (or 'e' to cancel): "));
        String roomNo = scanner.nextLine().trim();
        if (roomNo.equalsIgnoreCase("e")) return;

        for (Room r : rooms) {
            if (r.getRoomNumber().equalsIgnoreCase(roomNo)) {
                CLI.printMenuItem("1", "Price");
                CLI.printMenuItem("2", "Type");
                CLI.printMenuItem("3", "Status");
                System.out.print(CLI.prompt("Edit (or 'e' to cancel): "));
                String opt = scanner.nextLine().trim();
                if (opt.equalsIgnoreCase("e")) return;
                switch (opt) {
                    case "1":
                        System.out.print(CLI.prompt("New price (or 'e' to cancel): "));
                        String priceStr = scanner.nextLine().trim();
                        if (priceStr.equalsIgnoreCase("e")) return;
                        try { r.setPrice(Double.parseDouble(priceStr)); }
                        catch (NumberFormatException e) {
                            System.out.println(CLI.warning("Invalid price."));
                            Main.pause(scanner);
                            return;
                        }
                        break;
                    case "2":
                        System.out.print(CLI.prompt("New type (Single/Double/Triple/Quad/Suite, or 'e' to cancel): "));
                        String newType = scanner.nextLine().trim();
                        if (newType.equalsIgnoreCase("e")) return;
                        r.setType(newType);
                        break;
                    case "3":
                        String[] statuses = {"AVAILABLE", "MAINTENANCE"};
                        int preselect = r.getStatus().equals("MAINTENANCE") ? 1 : 0;
                        int statusChoice = CLI.selectFromList(statuses, "New status", scanner, preselect);
                        if (statusChoice == -1) return;
                        r.setStatus(statuses[statusChoice]);
                        break;
                    default:
                        System.out.println(CLI.warning("Invalid option."));
                        Main.pause(scanner);
                        return;
                }
                file.updateRooms(rooms);
                CLI.randomSpinner("Saving changes");
                System.out.println(CLI.success("Room " + roomNo + " updated."));
                Main.pause(scanner);
                return;
            }
        }
        System.out.println(CLI.warning("Room not found."));
        Main.pause(scanner);
    }

    private static void deleteRoom(Scanner scanner, Vector<Room> rooms, Files file) {
        CLI.clearScreen();
        CLI.printBanner("DELETE ROOM");
        System.out.println();
        listAllRooms(rooms);
        System.out.print(CLI.prompt("Room number to delete (or 'e' to cancel): "));
        String roomNo = scanner.nextLine().trim();
        if (roomNo.equalsIgnoreCase("e")) return;

        Room toRemove = null;
        for (Room r : rooms) {
            if (r.getRoomNumber().equalsIgnoreCase(roomNo)) { toRemove = r; break; }
        }
        if (toRemove == null) {
            System.out.println(CLI.warning("Room not found."));
            Main.pause(scanner);
            return;
        }
        System.out.print(CLI.prompt("Confirm delete " + roomNo + "? (yes/no/e): "));
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("e")) return;
        if (confirm.equalsIgnoreCase("yes")) {
            rooms.remove(toRemove);
            file.updateRooms(rooms);
            CLI.randomSpinner("Deleting room");
            System.out.println(CLI.success("Room " + roomNo + " deleted."));
        } else {
            System.out.println(CLI.dim("Delete cancelled."));
        }
        Main.pause(scanner);
    }

    // ─── Staff Management ──────────────────────────────────────────────
    private static void staffManagement(Scanner scanner, Vector<Account> users, Files file)
            throws Exception {
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("STAFF MANAGEMENT");
            System.out.println();
            CLI.printMenuItem("1", "List all staff");
            CLI.printMenuItem("2", "Add receptionist");
            CLI.printMenuItem("3", "Deactivate staff account");
            CLI.printMenuItem("4", "Back");
            CLI.printDivider();
            System.out.print(CLI.prompt("Choice: "));
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listAllStaff(users); Main.pause(scanner);         break;
                case "2": addReceptionist(scanner, users, file);            break;
                case "3": deactivateStaff(scanner, users, file);            break;
                case "4": return;
                default:
                    System.out.println(CLI.warning("Invalid option."));
                    Main.pause(scanner);
            }
        }
    }

    private static void listAllStaff(Vector<Account> users) {
        CLI.randomSpinner("Loading staff");
        CLI.clearScreen();
        CLI.printBanner("STAFF ACCOUNTS");
        System.out.println();
        boolean found = false;
        for (Account u : users) {
            if (u.getRole().equals("RECEPTION") || u.getRole().equals("MANAGER")) {
                System.out.printf("  %-15s | %s %s | %s | %s%n",
                        CLI.bold(u.getUsername()),
                        u.getFirstName(), u.getLastName(),
                        CLI.dim(u.getEmail()),
                        CLI.cyan(u.getRole()));
                found = true;
            }
        }
        if (!found) System.out.println(CLI.dim("  No staff accounts found."));
        CLI.printDivider();
    }

    private static void addReceptionist(Scanner scanner, Vector<Account> users, Files file)
            throws Exception {
        CLI.clearScreen();
        CLI.printBanner("ADD RECEPTIONIST");
        System.out.println();
        Account newStaff = new Account();
        boolean registered = newStaff.register(users, scanner);
        if (!registered) return;
        // The register() method appends as USER — promote the last entry to RECEPTION
        if (!users.isEmpty()) {
            Account lastAdded = users.get(users.size() - 1);
            lastAdded.setRole("RECEPTION");
            file.updateUsersFileAll(users);
            CLI.randomSpinner("Creating account");
            System.out.println(CLI.success("Account '" + lastAdded.getUsername() + "' set to RECEPTION."));
            Main.pause(scanner);
        }
    }

    private static void deactivateStaff(Scanner scanner, Vector<Account> users, Files file) {
        CLI.clearScreen();
        CLI.printBanner("DEACTIVATE STAFF");
        System.out.println();
        listAllStaff(users);
        System.out.print(CLI.prompt("Staff username to deactivate (or 'e' to cancel): "));
        String username = scanner.nextLine().trim();
        if (username.equalsIgnoreCase("e")) return;
        for (Account u : users) {
            if (u.getUsername().equals(username)
                    && (u.getRole().equals("RECEPTION") || u.getRole().equals("MANAGER"))) {
                System.out.print(CLI.prompt("Deactivate '" + username + "'? (yes/no/e): "));
                String deactivateConfirm = scanner.nextLine().trim();
                if (deactivateConfirm.equalsIgnoreCase("e")) return;
                if (deactivateConfirm.equalsIgnoreCase("yes")) {
                    u.setRole("USER");
                    file.updateUsersFileAll(users);
                    CLI.randomSpinner("Deactivating account");
                    System.out.println(CLI.success("Account deactivated (role set to USER)."));
                } else {
                    System.out.println(CLI.dim("Deactivation cancelled."));
                }
                Main.pause(scanner);
                return;
            }
        }
        System.out.println(CLI.warning("Staff member not found."));
        Main.pause(scanner);
    }

    // ─── Bookings & Stats ──────────────────────────────────────────────
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

    private static void viewStats(Vector<Bookings> bookings) {
        CLI.randomSpinner("Loading statistics");
        CLI.clearScreen();
        CLI.printBanner("BOOKING STATISTICS");
        System.out.println();

        int activeCount    = 0;
        int cancelledCount = 0;
        int checkedOutCount = 0;
        double totalRevenue = 0.0;

        for (Bookings b : bookings) {
            switch (b.getStatus()) {
                case "CANCELLED":   cancelledCount++;  break;
                case "CHECKED_OUT":
                    checkedOutCount++;
                    try {
                        java.time.format.DateTimeFormatter fmt =
                            java.time.format.DateTimeFormatter.ofPattern("dd-MM-uuuu")
                                .withResolverStyle(java.time.format.ResolverStyle.STRICT);
                        java.time.LocalDate checkIn  = java.time.LocalDate.parse(b.getCheckIn(), fmt);
                        java.time.LocalDate checkOut = java.time.LocalDate.parse(b.getCheckOut(), fmt);
                        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                        totalRevenue += nights * b.getRoom().getPrice();
                    } catch (Exception e) {
                        // skip malformed dates
                    }
                    break;
                default:
                    activeCount++;
            }
        }

        System.out.println("  " + CLI.dim("Active bookings  : ") + CLI.green(String.valueOf(activeCount)));
        System.out.println("  " + CLI.dim("Cancelled        : ") + CLI.red(String.valueOf(cancelledCount)));
        System.out.println("  " + CLI.dim("Checked out      : ") + CLI.cyan(String.valueOf(checkedOutCount)));
        System.out.println("  " + CLI.dim("Revenue          : ") + CLI.yellow(String.format("$%.2f", totalRevenue)));
        CLI.printDivider();
    }
}
