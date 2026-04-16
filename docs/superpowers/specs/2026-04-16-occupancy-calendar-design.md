# Occupancy Calendar — Design

**Date:** 2026-04-16
**Status:** Approved, ready for implementation plan
**Scope:** One feature. Single implementation cycle.

---

## Overview

Add a new menu entry — **"Occupancy calendar"** — visible in the Reception, Manager, and User menus. Renders a 7-day ASCII Gantt-style grid showing every room as a row and the next seven days as columns. Cells are colour-coded by status (available, confirmed, checked-in, checked-out, maintenance). The view is interactive: horizontal navigation with the arrow keys shifts the window, `Shift+←/→` jumps a week, `T` snaps back to today, and `Esc` exits. Today's column is always highlighted.

The USER view hides guest-specific information; Reception and Manager get the full view.

---

## Visual mock

```
╔══════════════════════════════════════════════════════════╗
║              OCCUPANCY CALENDAR (week of Apr 16)         ║
╚══════════════════════════════════════════════════════════╝

         Thu Fri Sat Sun Mon Tue Wed
         16  17  18  19  20  21  22
        ┌───┬───┬───┬───┬───┬───┬───┐
  R101  │██ │██ │██ │░░ │░░ │░░ │░░ │
  R102  │░░ │░░ │░░ │░░ │██ │██ │██ │
  R201  │▒▒ │▒▒ │▒▒ │▒▒ │▒▒ │▒▒ │▒▒ │
  R202  │░░ │░░ │██ │██ │██ │░░ │░░ │
        └───┴───┴───┴───┴───┴───┴───┘

  Legend:  ░░ Available   ██ Confirmed   ██ Checked-in
           ▒▒ Checked-out ▒▒ Maintenance

  ← → Day   Shift+← → Week   T Today   Esc Back
```

**Cell styling** (terminals with ANSI):

| State        | Glyph  | Colour   |
|--------------|--------|----------|
| AVAILABLE    | `░░`   | green    |
| CONFIRMED    | `██`   | cyan     |
| CHECKED_IN   | `██`   | magenta  |
| CHECKED_OUT  | `▒▒`   | dim      |
| MAINTENANCE  | `▒▒`   | red      |

**Today's column** is rendered bold + underlined in the header row regardless of where the window is positioned.

**"Dumb" terminals / no-ANSI fallback**: glyphs degrade to ASCII substitutes (`##`, `..`, `xx`) and colour is dropped. Arrow keys still work because they flow through JLine, which normalises input across terminal types.

---

## Architecture

One new class: `src/OccupancyCalendar.java`.

### Public entry point

```java
public static void show(Scanner scanner,
                        Vector<Room> rooms,
                        Vector<Bookings> bookings,
                        boolean showGuestNames)
```

Callers pass their already-loaded in-memory vectors. No file I/O. No writes. Pure read-only visualisation.

`showGuestNames` is a forward-looking flag that today controls nothing visible (cells carry no names), but gates any future enhancement that would surface guest identity (e.g. hovering a cell, footer details). Reception/Manager pass `true`; User passes `false`.

### Private internals

Three responsibilities, each a private static method:

1. **`render(rooms, bookings, windowStart)`** — prints banner, header row, cell grid, legend, hotkey footer. Pure function of inputs; no I/O other than `System.out`.
2. **`cellFor(room, date, bookings)`** — maps `(room, date)` → `CellStatus` enum. Centralises the "what's happening on this day" logic so `render` stays dumb.
3. **`handleKeys(...)`** — raw-mode key loop. Reads from a new `CLI.readArrowOrKey` primitive, mutates `windowStart`, re-renders on each change, exits on ESC.

### CellStatus enum (file-private)

```java
private enum CellStatus { AVAILABLE, CONFIRMED, CHECKED_IN, CHECKED_OUT, MAINTENANCE }
```

### New CLI primitive

`CLI.readChoice` returns `"1"`-`"9"` or `"ESC"` — no arrow keys. We need a new helper:

```java
public static String readArrowOrKey(Scanner fallbackScanner)
```

Returns one of: `"LEFT"`, `"RIGHT"`, `"SHIFT_LEFT"`, `"SHIFT_RIGHT"`, `"T"`, `"ESC"`. Other keys are ignored (keeps waiting). Same JLine `NonBlockingReader` + raw-mode pattern used by the existing readers.

Parsing rules for the escape sequence:
- `ESC` alone (no follow-up byte within 50 ms) → `"ESC"`
- `ESC [ D` → `"LEFT"`
- `ESC [ C` → `"RIGHT"`
- `ESC [ 1 ; 2 D` → `"SHIFT_LEFT"` (xterm modifier convention)
- `ESC [ 1 ; 2 C` → `"SHIFT_RIGHT"`
- plain `T`/`t` → `"T"`
- anything else → continue waiting

