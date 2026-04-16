import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.Vector;

/**
 * Interactive ASCII occupancy calendar.
 *
 * <p>Two modes:
 * <ul>
 *   <li><b>VIEW mode</b> ({@link #show}): read-only, all rooms, ESC exits.
 *   <li><b>PICK mode</b> ({@link #pickDates}): booking flow for a user.
 *       Shows only rooms that fit the requested capacity, lets the user
 *       navigate with ←/→/↑/↓, press ENTER to set check-in then check-out,
 *       and returns a {@link BookingSelection} (or {@code null} on cancel).
 * </ul>
 */
public class OccupancyCalendar {

    private static final DateTimeFormatter FMT     = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FMT_OUT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
                                                         .withResolverStyle(ResolverStyle.STRICT);

    // ── Cell status ──────────────────────────────────────────────────────

    /** Visibility: package-private so OccupancyCalendarTest can reference it. */
    enum CellStatus { AVAILABLE, CONFIRMED, CHECKED_IN, CHECKED_OUT, MAINTENANCE }

    // ── Result type for PICK mode ────────────────────────────────────────

    /** Returned by {@link #pickDates} when the user confirms a selection. */
    static final class BookingSelection {
        final Room      room;
        final LocalDate checkIn;
        final LocalDate checkOut;
        BookingSelection(Room room, LocalDate checkIn, LocalDate checkOut) {
            this.room     = room;
            this.checkIn  = checkIn;
            this.checkOut = checkOut;
        }
    }

    // ── ANSI helpers ─────────────────────────────────────────────────────

    private static final String RESET     = "\033[0m";
    private static final String BOLD      = "\033[1m";
    private static final String UNDERLINE = "\033[4m";
    private static final String GREEN     = "\033[32m";
    private static final String CYAN      = "\033[36m";
    private static final String MAGENTA   = "\033[35m";
    private static final String DIM       = "\033[2m";
    private static final String RED       = "\033[31m";
    private static final String YELLOW    = "\033[33m";
    private static final String INVERT    = "\033[7m";
    private static final String BG_GREEN  = "\033[42m";

    private static boolean isAnsiSupported() {
        if (System.getenv("NO_COLOR") != null) return false;
        if ("dumb".equals(System.getenv("TERM"))) return false;
        return !CLI.bold("X").equals("X");
    }

    private static String ansi(String code, String text) {
        return isAnsiSupported() ? code + text + RESET : text;
    }

    // ── Glyphs ───────────────────────────────────────────────────────────

    private static final String GLYPH_AVAILABLE   = "░░";
    private static final String GLYPH_CONFIRMED   = "██";
    private static final String GLYPH_CHECKED_IN  = "██";
    private static final String GLYPH_CHECKED_OUT = "▒▒";
    private static final String GLYPH_MAINTENANCE = "▒▒";

    private static final String FB_AVAILABLE   = "..";
    private static final String FB_CONFIRMED   = "##";
    private static final String FB_CHECKED_IN  = "##";
    private static final String FB_CHECKED_OUT = "xx";
    private static final String FB_MAINTENANCE = "MM";

    // ── Layout ───────────────────────────────────────────────────────────

    // Each day column: │ + COL_W visible chars + next │
    private static final int    COL_W      = 4;
    private static final String SEP_LINE   = "────";   // COL_W '─'
    private static final String VERT       = "│";
    private static final String CORNER_TL  = "┌";
    private static final String CORNER_TR  = "┐";
    private static final String CORNER_BL  = "└";
    private static final String CORNER_BR  = "┘";
    private static final String T_DOWN     = "┬";
    private static final String T_UP       = "┴";
    private static final String CROSS      = "┼";
    private static final String HORIZ_L    = "├";
    private static final String HORIZ_R    = "┤";

    // Row label area visible width.
    // Format: "  R101  Dbl  $120/n  " — 2 + 4 + 2 + 3 + 2 + 7 + 2 = uses LABEL_W
    // We keep it at 22 so it fits common room data without wrapping.
    private static final int    LABEL_W    = 22;
    // Header indentation must equal LABEL_W to align with grid columns.
    private static final String HDR_INDENT = " ".repeat(LABEL_W);

    // ── Public entry points ───────────────────────────────────────────────

