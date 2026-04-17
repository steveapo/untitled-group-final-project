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
            CLI.printMenuItem("C", "Occupancy calendar");
            CLI.printFooter("Logout");
            String choice = CLI.readChoice(scanner);

            switch (choice) {
                case "1": roomManagement(scanner, rooms, bookings, file);                break;
                case "2": staffManagement(scanner, users, file);                         break;
                case "3": viewAllBookings(bookings); Main.pause(scanner);                break;
                case "4": viewStats(bookings);       Main.pause(scanner);                break;
                case "C": OccupancyCalendar.show(scanner, rooms, bookings, true, file); break;
                case "ESC": return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option. Enter 1–4 or C."));
                    Main.pause(scanner);
            }
        }
    }

    // ─── Room Management ───────────────────────────────────────────────
    private static void roomManagement(Scanner scanner, Vector<Room> rooms, Vector<Bookings> bookings, Files file) {
        while (true) {
            CLI.clearScreen();
            CLI.printBanner("ROOM MANAGEMENT");
            System.out.println();
            CLI.printMenuItem("1", "List all rooms");
            CLI.printMenuItem("2", "Add a room");
            CLI.printMenuItem("3", "Edit a room");
            CLI.printMenuItem("4", "Delete a room");
            CLI.printFooter("Back");
            String choice = CLI.readChoice(scanner);
            switch (choice) {
                case "1": listAllRooms(rooms); Main.pause(scanner);  break;
                case "2": addRoom(scanner, rooms, file);              break;
                case "3": editRoom(scanner, rooms, file);             break;
                case "4": deleteRoom(scanner, rooms, file);           break;
                case "ESC": return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option."));
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

        String roomNo = CLI.promptUntilValid(
            "Room number (e.g. R401, Esc to go back): ", scanner,
            s -> {
                String upper = s.toUpperCase();
                if (!upper.matches("R\\d{3}")) {
                    return CLI.Result.err("[ERR_ROOM_FMT] Room number must follow R### format (e.g. R401).");
                }
                for (Room r : rooms) {
                    if (r.getRoomNumber().equalsIgnoreCase(upper)) {
                        return CLI.Result.err("[ERR_ROOM_DUP] Room " + upper + " already exists.");
                    }
                }
                return CLI.Result.ok(upper);
            });
        if (roomNo == null) return;

        Integer capacity = CLI.promptUntilValid(
            "Capacity (Esc to go back): ", scanner,
            s -> {
                try {
                    int n = Integer.parseInt(s);
                    if (n >= 1) return CLI.Result.ok(n);
                } catch (NumberFormatException _) { /* fall through */ }
                return CLI.Result.err("[ERR_CAPACITY] Capacity must be a positive whole number.");
            });
        if (capacity == null) return;

        Double price = CLI.promptUntilValid(
            "Price per night (Esc to go back): ", scanner,
            s -> {
                try {
                    double p = Double.parseDouble(s);
                    if (p > 0) return CLI.Result.ok(p);
                } catch (NumberFormatException _) { /* fall through */ }
                return CLI.Result.err("[ERR_PRICE] Price must be a positive number.");
            });
        if (price == null) return;

        String type = CLI.promptUntilValid(
            "Type (Single/Double/Triple/Quad/Suite, Esc to go back): ", scanner,
            s -> {
                if (s.matches("(?i)Single|Double|Triple|Quad|Suite")) {
                    return CLI.Result.ok(s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
                }
                return CLI.Result.err("[ERR_TYPE] Type must be one of: Single, Double, Triple, Quad, Suite.");
            });
        if (type == null) return;

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
        Room target = CLI.selectRoom(rooms, "Select room to edit", scanner);
        if (target == null) return;
        String roomNo = target.getRoomNumber();

        String[] fields = {"Price  ($" + String.format("%.2f", target.getPrice()) + "/night)",
                           "Type   (" + target.getType() + ")",
                           "Status (" + target.getStatus() + ")"};
        int fieldChoice = CLI.selectFromList(fields, "What to edit", scanner);
        if (fieldChoice == -1) return;

        switch (fieldChoice) {
            case 0: {
                Double newPrice = CLI.promptUntilValid(
                    "New price (Esc to go back): ", scanner,
                    s -> {
                        try {
                            double p = Double.parseDouble(s);
                            if (p > 0) return CLI.Result.ok(p);
                        } catch (NumberFormatException _) { /* fall through */ }
                        return CLI.Result.err("[ERR_PRICE] Price must be a positive number.");
                    });
                if (newPrice == null) return;
                target.setPrice(newPrice);
                break;
            }
            case 1: {
                String[] types = {"Single", "Double", "Triple", "Quad", "Suite"};
                int currentType = 0;
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase(target.getType())) { currentType = i; break; }
                }
                int typeChoice = CLI.selectFromList(types, "New type", scanner, currentType);
                if (typeChoice == -1) return;
                target.setType(types[typeChoice]);
                break;
            }
            case 2: {
                String[] statuses = {"AVAILABLE", "MAINTENANCE"};
                int preselect = target.getStatus().equals("MAINTENANCE") ? 1 : 0;
                int statusChoice = CLI.selectFromList(statuses, "New status", scanner, preselect);
                if (statusChoice == -1) return;
                target.setStatus(statuses[statusChoice]);
                break;
            }
            default: return;
        }
        file.updateRooms(rooms);
        CLI.randomSpinner("Saving changes");
        System.out.println(CLI.success("Room " + roomNo + " updated."));
        Main.pause(scanner);
    }

    private static void deleteRoom(Scanner scanner, Vector<Room> rooms, Files file) {
        CLI.clearScreen();
        CLI.printBanner("DELETE ROOM");
        System.out.println();
        Room toRemove = CLI.selectRoom(rooms, "Select room to delete", scanner);
        if (toRemove == null) return;
        String roomNo = toRemove.getRoomNumber();
        System.out.print(CLI.prompt("Confirm delete " + roomNo + "? (yes/no, Esc to go back): "));
        String confirm = CLI.readLine(scanner);
        if (confirm == null) return;
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
            CLI.printFooter("Back");
            String choice = CLI.readChoice(scanner);
            switch (choice) {
                case "1": listAllStaff(users); Main.pause(scanner);         break;
                case "2": addReceptionist(scanner, users, file);            break;
                case "3": deactivateStaff(scanner, users, file);            break;
                case "ESC": return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option."));
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

        Account target = CLI.promptUntilValid(
            "Staff username to deactivate (Esc to go back): ", scanner,
            s -> {
                for (Account u : users) {
                    if (u.getUsername().equals(s)
                            && (u.getRole().equals("RECEPTION") || u.getRole().equals("MANAGER"))) {
                        return CLI.Result.ok(u);
                    }
                }
                return CLI.Result.err("[ERR_NOT_FOUND] Staff member not found.");
            });
        if (target == null) return;

        System.out.print(CLI.prompt("Deactivate '" + target.getUsername() + "'? (yes/no, Esc to go back): "));
        String deactivateConfirm = CLI.readLine(scanner);
        if (deactivateConfirm == null) return;
        if (deactivateConfirm.equalsIgnoreCase("yes")) {
            target.setRole("USER");
            file.updateUsersFileAll(users);
            CLI.randomSpinner("Deactivating account");
            System.out.println(CLI.success("Account deactivated (role set to USER)."));
        } else {
            System.out.println(CLI.dim("Deactivation cancelled."));
        }
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
