# Occupancy Calendar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an interactive 7-day ASCII occupancy grid to the hotel booking CLI, reachable from Reception, Manager, and User menus.

**Architecture:** One new class `OccupancyCalendar` with a public `show(...)` entry point, pure `render(...)` output, pure `cellFor(...)` status-lookup, and a raw-mode `handleKeys(...)` loop. One new primitive in `CLI`: `readArrowOrKey(Scanner)` that returns `LEFT/RIGHT/SHIFT_LEFT/SHIFT_RIGHT/T/ESC` via JLine's `NonBlockingReader`. Menu entries added in the three user-facing menus (USER view passes an anonymisation flag).

**Tech Stack:** Java 22 (source release), JLine 3.26.3 (raw-mode terminal), JUnit 5 (tests), Maven (build).

**Design doc:** [docs/superpowers/specs/2026-04-16-occupancy-calendar-design.md](../specs/2026-04-16-occupancy-calendar-design.md)

---

## File Structure

| File | Responsibility | New or Modified |
|------|----------------|-----------------|
| `src/OccupancyCalendar.java` | Public `show()`; `CellStatus` enum; `cellFor()`, `render()`, `handleKeys()` private helpers | **new** |
| `src/CLI.java` | Add `readArrowOrKey(Scanner)` — JLine raw-mode reader returning `LEFT/RIGHT/SHIFT_LEFT/SHIFT_RIGHT/T/ESC` | modified |
| `src/ReceptionMenu.java` | Add item #10 "Occupancy calendar" — calls `OccupancyCalendar.show(..., true)` | modified |
| `src/ManagerMenu.java` | Add item #5 "Occupancy calendar" — calls `OccupancyCalendar.show(..., true)` | modified |
| `src/UserMenu.java` | Add item #5 "Occupancy calendar" — calls `OccupancyCalendar.show(..., false)` | modified |
| `test/OccupancyCalendarTest.java` | JUnit 5 unit tests for `cellFor()` | **new** |

**Why this split:** `cellFor` is the only pure, deterministic piece worth testing. `render` is I/O. `handleKeys` is interactive. Keeping them as separate `private static` methods on one class isolates the logic so the test can exercise it via a package-private accessor without exposing internals to callers.

---

## Task 1: Add `readArrowOrKey` primitive to `CLI`

**Files:**
- Modify: `src/CLI.java` (append a new public static method near the existing `readChoice`)

- [ ] **Step 1: Open `src/CLI.java` and locate the end of `readChoice`** (around line 360, just before the comment `// ── Raw-mode line reader with ESC support ───`).

- [ ] **Step 2: Add the new method immediately after `readChoice`'s closing brace.**

Insert this code:

```java
    // ── Arrow-key / hotkey reader ───────────────────────────────────────
    /**
     * Read a single navigation key: {@code LEFT}, {@code RIGHT},
     * {@code SHIFT_LEFT}, {@code SHIFT_RIGHT}, {@code T} (or {@code t}),
     * or {@code ESC}. Other keys are ignored — the method keeps waiting.
     *
     * <p>In raw mode (JLine) this consumes the full CSI escape sequence
     * for arrow keys. In the Scanner fallback it accepts vim-style letters:
     * {@code h}/{@code l} for LEFT/RIGHT, {@code H}/{@code L} for the Shift
     * variants, {@code t}/{@code T} for today, empty line or {@code e} for
     * ESC.
     *
     * @param fallbackScanner scanner used when raw mode is unavailable
     * @return one of {@code "LEFT"}, {@code "RIGHT"}, {@code "SHIFT_LEFT"},
     *         {@code "SHIFT_RIGHT"}, {@code "T"}, or {@code "ESC"}
     */
    public static String readArrowOrKey(Scanner fallbackScanner) {
        String result = withRawMode(() -> {
            NonBlockingReader reader = TERMINAL.reader();
            while (true) {
                int ch = reader.read();
                if (ch == 27) { // ESC
                    int next = reader.read(50L);
                    if (next == -2) return "ESC"; // standalone
                    if (next == '[') {
                        int third = reader.read(50L);
                        if (third == 'D') return "LEFT";
                        if (third == 'C') return "RIGHT";
                        // Shift-modified arrow: ESC [ 1 ; 2 D/C
                        if (third == '1') {
                            int semi = reader.read(50L); // expect ';'
                            int mod  = reader.read(50L); // expect '2'
                            int dir  = reader.read(50L); // 'D' or 'C'
                            if (semi == ';' && mod == '2') {
                                if (dir == 'D') return "SHIFT_LEFT";
                                if (dir == 'C') return "SHIFT_RIGHT";
                            }
                        }
                        // drain any unknown CSI tail and keep waiting
                        while (reader.read(10L) >= 0) { /* ignore */ }
                    } else {
                        while (reader.read(10L) >= 0) { /* ignore */ }
                    }
                    continue;
                }
                if (ch == 'T' || ch == 't') return "T";
                // ignore other keys
            }
        }, null);
        if (result != null) return result;

        // Scanner fallback
        String line = fallbackScanner.nextLine().trim();
        if (line.isEmpty() || line.equalsIgnoreCase("e")) return "ESC";
        switch (line) {
            case "h": return "LEFT";
            case "l": return "RIGHT";
            case "H": return "SHIFT_LEFT";
            case "L": return "SHIFT_RIGHT";
            case "t": case "T": return "T";
            default:  return "ESC";
        }
    }
```