    /**
     * VIEW mode: read-only calendar, all rooms, ESC exits.
     */
    public static void show(Scanner scanner,
                            Vector<Room> rooms,
                            Vector<Bookings> bookings,
                            boolean showGuestNames) {
        LocalDate selectedDay = LocalDate.now();
        int selectedRow = 0;
        while (true) {
            LocalDate windowStart = selectedDay.minusDays(3);
            CLI.clearScreen();
            renderView(rooms, bookings, windowStart, selectedDay, selectedRow, null, null);
            String key = CLI.readArrowOrKey(scanner);
            switch (key) {
                case "LEFT":        selectedDay = selectedDay.minusDays(1);              break;
                case "RIGHT":       selectedDay = selectedDay.plusDays(1);               break;
                case "SHIFT_LEFT":  selectedDay = selectedDay.minusWeeks(1);             break;
                case "SHIFT_RIGHT": selectedDay = selectedDay.plusWeeks(1);              break;
                case "UP":          if (selectedRow > 0) selectedRow--;                  break;
                case "DOWN":        if (selectedRow < rooms.size() - 1) selectedRow++;   break;
                case "T":           selectedDay = LocalDate.now();                        break;
                case "ESC":         return;
                default:            break;
            }
        }
    }

    /**
     * PICK mode: interactive date picker for booking.
     *
     * <p>Shows only rooms whose capacity &ge; {@code minCapacity} and whose
     * status is not MAINTENANCE.  The user navigates left/right to choose
     * check-in (ENTER), then moves right to choose check-out (ENTER again).
     * A room row must be selected (UP/DOWN) — the check-in and check-out
     * must both fall on the same room row.
     *
     * @return a {@link BookingSelection}, or {@code null} if the user cancelled.
     */
    public static BookingSelection pickDates(Scanner scanner,
                                             Vector<Room> allRooms,
                                             Vector<Bookings> bookings,
                                             int minCapacity) {
        // Filter to rooms that can accommodate the party and aren't in maintenance
        Vector<Room> rooms = new Vector<>();
        for (Room r : allRooms) {
            if (r.getCapacity() >= minCapacity && !"MAINTENANCE".equalsIgnoreCase(r.getStatus())) {
                rooms.add(r);
            }
        }
        if (rooms.isEmpty()) return null;

        LocalDate selectedDay = LocalDate.now();
        int selectedRow = 0;
        LocalDate checkIn = null;   // null until first ENTER

        while (true) {
            LocalDate windowStart = selectedDay.minusDays(3);
            CLI.clearScreen();
            renderView(rooms, bookings, windowStart, selectedDay, selectedRow, checkIn, null);
            String key = CLI.readArrowOrKey(scanner);
            switch (key) {
                case "LEFT":        selectedDay = selectedDay.minusDays(1);                          break;
                case "RIGHT":       selectedDay = selectedDay.plusDays(1);                           break;
                case "SHIFT_LEFT":  selectedDay = selectedDay.minusWeeks(1);                         break;
                case "SHIFT_RIGHT": selectedDay = selectedDay.plusWeeks(1);                          break;
                case "UP":          if (selectedRow > 0) selectedRow--;                              break;
                case "DOWN":        if (selectedRow < rooms.size() - 1) selectedRow++;               break;
                case "T":           selectedDay = LocalDate.now();                                    break;
                case "ESC":
                    if (checkIn != null) {
                        checkIn = null;      // cancel check-in, back to step 1
                    } else {
                        return null;         // full cancel
                    }
                    break;
                case "ENTER":
                    Room curRoom = rooms.get(selectedRow);
                    if (checkIn == null) {
                        // Step 1: set check-in — must be available on that day
                        if (cellFor(curRoom, selectedDay, bookings) == CellStatus.AVAILABLE) {
                            checkIn = selectedDay;
                        }
                        // else ignore — cell isn't available
                    } else {
                        // Step 2: set check-out — must be after check-in, on same room,
                        // and all days in [checkIn, selectedDay) must be available
                        if (selectedDay.isAfter(checkIn)
                                && allAvailable(curRoom, checkIn, selectedDay, bookings)) {
                            return new BookingSelection(curRoom, checkIn, selectedDay);
                        }
                        // else ignore invalid range
                    }
                    break;
                default: break;
            }
        }
    }

    // ── Renderer ─────────────────────────────────────────────────────────

