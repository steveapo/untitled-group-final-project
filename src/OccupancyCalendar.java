import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.Vector;

/**
 * Interactive ASCII occupancy calendar.
 *
 * <p>Renders a 7-day Gantt-style grid: rooms as rows, days as columns.
 * Cells are colour-coded by booking/room status. The view is navigable
 * with arrow keys (or vim-style fallback); ESC exits.
 *
 * <p>Entry point: {@link #show(Scanner, Vector, Vector, boolean)}.
 * The only stateful I/O is inside {@code handleKeys}; everything else
 * is a pure function of the in-memory vectors passed in by the caller.
 */
public class OccupancyCalendar {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ── Cell status ──────────────────────────────────────────────────────

    /** Visibility: package-private so OccupancyCalendarTest can reference it. */
    enum CellStatus { AVAILABLE, CONFIRMED, CHECKED_IN, CHECKED_OUT, MAINTENANCE }

    // ── ANSI helpers ─────────────────────────────────────────────────────

    private static final String RESET     = "\033[0m";
    private static final String BOLD      = "\033[1m";
    private static final String UNDERLINE = "\033[4m";
    private static final String GREEN     = "\033[32m";
    private static final String CYAN      = "\033[36m";
    private static final String MAGENTA   = "\033[35m";
    private static final String DIM       = "\033[2m";
    private static final String RED       = "\033[31m";

    /** Detect ANSI support by probing the TERM / NO_COLOR env vars and JLine. */
    private static boolean isAnsiSupported() {
        if (System.getenv("NO_COLOR") != null) return false;
        if ("dumb".equals(System.getenv("TERM"))) return false;
        // Use CLI.bold("") as a canary: if it returns the text unchanged, ANSI is off.
        return !CLI.bold("X").equals("X");
    }

    private static String ansi(String code, String text) {
        return isAnsiSupported() ? code + text + RESET : text;
    }

    // Glyph pairs (2 chars wide so columns align)
    private static final String GLYPH_AVAILABLE   = "░░";
    private static final String GLYPH_CONFIRMED   = "██";
    private static final String GLYPH_CHECKED_IN  = "██";
    private static final String GLYPH_CHECKED_OUT = "▒▒";
    private static final String GLYPH_MAINTENANCE = "▒▒";

    // ASCII fallback glyphs (for dumb terminals)
    private static final String FALLBACK_AVAILABLE   = "..";
    private static final String FALLBACK_CONFIRMED   = "##";
    private static final String FALLBACK_CHECKED_IN  = "##";
    private static final String FALLBACK_CHECKED_OUT = "xx";
    private static final String FALLBACK_MAINTENANCE = "MM";

    // ── Public entry point ───────────────────────────────────────────────

    /**
     * Display the interactive occupancy calendar and block until the user
     * presses ESC (or 'e' on dumb terminals).
     *
     * @param scanner        fallback scanner for dumb-terminal key input
     * @param rooms          all rooms (read-only)
     * @param bookings       all bookings (read-only)
     * @param showGuestNames reserved for future guest-identity features;
     *                       pass {@code true} for Reception/Manager,
     *                       {@code false} for User
     */
    public static void show(Scanner scanner,
                            Vector<Room> rooms,
                            Vector<Bookings> bookings,
                            boolean showGuestNames) {
        LocalDate windowStart = LocalDate.now();
        handleKeys(scanner, rooms, bookings, showGuestNames, windowStart);
    }

    // ── Key loop ─────────────────────────────────────────────────────────

    private static void handleKeys(Scanner scanner,
                                   Vector<Room> rooms,
                                   Vector<Bookings> bookings,
                                   boolean showGuestNames,
                                   LocalDate initialWindow) {
        LocalDate windowStart = initialWindow;
        while (true) {
            CLI.clearScreen();
            render(rooms, bookings, windowStart);
            String key = CLI.readArrowOrKey(scanner);
            switch (key) {
                case "LEFT":        windowStart = windowStart.minusDays(1);  break;
                case "RIGHT":       windowStart = windowStart.plusDays(1);   break;
                case "SHIFT_LEFT":  windowStart = windowStart.minusWeeks(1); break;
                case "SHIFT_RIGHT": windowStart = windowStart.plusWeeks(1);  break;
                case "T":           windowStart = LocalDate.now();            break;
                case "ESC":         return;
                default:            break;
            }
        }
    }

    // ── Renderer ─────────────────────────────────────────────────────────