- [ ] **Step 3: Compile to verify the method parses cleanly.**

Run: `mvn compile -q`
Expected: exits 0, no output.

- [ ] **Step 4: Commit.**

```bash
git add src/CLI.java
git commit -m "feat(cli): add readArrowOrKey primitive for arrow + Shift+arrow + T + ESC"
```

---

## Task 2: Create skeleton `OccupancyCalendar` class with `CellStatus` enum

**Files:**
- Create: `src/OccupancyCalendar.java`

- [ ] **Step 1: Create the file with the enum and public entry point skeleton.**

Write to `src/OccupancyCalendar.java`:

```java
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.Vector;

/**
 * Interactive 7-day ASCII occupancy grid.
 *
 * <p>Rooms are rows, days are columns. Cells are colour-coded by status
 * (available / confirmed / checked-in / checked-out / maintenance).
 * Horizontal arrow keys shift the window by one day; Shift+arrows by a
 * week; {@code T} snaps back to today; {@code Esc} exits. Read-only.
 */
public class OccupancyCalendar {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final int WINDOW_DAYS = 7;

    /** Derived status of a single room on a single day. */
    enum CellStatus { AVAILABLE, CONFIRMED, CHECKED_IN, CHECKED_OUT, MAINTENANCE }

    /**
     * Render the interactive calendar until the user presses ESC.
     *
     * @param scanner          fallback scanner for non-raw input modes
     * @param rooms            in-memory rooms vector (not mutated)
     * @param bookings         in-memory bookings vector (not mutated)
     * @param showGuestNames   forward-compat flag; currently unused by cells
     *                         but gates future guest-detail surfaces
     */
    public static void show(Scanner scanner,
                            Vector<Room> rooms,
                            Vector<Bookings> bookings,
                            boolean showGuestNames) {
        // handleKeys implementation lands in Task 5
        throw new UnsupportedOperationException("not yet implemented");
    }
}
```

- [ ] **Step 2: Compile to verify the file is valid Java and integrates with the project.**

Run: `mvn compile -q`
Expected: exits 0, no output.

- [ ] **Step 3: Commit.**

```bash
git add src/OccupancyCalendar.java
git commit -m "feat(calendar): add OccupancyCalendar skeleton with CellStatus enum"
```

---

## Task 3: Implement `cellFor()` using TDD — test first

**Files:**
- Create: `test/OccupancyCalendarTest.java`

- [ ] **Step 1: Write the failing test.**

Create `test/OccupancyCalendarTest.java`:

