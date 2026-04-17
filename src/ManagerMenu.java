import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

public class ManagerMenu {

    public static void show(Scanner scanner, Account account,
                            List<Room> rooms, List<Bookings> bookings,
                            List<Account> users, Files file) throws Exception {
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
                case "4": viewStats(rooms, bookings); Main.pause(scanner);               break;
                case "C": OccupancyCalendar.show(scanner, rooms, bookings, true, file); break;
                case "ESC": return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option. Enter 1–4 or C."));
                    Main.pause(scanner);
            }
        }
    }

    // ─── Room Management ───────────────────────────────────────────────
    private static void roomManagement(Scanner scanner, List<Room> rooms, List<Bookings> bookings, Files file) {
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
                case "3": editRoom(scanner, rooms, bookings, file);   break;
                case "4": deleteRoom(scanner, rooms, bookings, file); break;
                case "ESC": return;
                default:
                    System.out.println(CLI.warning("[ERR_OPTION] Invalid option."));
                    Main.pause(scanner);
            }
        }
    }

    private static void listAllRooms(List<Room> rooms) {
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

    private static void addRoom(Scanner scanner, List<Room> rooms, Files file) {
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

    private static void editRoom(Scanner scanner, List<Room> rooms,
                                  List<Bookings> bookings, Files file) {
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
                String newStatus = statuses[statusChoice];
                if ("MAINTENANCE".equals(newStatus)) {
                    // Schedule a dated maintenance window rather than flipping a permanent flag.
                    // This mirrors the calendar's M-hotkey flow — the dated booking is the single
                    // source of truth, so a window can't get "stuck" past its end date.
                    scheduleMaintenance(scanner, target, bookings, file);
                    return; // scheduleMaintenance handles its own status + pause
                }
                target.setStatus(newStatus);
                break;
            }
            default: return;
        }
        file.updateRooms(rooms);
        CLI.randomSpinner("Saving changes");
        System.out.println(CLI.success("Room " + roomNo + " updated."));
        Main.pause(scanner);
    }

    /**
     * Prompt for a maintenance start + end date and record it as a dated
     * {@code MAINTENANCE} booking for the given room.  Does not touch
     * {@code Room.status} — the booking is what blocks the room on the calendar.
     */
    private static void scheduleMaintenance(Scanner scanner, Room target,
                                             List<Bookings> bookings, Files file) {
        DateInput dates = new DateInput(scanner);
        LocalDate today = LocalDate.now();
        LocalDate from = dates.promptDate(
                "Maintenance start date (dd-MM-yyyy, Esc to go back): ",
                today,
                "[ERR_DATE_PAST] Start date cannot be in the past.");
        if (from == null) return;
        LocalDate to = dates.promptDate(
                "Maintenance end date (dd-MM-yyyy, Esc to go back): ",
                from.plusDays(1),
                "[ERR_DATE_ORDER] End date must be at least one day after the start date.");
        if (to == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        String fromStr = from.format(fmt);
        String toStr   = to.format(fmt);
        bookings.add(new Bookings(target, fromStr, toStr, "SYSTEM", "MAINTENANCE"));
        boolean ok = file.updateBookings(bookings);
        CLI.randomSpinner("Scheduling maintenance");
        if (ok) {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(from, to);
            System.out.println(CLI.success("Maintenance scheduled for "
                    + target.getRoomNumber() + ": "
                    + fromStr + " \u2192 " + toStr
                    + " (" + nights + " night" + (nights == 1 ? "" : "s") + ")."));
            System.out.println(CLI.dim(
                    "  The room stays AVAILABLE outside this window. "
                    + "Check the calendar or cancel the booking to clear it."));
        } else {
            System.out.println(CLI.warning(
                    "Maintenance added in memory but disk write failed. Check the Errors log."));
        }
        Main.pause(scanner);
    }

    private static void deleteRoom(Scanner scanner, List<Room> rooms,
                                    List<Bookings> bookings, Files file) {
        CLI.clearScreen();
        CLI.printBanner("DELETE ROOM");
        System.out.println();
        Room toRemove = CLI.selectRoom(rooms, "Select room to delete", scanner);
        if (toRemove == null) return;
        String roomNo = toRemove.getRoomNumber();

        // Summarise the blast radius: count each booking status attached to this room.
        int active = 0, upcoming = 0, past = 0, cancelled = 0;
        for (Bookings b : bookings) {
            if (!b.getRoom().getRoomNumber().equals(roomNo)) continue;
            switch (b.getStatus()) {
                case "CHECKED_IN":  active++;   break;
                case "CONFIRMED":   upcoming++; break;
                case "CHECKED_OUT": past++;     break;
                case "CANCELLED":   cancelled++; break;
                default:            upcoming++;            // MAINTENANCE etc.
            }
        }
        int total = active + upcoming + past + cancelled;

        System.out.println("  " + CLI.dim("Room    : ") + CLI.bold(roomNo)
                + "   " + toRemove.getType()
                + "   " + CLI.yellow(String.format("$%.2f", toRemove.getPrice())) + "/night"
                + "   " + UserMenu.statusColour(toRemove.getStatus()));

        if (total == 0) {
            System.out.println("  " + CLI.dim("Bookings: none"));
        } else {
            System.out.println("  " + CLI.dim("Bookings: ") + CLI.bold(total + " total")
                    + CLI.dim("  (")
                    + CLI.cyan(active + " checked-in")    + CLI.dim(", ")
                    + CLI.green(upcoming + " upcoming")   + CLI.dim(", ")
                    + CLI.dim(past + " past, " + cancelled + " cancelled)"));
            if (active > 0 || upcoming > 0) {
                System.out.println("  " + CLI.warning(
                        "This room has " + (active + upcoming)
                        + " active/upcoming booking(s). Deleting will remove every booking linked to it."));
            }
        }
        System.out.println();

        System.out.print(CLI.prompt("Type 'yes' to confirm delete " + roomNo + " (Esc to go back): "));
        String confirm = CLI.readLine(scanner);
        if (confirm == null) return;
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println(CLI.dim("Delete cancelled."));
            Main.pause(scanner);
            return;
        }

        // Cascade: remove all bookings for this room, then the room itself.
        bookings.removeIf(b -> b.getRoom().getRoomNumber().equals(roomNo));
        rooms.remove(toRemove);
        boolean roomsOk = file.updateRooms(rooms);
        boolean bookingsOk = file.updateBookings(bookings);
        CLI.randomSpinner("Deleting room");
        if (roomsOk && bookingsOk) {
            System.out.println(CLI.success("Room " + roomNo + " deleted"
                    + (total > 0 ? " (removed " + total + " linked booking(s))." : ".")));
        } else {
            System.out.println(CLI.warning(
                    "Room removed from memory but disk write failed. Check the Errors log."));
        }
        Main.pause(scanner);
    }

    // ─── Staff Management ──────────────────────────────────────────────
    private static void staffManagement(Scanner scanner, List<Account> users, Files file)
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

    private static void listAllStaff(List<Account> users) {
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

    private static void addReceptionist(Scanner scanner, List<Account> users, Files file)
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

    private static void deactivateStaff(Scanner scanner, List<Account> users, Files file) {
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

        System.out.println();
        System.out.println("  " + CLI.dim("Staff   : ") + CLI.bold(target.getUsername())
                + "  (" + target.getFirstName() + " " + target.getLastName()
                + ", " + CLI.cyan(target.getRole()) + ")");
        System.out.println("  " + CLI.warning(
                "Deactivation downgrades the account to USER. They keep their login "
                + "but lose all staff capabilities."));
        System.out.println();
        System.out.print(CLI.prompt("Type 'yes' to confirm (Esc to go back): "));
        String deactivateConfirm = CLI.readLine(scanner);
        if (deactivateConfirm == null) return;
        if (deactivateConfirm.equalsIgnoreCase("yes")) {
            target.setRole("USER");
            boolean ok = file.updateUsersFileAll(users);
            CLI.randomSpinner("Deactivating account");
            System.out.println(ok
                    ? CLI.success("Account deactivated (role set to USER).")
                    : CLI.warning("Deactivation applied in memory but disk write failed. Check the Errors log."));
        } else {
            System.out.println(CLI.dim("Deactivation cancelled."));
        }
        Main.pause(scanner);
    }

    // ─── Bookings & Stats ──────────────────────────────────────────────
    private static void viewAllBookings(List<Bookings> bookings) {
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

    /**
     * Comprehensive, at-a-glance stats page: inventory overview, booking-status
     * distribution with bar chart, revenue breakdown, a 7-day occupancy heat map,
     * and the top-earning rooms. Occupancy is computed via
     * {@link OccupancyCalendar#cellFor} so the numbers always agree with the
     * calendar view.
     */
    private static void viewStats(List<Room> rooms, List<Bookings> bookings) {
        CLI.randomSpinner("Loading statistics");
        CLI.clearScreen();
        CLI.printBanner("BOOKING STATISTICS");
        System.out.println();

        final LocalDate today = LocalDate.now();
        final DateTimeFormatter parse = DateTimeFormatter.ofPattern("dd-MM-uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        final DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE MMM d");

        // ── Aggregate the booking list in a single pass ──────────────────
        int cConfirmed = 0, cCheckedIn = 0, cCheckedOut = 0, cCancelled = 0, cMaintenance = 0;
        double revRealised = 0.0, revBooked = 0.0, revLost = 0.0;
        Map<String, Integer> staysPerRoom = new HashMap<>();
        Map<String, Double>  revPerRoom   = new HashMap<>();

        for (Bookings b : bookings) {
            String status = b.getStatus();
            String roomNo = b.getRoom().getRoomNumber();
            double value = 0.0;
            try {
                LocalDate ci = LocalDate.parse(b.getCheckIn(),  parse);
                LocalDate co = LocalDate.parse(b.getCheckOut(), parse);
                long nights = ChronoUnit.DAYS.between(ci, co);
                if (nights > 0) value = nights * b.getRoom().getPrice();
            } catch (Exception ignored) { /* leave value at 0 */ }

            switch (status) {
                case "CONFIRMED":
                    cConfirmed++;  revBooked  += value;
                    staysPerRoom.merge(roomNo, 1, Integer::sum);
                    revPerRoom  .merge(roomNo, value, Double::sum);
                    break;
                case "CHECKED_IN":
                    cCheckedIn++;  revBooked  += value;
                    staysPerRoom.merge(roomNo, 1, Integer::sum);
                    revPerRoom  .merge(roomNo, value, Double::sum);
                    break;
                case "CHECKED_OUT":
                    cCheckedOut++; revRealised += value;
                    staysPerRoom.merge(roomNo, 1, Integer::sum);
                    revPerRoom  .merge(roomNo, value, Double::sum);
                    break;
                case "CANCELLED":
                    cCancelled++;  revLost    += value;
                    break;
                case "MAINTENANCE":
                    cMaintenance++;
                    break;
                default:
                    cConfirmed++;  revBooked  += value;
            }
        }

        int totalBookings = bookings.size();
        int totalRooms    = rooms.size();
        int occupiedToday = occupiedCount(rooms, bookings, today);
        int pctToday      = percent(occupiedToday, totalRooms);
        double revTotal   = revRealised + revBooked;

        // ── OVERVIEW ─────────────────────────────────────────────────────
        printStatsSection("OVERVIEW");
        System.out.println("  " + labelVal("Bookings",
                CLI.bold(String.valueOf(totalBookings)) + CLI.dim("  total")));
        System.out.println("  " + labelVal("Rooms",
                CLI.bold(String.valueOf(totalRooms))    + CLI.dim("  total")));
        System.out.println("  " + labelVal("Today",
                renderBar(pctToday, 20, heatColor(pctToday))
                + "  " + CLI.bold(occupiedToday + "/" + totalRooms)
                + CLI.dim("  (" + pctToday + "%)")));
        System.out.println();

        // ── BOOKING STATUS ───────────────────────────────────────────────
        printStatsSection("BOOKING STATUS");
        if (totalBookings == 0) {
            System.out.println("  " + CLI.dim("No bookings yet."));
        } else {
            renderStatusRow("Confirmed",   cConfirmed,   totalBookings, CLI::green);
            renderStatusRow("Checked in",  cCheckedIn,   totalBookings, CLI::cyan);
            renderStatusRow("Checked out", cCheckedOut,  totalBookings, CLI::white);
            renderStatusRow("Cancelled",   cCancelled,   totalBookings, CLI::red);
            renderStatusRow("Maintenance", cMaintenance, totalBookings, CLI::magenta);
        }
        System.out.println();

        // ── REVENUE ──────────────────────────────────────────────────────
        printStatsSection("REVENUE");
        System.out.println("  " + labelVal("Realised",
                CLI.yellow(money(revRealised)) + CLI.dim("   completed stays")));
        System.out.println("  " + labelVal("Booked",
                CLI.yellow(money(revBooked))   + CLI.dim("   upcoming + in-house")));
        System.out.println("  " + labelVal("Lost",
                CLI.red(money(revLost))        + CLI.dim("   cancellations")));
        System.out.println("  " + CLI.dim("─".repeat(45)));
        System.out.println("  " + labelVal("Total",
                CLI.bold(CLI.yellow(money(revTotal)))));
        System.out.println();

        // ── OCCUPANCY — NEXT 7 DAYS ──────────────────────────────────────
        printStatsSection("OCCUPANCY — NEXT 7 DAYS");
        if (totalRooms == 0) {
            System.out.println("  " + CLI.dim("No rooms in inventory."));
        } else {
            for (int i = 0; i < 7; i++) {
                LocalDate d = today.plusDays(i);
                int occ = occupiedCount(rooms, bookings, d);
                int pct = percent(occ, totalRooms);
                String dateLabel = d.format(dayFmt);
                String leader = (i == 0) ? CLI.bold(dateLabel) + CLI.dim(" (today)")
                                         : CLI.dim(dateLabel);
                System.out.printf("  %-32s  %s  %s%s%n",
                        leader,
                        renderBar(pct, 20, heatColor(pct)),
                        CLI.bold(String.format("%d/%d", occ, totalRooms)),
                        CLI.dim(String.format("  (%d%%)", pct)));
            }
        }
        System.out.println();

        // ── TOP ROOMS BY REVENUE ─────────────────────────────────────────
        printStatsSection("TOP ROOMS BY REVENUE");
        if (revPerRoom.isEmpty()) {
            System.out.println("  " + CLI.dim("No paid stays recorded yet."));
        } else {
            List<Map.Entry<String, Double>> sorted = new ArrayList<>(revPerRoom.entrySet());
            sorted.sort((x, y) -> Double.compare(y.getValue(), x.getValue()));
            int rank = 1;
            for (Map.Entry<String, Double> e : sorted) {
                if (rank > 5) break;
                int stays = staysPerRoom.getOrDefault(e.getKey(), 0);
                System.out.printf("  %s  %s   %s%s%n",
                        CLI.cyan(rank + "."),
                        CLI.bold(e.getKey()),
                        CLI.yellow(money(e.getValue())),
                        CLI.dim("   " + stays + " stay" + (stays == 1 ? "" : "s")));
                rank++;
            }
        }

        CLI.printDivider();
    }

    // ── Stats-page helpers ────────────────────────────────────────────────

    /** Section title styled as " ── TITLE ──────── " in bold cyan. */
    private static void printStatsSection(String title) {
        int dashes = Math.max(3, 45 - title.length() - 4);
        System.out.println("  " + CLI.cyan(CLI.bold("── " + title + " " + "─".repeat(dashes))));
    }

    /** Fixed-width label + value so columns line up in the stats sections. */
    private static String labelVal(String label, String value) {
        return CLI.dim(String.format("%-12s", label)) + value;
    }

    /** Count rooms whose calendar cell on {@code date} is *not* AVAILABLE. */
    private static int occupiedCount(List<Room> rooms, List<Bookings> bookings, LocalDate date) {
        int n = 0;
        for (Room r : rooms) {
            if (OccupancyCalendar.cellFor(r, date, bookings) != OccupancyCalendar.CellStatus.AVAILABLE) n++;
        }
        return n;
    }

    private static int percent(int part, int whole) {
        return whole == 0 ? 0 : Math.round(100f * part / whole);
    }

    /**
     * Heat palette for occupancy bars: cooler colours for idle days, warmer for
     * busy. Gives each day a glanceable pulse even without reading the number.
     */
    private static Function<String, String> heatColor(int pct) {
        if (pct >= 85) return CLI::red;
        if (pct >= 60) return CLI::yellow;
        if (pct >= 30) return CLI::green;
        return CLI::cyan;
    }

    /** One row in the BOOKING STATUS section: coloured dot, label, bar, count, %. */
    private static void renderStatusRow(String label, int count, int total,
                                         Function<String, String> colorFn) {
        int pct = percent(count, total);
        System.out.printf("  %s %-14s  %s  %s%s%n",
                colorFn.apply("●"),
                label,
                renderBar(pct, 20, colorFn),
                CLI.bold(String.format("%3d", count)),
                CLI.dim(String.format("  (%d%%)", pct)));
    }

    /**
     * Fixed-width horizontal bar: coloured filled segment + dim empty segment.
     * ASCII fallback ('#', '.') for dumb terminals keeps the visual intact.
     */
    private static String renderBar(int pct, int width, Function<String, String> colorFn) {
        int clamped = Math.max(0, Math.min(100, pct));
        int filled  = Math.min(width, Math.round((float) clamped * width / 100f));
        String fillChar  = CLI.supportsAnsi() ? "█" : "#";
        String emptyChar = CLI.supportsAnsi() ? "░" : ".";
        return colorFn.apply(fillChar.repeat(filled))
             + CLI.dim(emptyChar.repeat(width - filled));
    }

    private static String money(double v) {
        return String.format("$%,.2f", v);
    }
}