    private static void render(Vector<Room> rooms,
                               Vector<Bookings> bookings,
                               LocalDate windowStart) {
        LocalDate today = LocalDate.now();
        boolean ansi = isAnsiSupported();

        // Banner
        String weekLabel = windowStart.format(DateTimeFormatter.ofPattern("MMM d"));
        System.out.println(ansi(BOLD,
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║         OCCUPANCY CALENDAR (week of " + padRight(weekLabel, 10) + ")         ║\n" +
                "╚══════════════════════════════════════════════════════════╝"));

        if (rooms.isEmpty()) {
            System.out.println("\n  No rooms configured.\n\n  Press Esc to go back.");
            return;
        }

        // Day-of-week names + day numbers header
        String[] dayNames = new String[7];
        String[] dayNums  = new String[7];
        for (int i = 0; i < 7; i++) {
            LocalDate d = windowStart.plusDays(i);
            dayNames[i] = d.getDayOfWeek().toString().substring(0, 3);
            dayNums[i]  = String.valueOf(d.getDayOfMonth());
        }

        // Header row
        StringBuilder header = new StringBuilder("         ");
        for (int i = 0; i < 7; i++) {
            LocalDate d = windowStart.plusDays(i);
            String label = padRight(dayNames[i], 3);
            if (d.equals(today) && ansi) {
                header.append(BOLD).append(UNDERLINE).append(label).append(RESET).append(" ");
            } else {
                header.append(label).append(" ");
            }
        }
        System.out.println();
        System.out.println(header);

        StringBuilder numRow = new StringBuilder("         ");
        for (int i = 0; i < 7; i++) {
            LocalDate d = windowStart.plusDays(i);
            String label = padRight(dayNums[i], 3);
            if (d.equals(today) && ansi) {
                numRow.append(BOLD).append(UNDERLINE).append(label).append(RESET).append(" ");
            } else {
                numRow.append(label).append(" ");
            }
        }
        System.out.println(numRow);

        // Grid
        System.out.println("        ┌───┬───┬───┬───┬───┬───┬───┐");
        for (int r = 0; r < rooms.size(); r++) {
            Room room = rooms.get(r);
            StringBuilder row = new StringBuilder();
            row.append("  ").append(padRight(room.getRoomNumber(), 4)).append("  │");
            for (int i = 0; i < 7; i++) {
                LocalDate d = windowStart.plusDays(i);
                CellStatus cs = cellFor(room, d, bookings);
                row.append(styledCell(cs, ansi)).append("│");
            }
            System.out.println(row);
            if (r < rooms.size() - 1) {
                System.out.println("        ├───┼───┼───┼───┼───┼───┼───┤");
            }
        }
        System.out.println("        └───┴───┴───┴───┴───┴───┴───┘");

        // Legend
        System.out.println();
        if (ansi) {
            System.out.println("  Legend:  " +
                    GREEN   + GLYPH_AVAILABLE   + RESET + " Available   " +
                    CYAN    + GLYPH_CONFIRMED   + RESET + " Confirmed   " +
                    MAGENTA + GLYPH_CHECKED_IN  + RESET + " Checked-in");
            System.out.println("           " +
                    DIM     + GLYPH_CHECKED_OUT + RESET + " Checked-out " +
                    RED     + GLYPH_MAINTENANCE + RESET + " Maintenance");
        } else {
            System.out.println("  Legend:  .. Available  ## Confirmed  ## Checked-in");
            System.out.println("           xx Checked-out  MM Maintenance");
        }

        // Hotkey footer
        System.out.println();
        System.out.println("  \u2190 \u2192 Day   Shift+\u2190 \u2192 Week   T Today   Esc Back");
        System.out.println("  (or: h l day, H L week, t today, e back)");
    }

    // ── Cell computation ─────────────────────────────────────────────────

    /**
     * Determine the display status for a given room on a given date.
     * Package-private so unit tests can call it directly.
     *
     * <p>Decision order:
     * <ol>
     *   <li>Room status == "MAINTENANCE" → {@code MAINTENANCE} (overrides bookings)
     *   <li>First matching non-CANCELLED booking in {@code [checkIn, checkOut)} → booking status
     *   <li>Otherwise → {@code AVAILABLE}
     * </ol>
     *
     * <p>The checkout day is exclusive (consistent with {@code DateInput.checkBookingsDate}).
     */
    static CellStatus cellFor(Room room, LocalDate date, Vector<Bookings> bookings) {
        if ("MAINTENANCE".equalsIgnoreCase(room.getStatus())) {
            return CellStatus.MAINTENANCE;
        }
        for (Bookings b : bookings) {
            if (!b.getRoom().getRoomNumber().equals(room.getRoomNumber())) continue;
            if ("CANCELLED".equalsIgnoreCase(b.getStatus())) continue;
            LocalDate checkIn, checkOut;
            try {
                checkIn  = LocalDate.parse(b.getCheckIn(),  FMT);
                checkOut = LocalDate.parse(b.getCheckOut(), FMT);
            } catch (DateTimeParseException e) {
                continue; // malformed record — treat as not on this day
            }
            // [checkIn, checkOut) — checkout day is exclusive
            if (!date.isBefore(checkIn) && date.isBefore(checkOut)) {
                switch (b.getStatus().toUpperCase()) {
                    case "CONFIRMED":   return CellStatus.CONFIRMED;
                    case "CHECKED_IN":  return CellStatus.CHECKED_IN;
                    case "CHECKED_OUT": return CellStatus.CHECKED_OUT;
                    default:            return CellStatus.CONFIRMED;
                }
            }
        }
        return CellStatus.AVAILABLE;
    }

    // ── Private utilities ─────────────────────────────────────────────────

    private static String styledCell(CellStatus cs, boolean ansi) {
        if (ansi) {
            switch (cs) {
                case AVAILABLE:   return GREEN   + GLYPH_AVAILABLE   + RESET;
                case CONFIRMED:   return CYAN    + GLYPH_CONFIRMED   + RESET;
                case CHECKED_IN:  return MAGENTA + GLYPH_CHECKED_IN  + RESET;
                case CHECKED_OUT: return DIM     + GLYPH_CHECKED_OUT + RESET;
                case MAINTENANCE: return RED     + GLYPH_MAINTENANCE + RESET;
            }
        } else {
            switch (cs) {
                case AVAILABLE:   return FALLBACK_AVAILABLE;
                case CONFIRMED:   return FALLBACK_CONFIRMED;
                case CHECKED_IN:  return FALLBACK_CHECKED_IN;
                case CHECKED_OUT: return FALLBACK_CHECKED_OUT;
                case MAINTENANCE: return FALLBACK_MAINTENANCE;
            }
        }
        return "??";
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