```java
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OccupancyCalendar — cellFor status derivation")
class OccupancyCalendarTest {

    private Room room(String number, String status) {
        return new Room(number, 2, 100.0, "Single", status);
    }

    private Bookings booking(Room r, String in, String out, String who, String status) {
        return new Bookings(r, in, out, who, status);
    }

    @Test
    @DisplayName("room with no bookings → AVAILABLE")
    void availableWhenNoBookings() {
        Room r = room("R101", "AVAILABLE");
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 1), new Vector<>()));
    }

    @Test
    @DisplayName("CONFIRMED booking spanning the day → CONFIRMED")
    void confirmedSpansDay() {
        Room r = room("R101", "AVAILABLE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r, "01-05-2026", "05-05-2026", "alice", "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.CONFIRMED,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 2), b));
    }

    @Test
    @DisplayName("CHECKED_IN booking → CHECKED_IN")
    void checkedIn() {
        Room r = room("R101", "AVAILABLE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r, "01-05-2026", "05-05-2026", "alice", "CHECKED_IN"));
        assertEquals(OccupancyCalendar.CellStatus.CHECKED_IN,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 2), b));
    }

    @Test
    @DisplayName("CHECKED_OUT booking → CHECKED_OUT")
    void checkedOut() {
        Room r = room("R101", "AVAILABLE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r, "01-05-2026", "05-05-2026", "alice", "CHECKED_OUT"));
        assertEquals(OccupancyCalendar.CellStatus.CHECKED_OUT,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 2), b));
    }

    @Test
    @DisplayName("CANCELLED booking is ignored → AVAILABLE")
    void cancelledIgnored() {
        Room r = room("R101", "AVAILABLE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r, "01-05-2026", "05-05-2026", "alice", "CANCELLED"));
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 2), b));
    }

    @Test
    @DisplayName("room MAINTENANCE overrides bookings")
    void maintenanceOverrides() {
        Room r = room("R101", "MAINTENANCE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r, "01-05-2026", "05-05-2026", "alice", "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.MAINTENANCE,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 2), b));
    }

    @Test
    @DisplayName("checkout day is NOT occupied (exclusive end)")
    void checkoutExclusive() {
        Room r = room("R101", "AVAILABLE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r, "01-05-2026", "05-05-2026", "alice", "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 5), b));
    }

    @Test
    @DisplayName("check-in day IS occupied (inclusive start)")
    void checkinInclusive() {
        Room r = room("R101", "AVAILABLE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r, "01-05-2026", "05-05-2026", "alice", "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.CONFIRMED,
                OccupancyCalendar.cellForTest(r, LocalDate.of(2026, 5, 1), b));
    }

    @Test
    @DisplayName("booking for a different room does not affect this room")
    void otherRoomIgnored() {
        Room r101 = room("R101", "AVAILABLE");
        Room r102 = room("R102", "AVAILABLE");
        Vector<Bookings> b = new Vector<>();
        b.add(booking(r102, "01-05-2026", "05-05-2026", "alice", "CONFIRMED"));
        assertEquals(OccupancyCalendar.CellStatus.AVAILABLE,
                OccupancyCalendar.cellForTest(r101, LocalDate.of(2026, 5, 2), b));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails for the right reason.**

Run: `mvn test -Dtest=OccupancyCalendarTest -q 2>&1 | tail -15`
Expected: compilation failure — `cannot find symbol: method cellForTest(...)` (because we haven't written it yet).

- [ ] **Step 3: Implement `cellFor` and the package-private test accessor in `src/OccupancyCalendar.java`.**

Add these two methods to the class (keep everything already in the file):

```java
    /**
     * Decide what status applies to {@code room} on {@code date} given the
     * current bookings vector. {@code MAINTENANCE} on the room wins. Then the
     * first non-cancelled booking whose {@code [checkIn, checkOut)} range
     * contains {@code date} wins; its status is mapped to the matching enum.
     * Otherwise {@code AVAILABLE}.
     */
    private static CellStatus cellFor(Room room, LocalDate date, Vector<Bookings> bookings) {
        if ("MAINTENANCE".equals(room.getStatus())) return CellStatus.MAINTENANCE;

        for (Bookings b : bookings) {
            if (!b.getRoom().getRoomNumber().equals(room.getRoomNumber())) continue;
            if ("CANCELLED".equals(b.getStatus())) continue;

            LocalDate in, out;
            try {
                in  = LocalDate.parse(b.getCheckIn(),  DATE_FORMATTER);
                out = LocalDate.parse(b.getCheckOut(), DATE_FORMATTER);
            } catch (DateTimeParseException _) {
                continue; // malformed record — treat as not-on-this-day
            }

            // [in, out) — check-in inclusive, check-out exclusive
            if (!date.isBefore(in) && date.isBefore(out)) {
                switch (b.getStatus()) {
                    case "CHECKED_IN":  return CellStatus.CHECKED_IN;
                    case "CHECKED_OUT": return CellStatus.CHECKED_OUT;
                    default:            return CellStatus.CONFIRMED;
                }
            }
        }
        return CellStatus.AVAILABLE;
    }

    /** Package-private accessor for unit tests. Do not call from production code. */
    static CellStatus cellForTest(Room room, LocalDate date, Vector<Bookings> bookings) {
        return cellFor(room, date, bookings);
    }
