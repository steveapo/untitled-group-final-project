# Hotel Room Booking System

A command-line hotel room booking application built in Java. Supports multiple user roles, interactive terminal UI with ANSI colors and keyboard navigation, and file-based CSV persistence.

Developed as a final project for ITC2205.

**Documentation:** [untitled-group-self.vercel.app](https://untitled-group-self.vercel.app)

---

## Quick Start

**Prerequisites:** Java 22 or later (the bundled JLine build targets a modern JDK)

Download the latest release ZIP, extract it, and run:

```bash
# macOS / Linux
./run.sh

# Windows
run.bat
```

Or run the JAR directly:

```bash
java -jar dist/HotelBooking.jar
```

---

## Build from Source

Clone the repository and run the build script:

```bash
git clone https://github.com/steveapo/untitled-group-final-project.git
cd untitled-group-final-project

# macOS / Linux
chmod +x build.sh
./build.sh

# Windows (PowerShell or Command Prompt)
build.bat
```

This compiles all sources, packages them into `dist/HotelBooking.jar`, and copies the data files into `dist/`.

---

## Demo Accounts

On first launch the system seeds three accounts and a realistic mix of rooms and
bookings (dates computed relative to today, so the calendar always shows a live
spread of past / current / upcoming reservations).

| Username    | Password    | Role         |
|-------------|-------------|--------------|
| `admin`     | `admin`     | Manager      |
| `reception` | `reception` | Receptionist |
| `user1`     | `user1`     | User         |

You can also browse as a **Guest** without logging in. Change these passwords
before using the system for anything beyond evaluation.

---

## Project Structure

```
src/
  Main.java            Entry point
  CLI.java             Terminal UI (ANSI colors, spinners, key input)
  Account.java         Authentication and user management
  Room.java            Room definitions and availability
  Bookings.java        Booking creation, lookup, and cancellation
  DateInput.java       Date parsing and validation
  Files.java           CSV file I/O
  SeedManager.java     Initial data seeding
  GuestMenu.java       Guest menu flows
  UserMenu.java        Registered user menu flows
  ReceptionMenu.java   Receptionist menu flows
  ManagerMenu.java     Manager/Owner menu flows

test/                  Unit tests

docs-site/             VitePress documentation site

Rooms                  Room data (CSV)
Bookings               Booking records (CSV)
Users                  User accounts (CSV)
Errors                 Error log (CSV)
```

---

## Key Features

- **Four user roles** -- Guest, User, Receptionist, and Manager/Owner -- each with scoped menus
- **Interactive terminal UI** -- arrow-key selectors, masked password input, loading spinners, and ESC hotkey navigation
- **SHA-512 password hashing** with random salts
- **File-based persistence** using CSV files (no external database required)
- **Cross-platform** -- runs on macOS, Linux, and Windows

---

## Data Files & Concurrency

The system persists state to four plain-text files alongside the JAR:

| File       | Contents                                      |
|------------|-----------------------------------------------|
| `Users`    | Accounts (username, hashed password, role)    |
| `Rooms`    | Room inventory                                |
| `Bookings` | Reservations (past, current, upcoming)        |
| `Errors`   | Runtime warnings and skipped-line diagnostics |

Writes are **atomic**: each update writes to a temp file and then performs a
`Files.move(..., REPLACE_EXISTING, ATOMIC_MOVE)`. A crash mid-write will never
leave a half-written data file. All files are read and written as UTF-8 so
round-tripping between macOS/Linux (LF) and Windows (CRLF) is safe.

> **Single-instance only.** This is a single-user CLI demo. There is **no
> file locking**, so running two copies of the application against the same
> data directory at once can result in one instance silently overwriting the
> other's changes (last-write-wins). If you need to move the app between
> machines, close it on the first machine before launching it on the second.

On first launch, if any of `Users` / `Rooms` / `Bookings` is missing, the app
seeds a fresh copy through [`SeedManager`](src/SeedManager.java). To reset the
demo at any time, simply delete the data files and relaunch — the seeder will
recreate them with a live spread of demo bookings.

---

## Keyboard Reference

| Context                       | Keys                                                          |
|-------------------------------|---------------------------------------------------------------|
| Menus                         | `1`-`9` choose · `C` calendar · `Esc` go back / logout        |
| Lists & selectors             | `↑`/`↓` navigate · `Enter` select · `Esc` cancel              |
| Occupancy calendar            | `←`/`→` day · `Shift+←`/`→` week · `↑`/`↓` room · `T` today · `Esc` back |
| Calendar (staff only)         | `M` start/end maintenance range                               |
| Text input                    | type freely · `Backspace` delete · `Enter` confirm · `Esc` cancel |
| IDE console fallback (no ESC) | enter `e` on its own line to cancel                           |

`Esc` is the universal cancel/back key in real terminals. Only when the raw
keyboard is unavailable (IDE consoles, piped stdin) does the fallback accept
the literal input `e` as a cancel sentinel.

---

## License

See repository for license details.
