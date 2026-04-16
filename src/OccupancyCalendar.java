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
        // selectedDay is the focused/cursor day; the 7-day window is always
        // centered on it (3 days before, selected, 3 days after).
        LocalDate selectedDay = initialWindow;
        while (true) {
            LocalDate windowStart = selectedDay.minusDays(3);
            CLI.clearScreen();
            render(rooms, bookings, windowStart, selectedDay);
            String key = CLI.readArrowOrKey(scanner);
            switch (key) {
                case "LEFT":        selectedDay = selectedDay.minusDays(1);  break;
                case "RIGHT":       selectedDay = selectedDay.plusDays(1);   break;
                case "SHIFT_LEFT":  selectedDay = selectedDay.minusWeeks(1); break;
                case "SHIFT_RIGHT": selectedDay = selectedDay.plusWeeks(1);  break;
                case "T":           selectedDay = LocalDate.now();            break;
                case "ESC":         return;
                default:            break;
            }
        }
    }

    // ── Renderer ─────────────────────────────────────────────────────────

    // Layout constants — every column is exactly COL_W visible chars wide.
    // The grid draws │ + COL_W chars + │, so header columns must also be COL_W each.
    private static final int   COL_W      = 4;   // visible width per day column
    private static final String SEP_LINE  = "────";  // COL_W dashes
    private static final String CROSS     = "┼";
    private static final String T_DOWN    = "┬";
    private static final String T_UP      = "┴";
    private static final String CORNER_TL = "┌";
    private static final String CORNER_TR = "┐";
    private static final String CORNER_BL = "└";
    private static final String CORNER_BR = "┘";
    private static final String VERT      = "│";
    private static final String HORIZ_L   = "├";
    private static final String HORIZ_R   = "┤";

    // Row label prefix: "  R101  │" — 2 + 4 (room) + 2 + 1 (│) = 9 chars before first cell.
    // Header prefix must be the same 8 visible chars (no │ — that's part of the grid).
    private static final String ROW_PREFIX    = "  ";   // 2 spaces before room number
    private static final int    ROOM_COL_W    = 4;      // room number visible width
    private static final String ROW_SEPARATOR = "  ";   // 2 spaces between room# and │

    // Full visible prefix width = ROW_PREFIX + ROOM_COL_W + ROW_SEPARATOR = 2+4+2 = 8
    private static final String HDR_INDENT = "          "; // 10 spaces to align with first cell

    private static final String INVERT = "\033[7m";  // reverse video = cursor highlight

    private static void render(Vector<Room> rooms,
                               Vector<Bookings> bookings,
                               LocalDate windowStart,
                               LocalDate selectedDay) {
        LocalDate today = LocalDate.now();
        boolean ansi = isAnsiSupported();

        // Banner — show the selected date prominently
        String selLabel = selectedDay.format(DateTimeFormatter.ofPattern("EEE MMM d"));
        String banner =
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║          OCCUPANCY CALENDAR  ▸ " + padRight(selLabel, 14) + "           ║\n" +
                "╚══════════════════════════════════════════════════════════╝";
        System.out.println(ansi(BOLD, banner));

        if (rooms.isEmpty()) {
            System.out.println("\n  No rooms configured.\n\n  Press Esc to go back.");
            return;
        }

        // Build separator lines using COL_W
        // e.g. "┌────┬────┬────┬────┬────┬────┬────┐"
        StringBuilder topLine  = new StringBuilder(HDR_INDENT).append(CORNER_TL);
        StringBuilder midLine  = new StringBuilder(HDR_INDENT).append(HORIZ_L);
        StringBuilder botLine  = new StringBuilder(HDR_INDENT).append(CORNER_BL);
        for (int i = 0; i < 7; i++) {
            topLine.append(SEP_LINE).append(i < 6 ? T_DOWN : CORNER_TR);
            midLine.append(SEP_LINE).append(i < 6 ? CROSS  : HORIZ_R);
            botLine.append(SEP_LINE).append(i < 6 ? T_UP   : CORNER_BR);
        }

        // Header rows: day-of-week name and day number, each COL_W wide
        StringBuilder nameRow = new StringBuilder(HDR_INDENT);
        StringBuilder numRow  = new StringBuilder(HDR_INDENT);
        for (int i = 0; i < 7; i++) {
            LocalDate d = windowStart.plusDays(i);
            boolean isSelected = d.equals(selectedDay);
            boolean isToday    = d.equals(today);

            // Build the visible text (COL_W chars each, centred with a leading space)
            String nameVis = " " + padRight(d.getDayOfWeek().toString().substring(0, 3), COL_W - 1);
            String numVis  = " " + padRight(String.valueOf(d.getDayOfMonth()), COL_W - 1);

            if (ansi) {
                if (isSelected) {
                    // Cursor column: inverse video + bold
                    nameRow.append(INVERT).append(BOLD).append(nameVis).append(RESET);
                    numRow .append(INVERT).append(BOLD).append(numVis) .append(RESET);
                } else if (isToday) {
                    // Today (not selected): bold + underline
                    nameRow.append(BOLD).append(UNDERLINE).append(nameVis).append(RESET);
                    numRow .append(BOLD).append(UNDERLINE).append(numVis) .append(RESET);
                } else {
                    nameRow.append(nameVis);
                    numRow .append(numVis);
                }
            } else {
                // Dumb terminal: mark selected with [ ] brackets
                if (isSelected) {
                    nameRow.append("[").append(d.getDayOfWeek().toString().substring(0, 2)).append("]");
                    numRow .append("[").append(padRight(String.valueOf(d.getDayOfMonth()), 2)).append("]");
                } else {
                    nameRow.append(nameVis);
                    numRow .append(numVis);
                }
            }
        }

        System.out.println();
        System.out.println(nameRow);
        System.out.println(numRow);
        System.out.println(topLine);

        // Grid rows
        for (int r = 0; r < rooms.size(); r++) {
            Room room = rooms.get(r);
            StringBuilder row = new StringBuilder();
            row.append(ROW_PREFIX)
               .append(padRight(room.getRoomNumber(), ROOM_COL_W))
               .append(ROW_SEPARATOR)
               .append(VERT);
            for (int i = 0; i < 7; i++) {
                LocalDate d = windowStart.plusDays(i);
                CellStatus cs = cellFor(room, d, bookings);
                boolean isSelected = d.equals(selectedDay);
                row.append(styledCell(cs, ansi, isSelected)).append(VERT);
            }
            System.out.println(row);
            if (r < rooms.size() - 1) {
                System.out.println(midLine);
            }
        }
        System.out.println(botLine);

        // Legend
        System.out.println();
        if (ansi) {
            System.out.println("  Legend:  " +
                    GREEN   + GLYPH_AVAILABLE   + RESET + " Available   " +
                    CYAN    + GLYPH_CONFIRMED   + RESET + " Confirmed   " +
                    MAGENTA + GLYPH_CHECKED_IN  + RESET + " Checked-in");
            System.out.println("           " +
                    DIM     + GLYPH_CHECKED_OUT + RESET + " Checked-out " +
                    RED     + GLYPH_MAINTENANCE + RESET + " Maintenance   " +
                    INVERT + " " + RESET + " Selected column");
        } else {
            System.out.println("  Legend:  .. Available  ## Confirmed  ## Checked-in");
            System.out.println("           xx Checked-out  MM Maintenance  [..] Selected");
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

    private static String styledCell(CellStatus cs, boolean ansi, boolean selected) {
        if (!ansi) {
            String g;
            switch (cs) {
                case AVAILABLE:   g = FALLBACK_AVAILABLE;   break;
                case CONFIRMED:   g = FALLBACK_CONFIRMED;   break;
                case CHECKED_IN:  g = FALLBACK_CHECKED_IN;  break;
                case CHECKED_OUT: g = FALLBACK_CHECKED_OUT; break;
                case MAINTENANCE: g = FALLBACK_MAINTENANCE; break;
                default:          g = "??";
            }
            return selected ? "[" + g.charAt(0) + "]" : " " + g + " ";
        }
        // ANSI path: colour glyph, then wrap selected column in inverse-video
        String colour;
        String glyph;
        switch (cs) {
            case AVAILABLE:   colour = GREEN;   glyph = GLYPH_AVAILABLE;   break;
            case CONFIRMED:   colour = CYAN;    glyph = GLYPH_CONFIRMED;   break;
            case CHECKED_IN:  colour = MAGENTA; glyph = GLYPH_CHECKED_IN;  break;
            case CHECKED_OUT: colour = DIM;     glyph = GLYPH_CHECKED_OUT; break;
            case MAINTENANCE: colour = RED;     glyph = GLYPH_MAINTENANCE; break;
            default:          colour = "";      glyph = "??";
        }
        // Each cell is COL_W visible chars: space + glyph(2) + space = 4
        // Selected: invert the whole cell background so it stands out clearly
        if (selected) {
            return INVERT + colour + " " + glyph + " " + RESET;
        }
        return colour + " " + glyph + " " + RESET;
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