```

- [ ] **Step 4: Run tests and confirm all nine pass.**

Run: `mvn test -Dtest=OccupancyCalendarTest -q 2>&1 | tail -10`
Expected: `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit.**

```bash
git add src/OccupancyCalendar.java test/OccupancyCalendarTest.java
git commit -m "feat(calendar): implement cellFor status derivation with unit tests"
```

---

## Task 4: Implement `render()` — the 7-day grid

**Files:**
- Modify: `src/OccupancyCalendar.java`

- [ ] **Step 1: Add the glyph + colour mapping and `render` method to `OccupancyCalendar`.**

Insert these methods inside the class (keep everything already there):

```java
    private static final String[] WEEKDAY_ABBREV =
            { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    /** Render the full screen: banner, header row, grid, legend, hotkey footer. */
    private static void render(Vector<Room> rooms, Vector<Bookings> bookings, LocalDate windowStart) {
        CLI.clearScreen();

        LocalDate today = LocalDate.now();
        String banner = "OCCUPANCY CALENDAR (week of " + formatBannerDate(windowStart) + ")";
        CLI.printBanner(banner);
        System.out.println();

        if (rooms.isEmpty()) {
            System.out.println(CLI.dim("  No rooms configured."));
            System.out.println();
            System.out.println(CLI.dim("  Esc Back"));
            return;
        }

        renderHeader(windowStart, today);
        renderBorder('┌', '┬', '┐');
        for (Room r : rooms) {
            renderRow(r, bookings, windowStart);
        }
        renderBorder('└', '┴', '┘');

        System.out.println();
        System.out.println("  " + CLI.dim("Legend:  ")
                + cellGlyph(CellStatus.AVAILABLE)   + " Available   "
                + cellGlyph(CellStatus.CONFIRMED)   + " Confirmed   "
                + cellGlyph(CellStatus.CHECKED_IN)  + " Checked-in");
        System.out.println("           "
                + cellGlyph(CellStatus.CHECKED_OUT) + " Checked-out "
                + cellGlyph(CellStatus.MAINTENANCE) + " Maintenance");
        System.out.println();
        System.out.println(CLI.dim("  ← → Day   Shift+← → Week   T Today   Esc Back"));
    }

    private static void renderHeader(LocalDate windowStart, LocalDate today) {
        StringBuilder names = new StringBuilder("         ");
        StringBuilder nums  = new StringBuilder("         ");
        for (int i = 0; i < WINDOW_DAYS; i++) {
            LocalDate d = windowStart.plusDays(i);
            String abbr = WEEKDAY_ABBREV[d.getDayOfWeek().getValue() - 1];
            String num  = String.format("%2d", d.getDayOfMonth());
            if (d.equals(today)) {
                names.append(CLI.bold("\033[4m" + abbr + "\033[24m")).append(" ");
                nums.append(CLI.bold("\033[4m" + num + " \033[24m")).append(" ");
            } else {
                names.append(abbr).append(" ");
                nums.append(num).append("  ");
            }
        }
        System.out.println(names.toString());
        System.out.println(nums.toString());
    }

    private static void renderBorder(char left, char mid, char right) {
        StringBuilder sb = new StringBuilder("        ");
        sb.append(left);
        for (int i = 0; i < WINDOW_DAYS; i++) {
            sb.append("───");
            sb.append(i < WINDOW_DAYS - 1 ? mid : right);
        }
        System.out.println(CLI.dim(sb.toString()));
    }

    private static void renderRow(Room room, Vector<Bookings> bookings, LocalDate windowStart) {
        StringBuilder sb = new StringBuilder("  ");
        sb.append(CLI.bold(String.format("%-6s", room.getRoomNumber())));
        sb.append(CLI.dim("│"));
        for (int i = 0; i < WINDOW_DAYS; i++) {
            LocalDate d = windowStart.plusDays(i);
            sb.append(cellGlyph(cellFor(room, d, bookings)));
            sb.append(" ");
            sb.append(CLI.dim("│"));
        }
        System.out.println(sb.toString());
    }

    /** Map a cell status to its 2-char coloured glyph. */
    private static String cellGlyph(CellStatus s) {
        switch (s) {
            case AVAILABLE:   return CLI.green("░░");
            case CONFIRMED:   return CLI.cyan("██");
            case CHECKED_IN:  return CLI.magenta("██");
            case CHECKED_OUT: return CLI.dim("▒▒");
            case MAINTENANCE: return CLI.red("▒▒");
            default:          return "░░";
        }
    }

    private static String formatBannerDate(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("MMM d"));
    }
```

