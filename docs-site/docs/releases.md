# Releases

## v1.6 — April 18, 2026 (Current)

**Download:**
- [HotelBooking-v1.6.tar.gz](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.6/HotelBooking-v1.6.tar.gz) (macOS/Linux)
- [HotelBooking-v1.6.zip](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.6/HotelBooking-v1.6.zip) (Windows)

### Highlights
- 🛡 **Hardened data layer** — strict 5-field CSV parsing with malformed-line logging, UTF-8 pinned for all file I/O, atomic writes via `Files.move(... REPLACE_EXISTING, ATOMIC_MOVE)`.
- 🧰 **Unified seeding** — the `seed.sh` / `seed.bat` helpers have been retired; `SeedManager.java` is now the single source of demo data. Bookings are generated relative to today so the calendar always shows a live mix of past / current / upcoming stays.
- 🪟 **Windows-ready launchers** — `run.sh` / `run.bat` work whether invoked from the repo root or from inside `dist/`. The old `cd .../dist` hardcode that broke the dist-copied launcher is gone.
- 📅 **Scheduled maintenance in the edit flow** — picking **MAINTENANCE** under *Edit room → Status* now prompts for start + end dates and writes a dated MAINTENANCE booking (same shape the calendar's `M` hotkey has always produced). Maintenance can no longer get stuck past its end date.
- 📊 **Redesigned Booking Statistics dashboard** for managers: overview, booking-status bar chart, revenue breakdown (Realised / Booked / Lost / Total), 7-day occupancy heat map, and top-earning rooms — all inline in the terminal.
- 🎯 **Room-deletion impact preview** — before deleting a room, managers now see a breakdown of every booking attached to it and a warning if any are active/upcoming. Deletion cascades through linked bookings.
- 🎛 **Arrow-key selectors in Edit Room** — field, type, and status are all picked with ↑↓ + Enter; no free-text entry required.
- 🎛 **Calendar cursor refinements** — cursor-on-booked now renders solid **white** (vs. the solid-green idle cell) so it's always visible; cursor-on-maintenance renders solid **purple** (was dotted).
- ⌨ **Single universal cancel key** — `e` / `q` no longer act as cancel shortcuts in raw mode, freeing them as regular input. ESC is the only cancel key in a real terminal. The `e` sentinel remains in the dumb-terminal fallback because IDE consoles can't transmit a real Escape byte.

### Calendar navigation improvements
- **Tab / Shift+Tab** — jump forward / back one calendar month in all calendar instances (staff view, booking picker). Cross-platform: the same keys work identically on macOS, Linux, and Windows.
- **L key (staff)** — remove maintenance from a date range directly in the calendar. Press **L** on a maintenance cell to anchor the start, navigate to the end of the block, press **L** again to clear. Esc cancels at any point.
- **Legend consistency** — the user-facing calendar legend now correctly shows the dotted glyph (░░) for unavailable cells, matching how they actually appear in the grid.

### Tech & code quality
- `Vector` replaced with `ArrayList` / `List` across the codebase (10 files, zero behavioural change).
- Dedicated `CLI.supportsAnsi()` helper — no more string-equality hacks for ANSI detection.
- Unified date format (strict `dd-MM-uuuu`) across `DateInput`, `OccupancyCalendar`, and storage — lenient parsing that silently accepted malformed input is gone.
- Test suite bumped to **84 tests passing**; flipped two tests that previously *asserted* the silent-accept-malformed-line bug.
- README: new **Data Files & Concurrency** and **Keyboard Reference** sections.
- Statistics screen date-label alignment fixed — ANSI escape bytes no longer throw off `printf` column widths.

### Bug fixes
- 🔧 Polluted `Bookings` file (24 stale rows from broken seed scripts) cleaned out.
- 🔧 CSV drift between the Java writers and the shell seeders — the root cause that corrupted the bookings file — fixed at the source.
- 🔧 `GuestMenu` no longer freezes on a blank screen after a cancelled registration.
- 🔧 Calendar cursor invisible on booked rows (solid-green-on-solid-green) — fixed.
- 🔧 User calendar legend showed solid red (██) for unavailable; cells actually render dotted red (░░) — legend corrected.
- 🔧 Reception guide colour legend said green = available; staff view uses gray = available — corrected.

### System requirements
- **Java:** 22 or later (bundled JLine build targets a modern JDK)
- **Terminal:** Any modern terminal (macOS Terminal, Windows Terminal 10+, PowerShell 7+, iTerm2, etc.)
- **Disk:** ~1.3 MB

### Known limitations
- Single-instance only. There is no file locking — running two copies against the same data directory risks last-write-wins collisions. Close the app on machine A before launching it on machine B.

---

## v1.5 — April 17, 2026

**Download:**
- [HotelBooking-v1.5.tar.gz](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.5/HotelBooking-v1.5.tar.gz) (macOS/Linux)
- [HotelBooking-v1.5.zip](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.5/HotelBooking-v1.5.zip) (Windows)

### Pre-Loaded Data
- ✨ **3 pre-configured accounts:** admin, reception, user1
- ✨ **4 default rooms:** One of each type (Single, Double, Triple, Quad)
- ✨ **Clean initialization:** No unnecessary demo data cluttering the system
- 🔧 **Optional seed script:** `./seed.sh` (or `seed.bat` on Windows) to add demo data and statistics

### New Features
- 🎯 **Lean first-run experience** - Start with exactly what you need
- 🎯 **Optional demo data** - Run seed.sh after setup if you want sample bookings and extra rooms
- 🎯 **Separate seed scripts** - Choose to populate demo data without cluttering the main application

### System Requirements
- **Java:** 11 or higher
- **Terminal:** Any modern terminal (macOS Terminal, Windows Terminal, cmd.exe, etc.)
- **Disk Space:** ~1.3 MB

---

## v1.4 — April 17, 2026

**Download:**
- [HotelBooking-v1.4.tar.gz](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.4/HotelBooking-v1.4.tar.gz) (macOS/Linux)
- [HotelBooking-v1.4.zip](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.4/HotelBooking-v1.4.zip) (Windows)

### Critical Bug Fixes
- 🔧 **Fixed first-run crash** - Rooms and Bookings files now auto-created on startup
- 🔧 **Default room inventory** - 5 sample rooms (R101-R105) seeded automatically
- 🔧 Resolves: "Critical data files are missing. Exiting." error on fresh installs

### System Requirements
- **Java:** 11 or higher
- **Terminal:** Any modern terminal (macOS Terminal, Windows Terminal, cmd.exe, etc.)
- **Disk Space:** ~1.3 MB

---

## v1.3 — April 17, 2026

**Download:**
- [HotelBooking-v1.3.tar.gz](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.3/HotelBooking-v1.3.tar.gz) (macOS/Linux)
- [HotelBooking-v1.3.zip](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.3/HotelBooking-v1.3.zip) (Windows)

### New Features
- ✨ **Interactive Occupancy Calendar** - Visual date picker with arrow key navigation
- ✨ **Smart Booking Confirmation** - Input validation loop with clean re-prompting
- ✨ **Cross-Platform Support** - Full ANSI colors on modern terminals, graceful fallback on legacy systems
- ✨ **Improved Calendar UI** - Perfect alignment and solid highlighting for selected cells

### Bug Fixes
- 🔧 Fixed calendar header alignment with column separators
- 🔧 Fixed cell highlighting to show solid glyphs (no white boxes)
- 🔧 Fixed booking confirmation input validation (no immediate cancellation)
- 🔧 Fixed UI creep in confirmation prompt (surgical line cleanup)

### System Requirements
- **Java:** 11 or higher
- **Terminal:** Any modern terminal (macOS Terminal, Windows Terminal, cmd.exe, etc.)
- **Disk Space:** ~1.3 MB

### Installation
See [Getting Started](/getting-started.md) for detailed installation instructions.

### What's Included
- `HotelBooking.jar` - Main application
- `run.sh` - Launch script for macOS/Linux
- `run.bat` - Launch script for Windows

### Known Limitations
- Session data stored locally (not cloud-synced)
- Single-user application (no concurrent access)
- Date input format: dd-MM-yyyy

---

## Previous Versions

For full development history, see the [GitHub commit log](https://github.com/steveapo/untitled-group-final-project/commits/main).