    /**
     * Render the grid.
     *
     * @param rooms        rooms to display (already filtered if in pick mode)
     * @param windowStart  first visible day
     * @param selectedDay  the column cursor
     * @param selectedRow  the row cursor (highlighted room row)
     * @param checkIn      if non-null, the locked check-in date (pick mode step 2)
     * @param checkOut     unused (reserved for future preview highlight)
     */
    private static void renderView(Vector<Room> rooms,
                                   Vector<Bookings> bookings,
                                   LocalDate windowStart,
                                   LocalDate selectedDay,
                                   int selectedRow,
                                   LocalDate checkIn,
                                   LocalDate checkOut) {
        LocalDate today = LocalDate.now();
        boolean ansi = isAnsiSupported();
        boolean pickMode = (checkIn != null || checkOut != null);

        // ── Banner ────────────────────────────────────────────────────────
        String selLabel = selectedDay.format(DateTimeFormatter.ofPattern("EEE MMM d"));
        String modeTag  = checkIn == null ? "Select check-in" : "Select check-out";
        String bannerMid = pickMode
                ? "BOOK A ROOM  ▸ " + padRight(selLabel, 12) + "  [" + modeTag + "]"
                : "OCCUPANCY CALENDAR  ▸ " + padRight(selLabel, 14);
        String banner =
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║  " + padRight(bannerMid, 56) + "║\n" +
                "╚══════════════════════════════════════════════════════════╝";
        System.out.println(ansi(BOLD, banner));

        if (rooms.isEmpty()) {
            System.out.println("\n  No rooms available.\n\n  Press Esc to go back.");
            return;
        }

        // ── Grid separator lines ──────────────────────────────────────────
        StringBuilder topLine = new StringBuilder(HDR_INDENT).append(CORNER_TL);
        StringBuilder midLine = new StringBuilder(HDR_INDENT).append(HORIZ_L);
        StringBuilder botLine = new StringBuilder(HDR_INDENT).append(CORNER_BL);
        for (int i = 0; i < 7; i++) {
            topLine.append(SEP_LINE).append(i < 6 ? T_DOWN : CORNER_TR);
            midLine.append(SEP_LINE).append(i < 6 ? CROSS  : HORIZ_R);
            botLine.append(SEP_LINE).append(i < 6 ? T_UP   : CORNER_BR);
        }

        // ── Day header rows ───────────────────────────────────────────────
        StringBuilder nameRow = new StringBuilder(HDR_INDENT);
        StringBuilder numRow  = new StringBuilder(HDR_INDENT);
        for (int i = 0; i < 7; i++) {
            LocalDate d = windowStart.plusDays(i);
            boolean isColSel = d.equals(selectedDay);
            boolean isToday  = d.equals(today);
            String nameVis = " " + padRight(d.getDayOfWeek().toString().substring(0, 3), COL_W - 1);
            String numVis  = " " + padRight(String.valueOf(d.getDayOfMonth()), COL_W - 1);
            if (ansi) {
                if (isColSel) {
                    nameRow.append(INVERT).append(BOLD).append(nameVis).append(RESET);
                    numRow .append(INVERT).append(BOLD).append(numVis) .append(RESET);
                } else if (isToday) {
                    nameRow.append(BOLD).append(UNDERLINE).append(nameVis).append(RESET);
                    numRow .append(BOLD).append(UNDERLINE).append(numVis) .append(RESET);
                } else {
                    nameRow.append(nameVis);
                    numRow .append(numVis);
                }
            } else {
                if (isColSel) {
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

        // ── Grid rows ─────────────────────────────────────────────────────
        for (int r = 0; r < rooms.size(); r++) {
            Room room = rooms.get(r);
            boolean isRowSel = (r == selectedRow);

            // Row label: room number + type + price, padded to LABEL_W
            String label = buildLabel(room, isRowSel, ansi);

            StringBuilder row = new StringBuilder();
            row.append(label).append(VERT);

            for (int i = 0; i < 7; i++) {
                LocalDate d = windowStart.plusDays(i);
                CellStatus cs = cellFor(room, d, bookings);
                boolean isColSel = d.equals(selectedDay);
                boolean inRange  = checkIn != null && !d.isBefore(checkIn) && d.isBefore(selectedDay);
                row.append(styledCell(cs, ansi, isColSel && isRowSel, inRange && isRowSel)).append(VERT);
            }
            System.out.println(row);
            if (r < rooms.size() - 1) {
                // For the selected row's separator, highlight the separator line too
                if (isRowSel && ansi) {
                    System.out.println(ansi(BOLD, midLine.toString()));
                } else {
                    System.out.println(midLine);
                }
            }
        }
        System.out.println(botLine);

        // ── Check-in indicator ────────────────────────────────────────────
        if (checkIn != null) {
            String ciStr = checkIn.format(DateTimeFormatter.ofPattern("EEE MMM d"));
            System.out.println("\n  " + ansi(BG_GREEN + BOLD, " Check-in: " + ciStr + " ")
                    + "  → now select check-out date and press Enter");
        }

        // ── Legend ────────────────────────────────────────────────────────
        System.out.println();
        if (ansi) {
            System.out.println("  Legend:  "
                    + GREEN   + GLYPH_AVAILABLE   + RESET + " Available   "
                    + CYAN    + GLYPH_CONFIRMED   + RESET + " Confirmed   "
                    + MAGENTA + GLYPH_CHECKED_IN  + RESET + " Checked-in");
            System.out.println("           "
                    + DIM     + GLYPH_CHECKED_OUT + RESET + " Checked-out "
                    + RED     + GLYPH_MAINTENANCE + RESET + " Maintenance");
        } else {
            System.out.println("  Legend:  .. Available  ## Confirmed  ## Checked-in");
            System.out.println("           xx Checked-out  MM Maintenance");
        }

        // ── Footer ────────────────────────────────────────────────────────
        System.out.println();
        if (pickMode) {
            System.out.println("  \u2190 \u2192 Day   Shift+\u2190\u2192 Week   \u2191 \u2193 Room   Enter Select   Esc " + (checkIn != null ? "Clear check-in" : "Cancel"));
        } else {
            System.out.println("  \u2190 \u2192 Day   Shift+\u2190\u2192 Week   \u2191 \u2193 Room   T Today   Esc Back");
        }
        System.out.println("  (or: h l day, H L week, k j room, t today, e back)");
    }

    // ── Label builder ─────────────────────────────────────────────────────

    private static String buildLabel(Room room, boolean selected, boolean ansi) {
        // Format: "  R101  Dbl  $120/n  " (LABEL_W total visible chars)
        String typeShort = room.getType().length() >= 3 ? room.getType().substring(0, 3) : room.getType();
        String price     = String.format("$%d/n", (int) room.getPrice());
        String cap       = room.getCapacity() + "p";
        // visible content: roomNum(4) + "  " + type(3) + "  " + price(up to 7) + "  " + cap(2-3)
        String content   = padRight(room.getRoomNumber(), 4)
                         + "  " + padRight(typeShort, 3)
                         + "  " + padRight(price, 7)
                         + "  " + cap;
        // wrap in 2-space left margin, pad to LABEL_W
        String vis = "  " + padRight(content, LABEL_W - 2);

        if (!ansi) return vis;
        if (selected) return BOLD + INVERT + vis + RESET;
        return vis;
    }

    // ── Cell helpers ──────────────────────────────────────────────────────

    /**
     * Returns true if every day in [checkIn, checkOut) is AVAILABLE for the room.
     * checkOut is exclusive.
     */
    private static boolean allAvailable(Room room, LocalDate checkIn,
                                         LocalDate checkOut, Vector<Bookings> bookings) {
        LocalDate d = checkIn;
        while (d.isBefore(checkOut)) {
            if (cellFor(room, d, bookings) != CellStatus.AVAILABLE) return false;
            d = d.plusDays(1);
        }
        return true;
    }

    // ── Cell computation ─────────────────────────────────────────────────

    /**
     * Determine the display status for a given room on a given date.
     * Package-private so unit tests can call it directly.
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
                continue;
            }
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

    // ── Cell styling ─────────────────────────────────────────────────────

    /**
     * @param selected  true if this is the cursor cell (col × row intersection)
     * @param inRange   true if this cell is in the [checkIn, cursor) preview range
     */
    private static String styledCell(CellStatus cs, boolean ansi,
                                      boolean selected, boolean inRange) {
        if (!ansi) {
            String g;
            switch (cs) {
                case AVAILABLE:   g = FB_AVAILABLE;   break;
                case CONFIRMED:   g = FB_CONFIRMED;   break;
                case CHECKED_IN:  g = FB_CHECKED_IN;  break;
                case CHECKED_OUT: g = FB_CHECKED_OUT; break;
                case MAINTENANCE: g = FB_MAINTENANCE; break;
                default:          g = "??";
            }
            if (selected)  return "[" + g.charAt(0) + "]";
            if (inRange)   return "<" + g + ">";
            return " " + g + " ";
        }

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
        // cursor cell: invert + colour
        if (selected) return INVERT + colour + " " + glyph + " " + RESET;
        // range preview (between check-in and cursor on selected row): yellow tint + underline
        if (inRange)  return YELLOW + UNDERLINE + " " + glyph + " " + RESET;
        return colour + " " + glyph + " " + RESET;
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