- [ ] **Step 2: Verify it compiles.**

Run: `mvn compile -q`
Expected: exits 0.

- [ ] **Step 3: Run existing tests to ensure nothing regressed.**

Run: `mvn test -q 2>&1 | tail -10`
Expected: `Tests run: 84, Failures: 0, Errors: 0, Skipped: 0` (75 existing + 9 new from Task 3).

- [ ] **Step 4: Commit.**

```bash
git add src/OccupancyCalendar.java
git commit -m "feat(calendar): implement render() — 7-day ASCII grid with coloured cells"
```

---

## Task 5: Implement `handleKeys()` loop and wire up `show()`

**Files:**
- Modify: `src/OccupancyCalendar.java`

- [ ] **Step 1: Replace the `show()` body with the real implementation.**

Find this block in `src/OccupancyCalendar.java`:

```java
    public static void show(Scanner scanner,
                            Vector<Room> rooms,
                            Vector<Bookings> bookings,
                            boolean showGuestNames) {
        // handleKeys implementation lands in Task 5
        throw new UnsupportedOperationException("not yet implemented");
    }
```

Replace it with:

```java
    public static void show(Scanner scanner,
                            Vector<Room> rooms,
                            Vector<Bookings> bookings,
                            boolean showGuestNames) {
        LocalDate windowStart = LocalDate.now();
        while (true) {
            render(rooms, bookings, windowStart);
            String key = CLI.readArrowOrKey(scanner);
            switch (key) {
                case "LEFT":        windowStart = windowStart.minusDays(1); break;
                case "RIGHT":       windowStart = windowStart.plusDays(1);  break;
                case "SHIFT_LEFT":  windowStart = windowStart.minusDays(WINDOW_DAYS); break;
                case "SHIFT_RIGHT": windowStart = windowStart.plusDays(WINDOW_DAYS);  break;
                case "T":           windowStart = LocalDate.now(); break;
                case "ESC":         return;
                default:            /* ignore */
            }
        }
    }
```

Note: `showGuestNames` is intentionally unused inside this method for now — it's carried through the public signature so future additions (e.g. a selected-day details footer) can honour it without an API break.

- [ ] **Step 2: Verify it compiles.**

Run: `mvn compile -q`
Expected: exits 0.

- [ ] **Step 3: Run all tests to confirm no regression.**

Run: `mvn test -q 2>&1 | tail -10`
Expected: `Tests run: 84, Failures: 0, Errors: 0`.

- [ ] **Step 4: Commit.**

```bash
git add src/OccupancyCalendar.java
git commit -m "feat(calendar): wire up show() with arrow-key navigation loop"
```

---

## Task 6: Add "Occupancy calendar" to Reception menu

**Files:**
- Modify: `src/ReceptionMenu.java`

- [ ] **Step 1: Find the menu-render block in `show(...)`** (around lines 16–27 of `src/ReceptionMenu.java`).

- [ ] **Step 2: Add a new menu item "10" and its handler.**

**Change 1** — after the line `CLI.printMenuItem("9", "Mark room maintenance / available");` add:

```java
            CLI.printMenuItem("10", "Occupancy calendar");
```

Wait — `CLI.readChoice` returns single keypresses `1`–`9`, not two-digit `10`. Don't use `10`. Reorder instead: move "Occupancy calendar" to a lower slot by renumbering. Actually: keep all existing items and use a letter key `C` instead. `readChoice` only returns `1`-`9` or `ESC`, so we can't use `C`.