Scanner fallback (dumb terminals, piped stdin): accept a line like `h`/`l` (vim-style) for `LEFT`/`RIGHT`, `H`/`L` for shift variants, `t`/`T` for today, `e` / empty for ESC.

---

## Data flow

```
ReceptionMenu.show() ──┐
ManagerMenu.show()   ──┤─→ OccupancyCalendar.show(rooms, bookings, flag)
UserMenu.show()      ──┘       │
                               ├── windowStart = LocalDate.now()
                               └── loop:
                                     CLI.clearScreen()
                                     render(rooms, bookings, windowStart)
                                     key = CLI.readArrowOrKey()
                                     switch (key):
                                       LEFT        → windowStart -= 1 day
                                       RIGHT       → windowStart += 1 day
                                       SHIFT_LEFT  → windowStart -= 7 days
                                       SHIFT_RIGHT → windowStart += 7 days
                                       T           → windowStart = today
                                       ESC         → return
```

`cellFor(room, date, bookings)` decision order:
1. If `room.getStatus() == "MAINTENANCE"` → `MAINTENANCE` (overrides any booking).
2. For each booking matching the room:
   - Skip if `status == "CANCELLED"`.
   - If `date` is in `[checkIn, checkOut)` (checkout day exclusive, matching `DateInput.checkBookingsDate` convention) → return status-mapped enum (`CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`).
3. Otherwise → `AVAILABLE`.

---

## Error handling & edge cases

- **Empty `rooms`** → render "No rooms configured." in place of the grid. Only `Esc` is accepted.
- **Empty `bookings`** → grid renders all-available. Normal.
- **Maintenance room** → always red `▒▒`, overrides any booking on that room.
- **Cancelled bookings** → skipped entirely (match existing `checkBookingsDate` filter).
- **Overlapping bookings for same room (data corruption)** → last matching booking in the vector wins. Not worth special-casing.
- **Terminal narrower than ~40 cols** → render may wrap, but output stays readable. Not special-cased.
- **Date parse errors** on booking records → malformed booking is treated as not-on-this-day (cell falls through to AVAILABLE). A single bad record should not crash the view.

No writes, so no persistence errors.

---

## Testing

Follows the existing project style (JUnit 5, real objects, no mocking framework).

### `test/OccupancyCalendarTest.java`

Tests the pure `cellFor()` logic via a package-private accessor (`static CellStatus cellForTest(...)` or similar visibility trick). Cases:

1. Room has no bookings → `AVAILABLE`.
2. Booking CONFIRMED spans the day → `CONFIRMED`.
3. Booking CHECKED_IN spans the day → `CHECKED_IN`.
4. Booking CHECKED_OUT spans the day → `CHECKED_OUT`.
5. Booking CANCELLED spans the day → cell is `AVAILABLE`.
6. Room status MAINTENANCE with a booking on the same day → `MAINTENANCE` (override).
7. Date equal to the booking's check-out day → `AVAILABLE` (exclusive end).
8. Date equal to the booking's check-in day → matches booking status.

No tests for `render()` or `handleKeys()` — they are I/O and interactive, respectively, and the existing project consistently skips those (confirmed: `CLITest` only tests pure helpers like colour wrappers and `randomDelayMs`).

### Existing tests

All 75 existing tests continue to pass — the new class does not touch any shared state or existing methods.

---

## Scope boundaries

**In scope:**
- 7-day window, starting today by default.
- `←/→` shifts by one day; `Shift+←/→` by a week.
- `T` snaps back to today.
- `Esc` exits.
- Today's column is highlighted in the header.
- Colour-coded cells per status.
- Menu entries added in Reception, Manager, and User menus.
- USER view hides guest-related information (forward-compatible flag).
- Unit tests for `cellFor()`.

**Explicitly out of scope** (easy to add later):
- Per-cell cursor / Enter-to-drill-in.
- Room filtering / sorting by status.
- Month view or longer horizons.
- Jump-to-date input.
- Anything that *writes* data (create / cancel bookings from the grid).

---

## Files touched

| File | Change |
|------|--------|
| `src/OccupancyCalendar.java` | **new** — entry point, render, cellFor, handleKeys |
| `src/CLI.java` | add `readArrowOrKey(Scanner)` |
| `src/ReceptionMenu.java` | add "Occupancy calendar" menu item |
| `src/ManagerMenu.java` | add "Occupancy calendar" menu item |
| `src/UserMenu.java` | add "Occupancy calendar" menu item (anonymised) |
| `test/OccupancyCalendarTest.java` | **new** — unit tests for `cellFor` |

No changes to `Files`, `Bookings`, `Room`, `Account`, `DateInput`, `Main`, `SeedManager`, `GuestMenu`.
