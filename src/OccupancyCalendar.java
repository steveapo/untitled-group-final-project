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
 *   <li><b>VIEW mode</b> ({@link #show}): read-only, navigation only, ESC exits.
 *   <li><b>PICK mode</b> ({@link #pickDates}): booking date+room selector for users.
 * </ul>
 *
 * <p>Alignment contract: every day column is exactly {@value #COL_W} visible chars.
 * All ANSI escape codes are applied AFTER padding so they contribute zero visible width.
 */
public class OccupancyCalendar {

    private static final DateTimeFormatter FMT     = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FMT_OUT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    // ── Cell status ──────────────────────────────────────────────────────

    enum CellStatus { AVAILABLE, CONFIRMED, CHECKED_IN, CHECKED_OUT, MAINTENANCE }

    // ── Result types ───────────────────────────────────────────────────

    static final class BookingSelection {
        final Room      room;
        final LocalDate checkIn;
        final LocalDate checkOut;
        BookingSelection(Room room, LocalDate checkIn, LocalDate checkOut) {
            this.room = room; this.checkIn = checkIn; this.checkOut = checkOut;
        }
    }

    static final class MaintenanceSelection {
        final Room      room;
        final LocalDate from;
        final LocalDate to;
        MaintenanceSelection(Room room, LocalDate from, LocalDate to) {
            this.room = room; this.from = from; this.to = to;
        }
    }

    // ── ANSI codes ───────────────────────────────────────────────────────

    private static final String RESET     = "\033[0m";
    private static final String BOLD      = "\033[1m";
    private static final String UNDERLINE = "\033[4m";
    private static final String INVERT    = "\033[7m";

    // Foreground
    private static final String FG_GREEN  = "\033[32m";
    private static final String FG_RED    = "\033[31m";
    private static final String FG_YELLOW = "\033[33m";
    private static final String FG_GRAY   = "\033[90m";   // bright black = dark gray
    private static final String FG_PINK   = "\033[95m";   // bright magenta ≈ pink

    private static boolean isAnsi() {
        if (System.getenv("NO_COLOR") != null) return false;
        if ("dumb".equals(System.getenv("TERM"))) return false;
        return !CLI.bold("X").equals("X");
    }

    // Wrap text in an ANSI code only if ANSI is supported.
    // IMPORTANT: `text` must already be padded to its final visible width before calling this.
    private static String a(String code, String text) {
        return isAnsi() ? code + text + RESET : text;
    }

    // ── Glyphs (each exactly 2 visible chars) ────────────────────────────

    private static final String GLYPH_AVAILABLE   = "░░";
    private static final String GLYPH_OCCUPIED    = "██";  // confirmed / checked-in / checked-out
    private static final String GLYPH_MAINTENANCE = "▒▒";

    private static final String FB_AVAILABLE   = "..";
    private static final String FB_OCCUPIED    = "##";
    private static final String FB_MAINTENANCE = "MM";
    private static final String FB_RANGE       = "~~";

    // ── Layout constants ─────────────────────────────────────────────────

    // Each day column: COL_W visible chars between │ separators.
    // Cell content:  " " + glyph(2) + " " = 4 visible chars  → COL_W = 4
    private static final int    COL_W     = 4;
    private static final String DASH4     = "────";   // exactly COL_W '─'

    // Row-label visible width.  Format (see buildLabel):
    //   2(margin) + 4(room#) + 2(gap) + 3(type) + 2(gap) + 7(price) + 2(gap) + 3(cap) = 25
    private static final int    LABEL_W   = 25;

    // Box-drawing characters
    private static final String V   = "│";
    private static final String TL  = "┌"; private static final String TR  = "┐";
    private static final String BL  = "└"; private static final String BR  = "┘";
    private static final String TD  = "┬"; private static final String TU  = "┴";
    private static final String CR  = "┼";
    private static final String HL  = "├"; private static final String HR  = "┤";

    // ── Public entry points ───────────────────────────────────────────────

    /** VIEW mode — read-only, all rooms passed in, ESC exits.
     *  Staff can press M to enter maintenance marking mode. */
    public static void show(Scanner scanner,
                            Vector<Room> rooms,
                            Vector<Bookings> bookings,
                            boolean isStaff) {
        show(scanner, rooms, bookings, isStaff, null);
    }

    /**
     * VIEW mode with optional maintenance marking.
     * @param file  if non-null, staff can press M to mark maintenance ranges
     */
    public static void show(Scanner scanner,
                            Vector<Room> rooms,
                            Vector<Bookings> bookings,
                            boolean isStaff,
                            Files file) {
        LocalDate weekStart    = mondayOf(LocalDate.now());
        int       colCursor    = LocalDate.now().getDayOfWeek().getValue() - 1;
        int       rowCursor    = 0;
        // Maintenance marking state (staff only)
        LocalDate maintStart   = null;
        int       maintRow     = -1;
        String    statusMsg    = null;

        while (true) {
            CLI.clearScreen();
            CLI.printBanner("OCCUPANCY CALENDAR");
            renderGrid(rooms, bookings, weekStart, colCursor, rowCursor,
                       maintStart, isStaff, false, maintRow);
            if (statusMsg != null) {
                System.out.println("  " + statusMsg);
                statusMsg = null;
            }
            String key = CLI.readArrowOrKey(scanner);
            switch (key) {
                case "LEFT":
                    if (colCursor > 0) colCursor--;
                    else { weekStart = weekStart.minusWeeks(1); colCursor = 6; }
                    break;
                case "RIGHT":
                    if (colCursor < 6) colCursor++;
                    else { weekStart = weekStart.plusWeeks(1); colCursor = 0; }
                    break;
                case "SHIFT_LEFT":  weekStart = weekStart.minusWeeks(1); break;
                case "SHIFT_RIGHT": weekStart = weekStart.plusWeeks(1);  break;
                case "UP":    if (rowCursor > 0) rowCursor--;                  break;
                case "DOWN":  if (rowCursor < rooms.size() - 1) rowCursor++;   break;
                case "T":
                    weekStart = mondayOf(LocalDate.now());
                    colCursor = LocalDate.now().getDayOfWeek().getValue() - 1;
                    break;
                case "M":
                    if (!isStaff || file == null) break;
                    LocalDate curDay = weekStart.plusDays(colCursor);
                    Room curRoom = rooms.get(rowCursor);
                    if (maintStart == null) {
                        // First M press: mark start
                        maintStart = curDay;
                        maintRow = rowCursor;
                        statusMsg = CLI.magenta("Maintenance start: "
                                + curDay.format(DateTimeFormatter.ofPattern("EEE MMM d"))
                                + " on " + curRoom.getRoomNumber()
                                + "  — press M again for end date, Esc to cancel");
                    } else {
                        // Second M press: mark end (must be same room, on or after start)
                        if (rowCursor != maintRow) {
                            statusMsg = CLI.warning("Must select end date on the same room ("
                                    + rooms.get(maintRow).getRoomNumber() + ")");
                            break;
                        }
                        LocalDate from, to;
                        if (curDay.isBefore(maintStart)) {
                            from = curDay; to = maintStart;
                        } else {
                            from = maintStart; to = curDay;
                        }
                        // to is inclusive, but bookings use exclusive end — add 1 day
                        LocalDate toExclusive = to.plusDays(1);
                        Room targetRoom = rooms.get(maintRow);
                        String fromStr = from.format(FMT_OUT);
                        String toStr   = toExclusive.format(FMT_OUT);
                        bookings.add(new Bookings(targetRoom, fromStr, toStr, "SYSTEM", "MAINTENANCE"));
                        file.updateBookings(bookings);
                        statusMsg = CLI.success("Maintenance set: " + targetRoom.getRoomNumber()
                                + " from " + from.format(DateTimeFormatter.ofPattern("MMM d"))
                                + " to " + to.format(DateTimeFormatter.ofPattern("MMM d")));
                        maintStart = null;
                        maintRow = -1;
                    }
                    break;
                case "ESC":
                    if (maintStart != null) {
                        maintStart = null;
                        maintRow = -1;
                        statusMsg = CLI.dim("Maintenance marking cancelled.");
                    } else {
                        return;
                    }
                    break;
                default: break;
            }
        }
    }

    /**
     * PICK mode — date+room picker for the booking flow.
     * Shows only rooms with capacity &ge; minCapacity that are not in maintenance.
     * Returns a {@link BookingSelection} or {@code null} on cancel.
     */
    public static BookingSelection pickDates(Scanner scanner,
                                             Vector<Room> allRooms,
                                             Vector<Bookings> bookings,
                                             int minCapacity) {
        Vector<Room> rooms = new Vector<>();
        for (Room r : allRooms) {
            if (r.getCapacity() >= minCapacity) rooms.add(r);
        }
        if (rooms.isEmpty()) return null;

        LocalDate weekStart = mondayOf(LocalDate.now());
        int colCursor = LocalDate.now().getDayOfWeek().getValue() - 1;
        int rowCursor = 0;
        LocalDate checkIn = null;

        while (true) {
            CLI.clearScreen();
            CLI.printBanner("BOOK A ROOM");
            renderGrid(rooms, bookings, weekStart, colCursor, rowCursor,
                       checkIn, false, true);
            String key = CLI.readArrowOrKey(scanner);
            switch (key) {
                case "LEFT":
                    if (colCursor > 0) colCursor--;
                    else { weekStart = weekStart.minusWeeks(1); colCursor = 6; }
                    break;
                case "RIGHT":
                    if (colCursor < 6) colCursor++;
                    else { weekStart = weekStart.plusWeeks(1); colCursor = 0; }
                    break;
                case "SHIFT_LEFT":  weekStart = weekStart.minusWeeks(1); break;
                case "SHIFT_RIGHT": weekStart = weekStart.plusWeeks(1);  break;
                case "UP":   if (rowCursor > 0) rowCursor--;                  break;
                case "DOWN": if (rowCursor < rooms.size() - 1) rowCursor++;   break;
                case "T":
                    weekStart = mondayOf(LocalDate.now());
                    colCursor = LocalDate.now().getDayOfWeek().getValue() - 1;
                    break;
                case "ENTER": {
                    Room cur = rooms.get(rowCursor);
                    LocalDate curDay = weekStart.plusDays(colCursor);
                    if (checkIn == null) {
                        if (cellFor(cur, curDay, bookings) == CellStatus.AVAILABLE
                                && !curDay.isBefore(LocalDate.now())) {
                            checkIn = curDay;
                        }
                    } else {
                        if (curDay.isAfter(checkIn)
                                && allAvailable(cur, checkIn, curDay, bookings)) {
                            return new BookingSelection(cur, checkIn, curDay);
                        }
                    }
                    break;
                }
                case "ESC":
                    if (checkIn != null) { checkIn = null; }
                    else return null;
                    break;
                default: break;
            }
        }
    }

    // ── Renderer ─────────────────────────────────────────────────────────

    /** Render overload without maintenance row (backward compat). */
    private static void renderGrid(Vector<Room> rooms,
                                   Vector<Bookings> bookings,
                                   LocalDate weekStart,
                                   int colCursor,
                                   int rowCursor,
                                   LocalDate checkIn,
                                   boolean isStaff,
                                   boolean pickMode) {
        renderGrid(rooms, bookings, weekStart, colCursor, rowCursor,
                   checkIn, isStaff, pickMode, -1);
    }

    /**
     * Render the 7-day grid.  All alignment is computed on plain strings first;
     * ANSI codes are applied only after the string reaches its final visible width.
     *
     * @param weekStart  Monday of the displayed week
     * @param colCursor  0-based column index within the week (0=Mon…6=Sun)
     * @param rowCursor  0-based room row index
     * @param checkIn    locked check-in / maintenance start date (null otherwise)
     * @param isStaff    true for reception/manager colour scheme
     * @param pickMode   true when used for booking selection
     * @param maintRow   row index locked for maintenance marking (-1 if none)
     */
    private static void renderGrid(Vector<Room> rooms,
                                   Vector<Bookings> bookings,
                                   LocalDate weekStart,
                                   int colCursor,
                                   int rowCursor,
                                   LocalDate checkIn,
                                   boolean isStaff,
                                   boolean pickMode,
                                   int maintRow) {
        boolean ansi      = isAnsi();
        LocalDate today   = LocalDate.now();
        LocalDate selDay  = weekStart.plusDays(colCursor);

        // ── Status line ───────────────────────────────────────────────────
        String weekLabel = weekStart.format(DateTimeFormatter.ofPattern("MMM d"))
                + " – " + weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        System.out.println();
        if (pickMode) {
            String step = checkIn == null
                    ? "Step 1: select check-in date  (Enter to confirm)"
                    : "Step 2: select check-out date (Enter to confirm)  "
                      + "Check-in: " + checkIn.format(DateTimeFormatter.ofPattern("EEE MMM d"));
            System.out.println("  " + CLI.dim(weekLabel) + "   " + CLI.cyan(step));
        } else if (maintRow >= 0 && checkIn != null) {
            System.out.println("  " + CLI.dim(weekLabel) + "   "
                    + CLI.magenta("Maintenance from: "
                        + checkIn.format(DateTimeFormatter.ofPattern("EEE MMM d"))
                        + "  →  cursor: "
                        + selDay.format(DateTimeFormatter.ofPattern("EEE MMM d"))));
        } else {
            System.out.println("  " + CLI.dim("Week: ") + CLI.bold(weekLabel)
                    + "   " + CLI.dim("Selected: ")
                    + CLI.cyan(selDay.format(DateTimeFormatter.ofPattern("EEE MMM d"))));
        }
        System.out.println();

        if (rooms.isEmpty()) {
            System.out.println("  No rooms available.  Press Esc to go back.");
            return;
        }

        // ── Separator lines ──────────────────────────────────────────────
        // Each is: LABEL_W spaces + corner + (DASH4 + separator) × 7
        // Total grid width = LABEL_W + 1(V) + (COL_W + 1(V)) × 7 = LABEL_W + 1 + 35 = LABEL_W+36
        String topLine = spaces(LABEL_W) + TL + repeat(DASH4 + TD, 6) + DASH4 + TR;
        String midLine = spaces(LABEL_W) + HL + repeat(DASH4 + CR, 6) + DASH4 + HR;
        String botLine = spaces(LABEL_W) + BL + repeat(DASH4 + TU, 6) + DASH4 + BR;

        // ── Header row ────────────────────────────────────────────────────
        // Two rows: day-of-week names, then day numbers.
        // Each column is exactly COL_W visible chars: " DOW" or " DD " (leading space, 3 chars).
        StringBuilder nameRow = new StringBuilder(spaces(LABEL_W)).append(V);
        StringBuilder numRow  = new StringBuilder(spaces(LABEL_W)).append(V);

        for (int i = 0; i < 7; i++) {
            LocalDate d       = weekStart.plusDays(i);
            boolean isSel     = (i == colCursor);
            boolean isToday   = d.equals(today);
            boolean isCheckIn = checkIn != null && d.equals(checkIn);

            // Build plain visible strings of exactly COL_W chars each
            String namePlain = " " + padRight(d.getDayOfWeek().toString().substring(0, 3), COL_W - 1);
            String numPlain  = " " + padLeft(String.valueOf(d.getDayOfMonth()), COL_W - 1);

            if (ansi) {
                if (isCheckIn) {
                    // locked check-in: green background
                    nameRow.append(a(FG_GREEN + BOLD, namePlain));
                    numRow .append(a(FG_GREEN + BOLD, numPlain));
                } else if (isSel) {
                    nameRow.append(a(INVERT + BOLD, namePlain));
                    numRow .append(a(INVERT + BOLD, numPlain));
                } else if (isToday) {
                    nameRow.append(a(BOLD + UNDERLINE, namePlain));
                    numRow .append(a(BOLD + UNDERLINE, numPlain));
                } else {
                    nameRow.append(namePlain);
                    numRow .append(numPlain);
                }
            } else {
                // Dumb terminal: surround selected with []
                if (isSel || isCheckIn) {
                    nameRow.append("[").append(d.getDayOfWeek().toString().substring(0, 2)).append("]");
                    numRow .append("[").append(padLeft(String.valueOf(d.getDayOfMonth()), 2)).append("]");
                } else {
                    nameRow.append(namePlain);
                    numRow .append(numPlain);
                }
            }
            nameRow.append(V);
            numRow.append(V);
        }

        System.out.println(nameRow);
        System.out.println(numRow);
        System.out.println(topLine);

        // ── Room rows ─────────────────────────────────────────────────────
        for (int r = 0; r < rooms.size(); r++) {
            Room room     = rooms.get(r);
            boolean isRow = (r == rowCursor);

            // Label: plain string of exactly LABEL_W visible chars, then styled
            String labelPlain = buildLabel(room);
            String label;
            if (ansi && isRow) {
                label = a(BOLD + INVERT, labelPlain);
            } else {
                label = labelPlain;
            }

            StringBuilder row = new StringBuilder(label).append(V);

            for (int i = 0; i < 7; i++) {
                LocalDate d       = weekStart.plusDays(i);
                boolean isCol     = (i == colCursor);
                boolean isCursor  = isRow && isCol;
                boolean inRange;
                boolean inMaintRange = false;
                if (maintRow >= 0 && r == maintRow && checkIn != null) {
                    // Maintenance marking mode: show pink range preview
                    LocalDate mFrom = checkIn.isBefore(selDay) ? checkIn : selDay;
                    LocalDate mTo   = checkIn.isAfter(selDay)  ? checkIn : selDay;
                    inMaintRange = !d.isBefore(mFrom) && !d.isAfter(mTo);
                    inRange = false;
                } else {
                    inRange = isRow && checkIn != null
                              && !d.isBefore(checkIn) && d.isBefore(selDay);
                }
                CellStatus cs     = cellFor(room, d, bookings);
                row.append(styledCell(cs, ansi, isCursor, inRange, inMaintRange, isStaff, pickMode)).append(V);
            }

            System.out.println(row);
            if (r < rooms.size() - 1) {
                System.out.println(isRow && ansi ? a(BOLD, midLine) : midLine);
            }
        }

        System.out.println(botLine);

        // ── Legend ────────────────────────────────────────────────────────
        System.out.println();
        if (ansi) {
            if (isStaff) {
                // Staff: gray=available, green=booked, pink=maintenance
                System.out.println("  Legend:  "
                        + a(FG_GRAY,  GLYPH_AVAILABLE)   + " Available   "
                        + a(FG_GREEN, GLYPH_OCCUPIED)     + " Booked      "
                        + a(FG_PINK,  GLYPH_MAINTENANCE)  + " Maintenance");
            } else if (pickMode) {
                // Pick mode (user booking): green=available, red=unavailable
                System.out.println("  Legend:  "
                        + a(FG_GREEN,  GLYPH_AVAILABLE)  + " Available   "
                        + a(FG_RED,    GLYPH_OCCUPIED)   + " Unavailable   "
                        + a(FG_YELLOW + UNDERLINE, GLYPH_AVAILABLE) + " Selected range");
            } else {
                // User view mode: green=available, red=unavailable
                System.out.println("  Legend:  "
                        + a(FG_GREEN, GLYPH_AVAILABLE)  + " Available   "
                        + a(FG_RED,   GLYPH_OCCUPIED)   + " Unavailable");
            }
        } else {
            System.out.println("  Legend:  .. Available  ## Booked/Unavailable  MM Maintenance");
        }

        // ── Footer ────────────────────────────────────────────────────────
        System.out.println();
        if (pickMode) {
            String escLabel = checkIn != null ? "Clear check-in" : "Cancel";
            System.out.println("  \u2190\u2192 Day  Shift+\u2190\u2192 Week  \u2191\u2193 Room  Enter Select  Esc " + escLabel);
        } else if (maintRow >= 0) {
            System.out.println("  \u2190\u2192 Day  Shift+\u2190\u2192 Week  "
                    + CLI.magenta("M End maintenance")
                    + "  Esc Cancel");
        } else if (isStaff && !pickMode) {
            System.out.println("  \u2190\u2192 Day  Shift+\u2190\u2192 Week  \u2191\u2193 Room  T Today  "
                    + CLI.magenta("M Maintenance")
                    + "  Esc Back");
        } else {
            System.out.println("  \u2190\u2192 Day  Shift+\u2190\u2192 Week  \u2191\u2193 Room  T Today  Esc Back");
        }
        System.out.println("  " + CLI.dim("(vim: h l day  H L week  k j room  enter  t today  e back)"));
    }

    // ── Row label ─────────────────────────────────────────────────────────

    /**
     * Build a plain (no ANSI) label of exactly {@link #LABEL_W} visible chars.
     * Format: 2-space margin + room# + 2-gap + type(3) + 2-gap + price + 2-gap + cap
     */
    private static String buildLabel(Room room) {
        String rn    = padRight(room.getRoomNumber(), 4);
        String type  = padRight(room.getType().length() > 3
                            ? room.getType().substring(0, 3) : room.getType(), 3);
        String price = padRight("$" + (int) room.getPrice() + "/n", 7);
        String cap   = room.getCapacity() + "p";
        // visible: 2 + 4 + 2 + 3 + 2 + 7 + 2 + len(cap) → pad to LABEL_W
        String inner = "  " + rn + "  " + type + "  " + price + "  " + cap;
        return padRight(inner, LABEL_W);
    }

    // ── Cell computation ─────────────────────────────────────────────────

    static CellStatus cellFor(Room room, LocalDate date, Vector<Bookings> bookings) {
        if ("MAINTENANCE".equalsIgnoreCase(room.getStatus())) return CellStatus.MAINTENANCE;
        for (Bookings b : bookings) {
            if (!b.getRoom().getRoomNumber().equals(room.getRoomNumber())) continue;
            if ("CANCELLED".equalsIgnoreCase(b.getStatus())) continue;
            LocalDate ci, co;
            try {
                ci = LocalDate.parse(b.getCheckIn(),  FMT);
                co = LocalDate.parse(b.getCheckOut(), FMT);
            } catch (DateTimeParseException e) { continue; }
            if (!date.isBefore(ci) && date.isBefore(co)) {
                switch (b.getStatus().toUpperCase()) {
                    case "CONFIRMED":   return CellStatus.CONFIRMED;
                    case "CHECKED_IN":  return CellStatus.CHECKED_IN;
                    case "CHECKED_OUT": return CellStatus.CHECKED_OUT;
                    case "MAINTENANCE": return CellStatus.MAINTENANCE;
                    default:            return CellStatus.CONFIRMED;
                }
            }
        }
        return CellStatus.AVAILABLE;
    }

    private static boolean allAvailable(Room room, LocalDate from, LocalDate to,
                                         Vector<Bookings> bookings) {
        for (LocalDate d = from; d.isBefore(to); d = d.plusDays(1)) {
            if (cellFor(room, d, bookings) != CellStatus.AVAILABLE) return false;
        }
        return true;
    }

    // ── Cell styling ─────────────────────────────────────────────────────

    /**
     * Returns a string of exactly COL_W visible chars: " glyph ".
     * ANSI codes wrap the whole unit after padding.
     */
    private static String styledCell(CellStatus cs, boolean ansi,
                                      boolean cursor, boolean inRange,
                                      boolean inMaintRange,
                                      boolean isStaff, boolean pickMode) {
        if (!ansi) {
            // dumb terminal
            String fb;
            if (cursor)  { fb = pickMode ? FB_RANGE : FB_AVAILABLE; }
            else if (inMaintRange) { fb = FB_MAINTENANCE; }
            else if (inRange) { fb = FB_RANGE; }
            else switch (cs) {
                case AVAILABLE:   fb = FB_AVAILABLE;   break;
                case MAINTENANCE: fb = FB_MAINTENANCE; break;
                default:          fb = FB_OCCUPIED;
            }
            // plain cell: space + 2 chars + space = 4
            String cell = " " + fb + " ";
            if (cursor) return "[" + fb + "]";  // still COL_W = 4
            return cell;
        }

        // ANSI: pick colour and glyph based on scheme
        if (cursor) {
            // cursor cell: solid glyph with colour (no invert, just bold colour)
            String solidGlyph = statusSolidGlyph(cs, isStaff, pickMode);
            // Return the colored solid glyph with spaces (no INVERT needed)
            return " " + solidGlyph + " ";
        }
        if (inMaintRange) {
            // maintenance range preview: pink underline
            return a(FG_PINK + UNDERLINE, " " + GLYPH_MAINTENANCE + " ");
        }
        if (inRange) {
            // range preview: yellow underline on available glyph
            return a(FG_YELLOW + UNDERLINE, " " + GLYPH_AVAILABLE + " ");
        }
        return " " + statusGlyph(cs, isStaff, pickMode) + " ";
    }

    /**
     * Returns a 2-char ANSI-coloured glyph string for the given status and colour scheme.
     * The 2 glyph chars are the visible content; colour codes wrap only those chars.
     * Uses dotted for available, solid for unavailable.
     */
    private static String statusGlyph(CellStatus cs, boolean isStaff, boolean pickMode) {
        if (isStaff) {
            // Staff scheme: green=available (dotted), green=any booking (solid), pink=maintenance (solid)
            switch (cs) {
                case AVAILABLE:   return a(FG_GREEN, GLYPH_AVAILABLE);  // dotted available
                case MAINTENANCE: return a(FG_PINK,  GLYPH_MAINTENANCE);  // solid maintenance
                default:          return a(FG_GREEN, GLYPH_OCCUPIED);  // solid booking
            }
        } else {
            // User scheme: green=available (dotted), red=unavailable (dotted in normal view)
            if (cs == CellStatus.AVAILABLE) return a(FG_GREEN, GLYPH_AVAILABLE);  // dotted available
            return a(FG_RED, GLYPH_AVAILABLE);  // dotted unavailable in normal view
        }
    }

    /**
     * Returns a 2-char solid ANSI-coloured glyph for cursor mode.
     * When selected, unavailable cells show as solid red, available as solid green.
     */
    private static String statusSolidGlyph(CellStatus cs, boolean isStaff, boolean pickMode) {
        if (isStaff) {
            // Staff scheme: all solid in cursor mode
            switch (cs) {
                case AVAILABLE:   return a(FG_GREEN, GLYPH_OCCUPIED);  // solid available
                case MAINTENANCE: return a(FG_PINK,  GLYPH_MAINTENANCE);  // solid maintenance
                default:          return a(FG_GREEN, GLYPH_OCCUPIED);  // solid booking
            }
        } else {
            // User scheme: green=available (solid), red=unavailable (solid)
            if (cs == CellStatus.AVAILABLE) return a(FG_GREEN, GLYPH_OCCUPIED);  // solid available
            return a(FG_RED, GLYPH_OCCUPIED);  // solid unavailable
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    /** Returns the Monday of the week containing {@code date}. */
    private static LocalDate mondayOf(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    private static String padRight(String s, int w) {
        if (s == null) s = "";
        if (s.length() >= w) return s.substring(0, w);
        return s + spaces(w - s.length());
    }

    private static String padLeft(String s, int w) {
        if (s == null) s = "";
        if (s.length() >= w) return s.substring(0, w);
        return spaces(w - s.length()) + s;
    }

    private static String spaces(int n) {
        return n <= 0 ? "" : " ".repeat(n);
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }
}