Cleanest path that doesn't touch `readChoice`: insert "Occupancy calendar" as item **#1** and push everything else down by one. The original item #9 becomes #10 which conflicts again.

Correct approach — promote the calendar to item #1 and collapse the existing 9 items into a sub-menu "Bookings & Rooms" as item #2. That's a much bigger change.

**Revised Step 2** — use the simplest approach that keeps `readChoice`'s 1-9 contract: extend `CLI.readChoice` to also return a letter key for the calendar.

- [ ] **Step 2 (revised): Add a `C` hotkey to `CLI.readChoice`.**

Open `src/CLI.java`. Find `readChoice`'s raw-mode block where it handles digit keys. It currently reads:

```java
                if (ch >= '1' && ch <= '9') return String.valueOf((char) ch);
                if (ch == 'e' || ch == 'q') return "ESC";
```

Add one line between those two:

```java
                if (ch == 'c' || ch == 'C') return "C";
```

And in the Scanner fallback at the end of `readChoice`, add before the final `return input;`:

```java
        if (input.equalsIgnoreCase("c")) return "C";
```

- [ ] **Step 3: Verify `CLI` still compiles.**

Run: `mvn compile -q`
Expected: exits 0.

- [ ] **Step 4: Now add the menu entry in `src/ReceptionMenu.java`.**

Find this block in `ReceptionMenu.show(...)`:

```java
            CLI.printMenuItem("9", "Mark room maintenance / available");
            CLI.printFooter("Logout");
            String choice = CLI.readChoice(scanner);
```

Replace with:

```java
            CLI.printMenuItem("9", "Mark room maintenance / available");
            CLI.printMenuItem("C", "Occupancy calendar");
            CLI.printFooter("Logout");
            String choice = CLI.readChoice(scanner);
```

Then in the switch statement, find:

```java
                case "9":  setRoomStatus(scanner, rooms, file);                                    break;
                case "ESC": return;
```

Insert a case before `"ESC"`:

```java
                case "9":  setRoomStatus(scanner, rooms, file);                                    break;
                case "C":  OccupancyCalendar.show(scanner, rooms, bookings, true);                 break;
                case "ESC": return;
```

- [ ] **Step 5: Compile and run all tests.**

Run: `mvn test -q 2>&1 | tail -10`
Expected: `Tests run: 84, Failures: 0, Errors: 0`.

- [ ] **Step 6: Commit.**

```bash
git add src/CLI.java src/ReceptionMenu.java
git commit -m "feat(calendar): add 'C' hotkey + Occupancy calendar entry in Reception menu"
```

---

## Task 7: Add "Occupancy calendar" to Manager menu

**Files:**
- Modify: `src/ManagerMenu.java`

- [ ] **Step 1: Find the main menu block in `show(...)`** (around lines 12–17). It currently shows items 1–4.

- [ ] **Step 2: Add the C entry and handler.**

Find:

```java
            CLI.printMenuItem("4", "View statistics");
            CLI.printFooter("Logout");
            String choice = CLI.readChoice(scanner);
```

Replace with:

```java
            CLI.printMenuItem("4", "View statistics");
            CLI.printMenuItem("C", "Occupancy calendar");
            CLI.printFooter("Logout");
            String choice = CLI.readChoice(scanner);
```

Find the switch body:

```java
                case "4": viewStats(bookings);       Main.pause(scanner);  break;
                case "ESC": return;
```

Replace with:

```java
                case "4": viewStats(bookings);       Main.pause(scanner);  break;
                case "C": OccupancyCalendar.show(scanner, rooms, bookings, true); break;
                case "ESC": return;
```

- [ ] **Step 3: Compile and run all tests.**

Run: `mvn test -q 2>&1 | tail -10`
Expected: `Tests run: 84, Failures: 0, Errors: 0`.

- [ ] **Step 4: Commit.**

```bash
git add src/ManagerMenu.java
git commit -m "feat(calendar): add Occupancy calendar entry in Manager menu"
```

---

## Task 8: Add "Occupancy calendar" to User menu (anonymised)

**Files:**
- Modify: `src/UserMenu.java`

- [ ] **Step 1: Open `src/UserMenu.java` and find the top-level menu block.** Look for the row of `CLI.printMenuItem("1", ...)` calls in `show(...)` (typically lines ~20-28).

- [ ] **Step 2: Add `C` entry just before `CLI.printFooter("Logout");`.**

Find the last `CLI.printMenuItem(...)` before the footer (item "4"), then insert after it:

```java
            CLI.printMenuItem("C", "Occupancy calendar");
```

- [ ] **Step 3: Add the switch case.** In the switch statement, find the line containing `case "ESC":` and insert above it:

```java
                case "C":  OccupancyCalendar.show(scanner, rooms, bookings, false); break;
```

(Note the `false` — this is the USER view.)

- [ ] **Step 4: Compile and run all tests.**

Run: `mvn test -q 2>&1 | tail -10`
Expected: `Tests run: 84, Failures: 0, Errors: 0`.

- [ ] **Step 5: Commit.**

```bash
git add src/UserMenu.java
git commit -m "feat(calendar): add Occupancy calendar entry in User menu (anonymised)"
```

---

## Task 9: Manual smoke test

**Files:** none (interactive verification)

- [ ] **Step 1: Build the distributable JAR.**

Run: `./build.sh`
Expected: ends with `Output: dist/HotelBooking.jar`.

- [ ] **Step 2: Launch the app.**

Run: `cd dist && java --enable-native-access=ALL-UNNAMED -jar HotelBooking.jar`

- [ ] **Step 3: Log in as manager (`admin` / `admin`) and press `C`.**

Expected: the occupancy calendar renders — banner with week-of date, 7 day-columns labelled Mon/Tue/…, one row per room, legend at bottom, hotkey footer.

- [ ] **Step 4: Verify navigation keys.**

- `→` advances window by one day (date in banner increments).
- `←` retreats by one day.
- `Shift+→` advances by seven days.
- `Shift+←` retreats by seven days.
- `T` snaps back to today.
- `Esc` returns to the Manager menu.

- [ ] **Step 5: Log out, register a USER, log in, press `C`.**

Expected: same calendar view renders (the anonymisation flag currently has no visible effect because cells carry no names — verify the view still works end-to-end).

- [ ] **Step 6: Exit the app and stop the smoke test.**

No commit here — nothing changed on disk.

---

## Self-Review (done by plan author before handoff)

**Spec coverage check:**
- 7-day window, starting today → Task 5 (`windowStart = LocalDate.now()`, `WINDOW_DAYS = 7`). ✓
- `←/→` day, `Shift+←/→` week, `T` today, `Esc` exit → Task 1 (primitive), Task 5 (dispatch). ✓
- Today highlighted in header → Task 4 (`renderHeader` bold+underline branch). ✓
- Colour-coded cells per status → Task 4 (`cellGlyph`). ✓
- CellStatus enum (AVAILABLE/CONFIRMED/CHECKED_IN/CHECKED_OUT/MAINTENANCE) → Task 2. ✓
- Reception + Manager + User menu entries → Tasks 6, 7, 8. ✓
- USER anonymisation flag → Task 8 passes `false`. Flag threaded through `show()` signature. ✓
- Cancelled bookings skipped → Task 3 test + `cellFor` implementation. ✓
- Checkout-day exclusive → Task 3 test + `cellFor`. ✓
- Maintenance overrides → Task 3 test + `cellFor`. ✓
- Malformed date → cell falls through to AVAILABLE → Task 3 `try/catch` in `cellFor`. ✓
- Empty rooms message → Task 4 `render` early return. ✓
- ANSI-off degrades gracefully → existing `CLI.ansi()` helpers already handle it; no extra work needed. ✓
- Unit tests for `cellFor` → Task 3 (9 tests covering all spec cases). ✓
- No tests for `render`/`handleKeys` (I/O and interactive) → consistent with existing project style. ✓

**Placeholder scan:** no TBD/TODO. Every code block is the actual code to paste. Every command has expected output. Task 6 had a false start re: item "10" — I kept the reasoning visible in the plan (as a teaching moment for the executor) and ended with the clean solution (add a `C` hotkey to `readChoice`).

**Type consistency:** `CellStatus` enum values used identically across Tasks 2, 3, 4. `readArrowOrKey` returns identical strings in Tasks 1 and 5. Method signatures match across all call sites.

**Gaps:** none identified.
