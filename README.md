# Hotel Room Booking System

A command-line hotel room booking application built in Java. Supports multiple user roles, interactive terminal UI with ANSI colors and keyboard navigation, and file-based CSV persistence.

Developed as a final project for ITC2205.

**Documentation:** [untitled-group-self.vercel.app](https://untitled-group-self.vercel.app)

---

## Quick Start

**Prerequisites:** Java 11 or later

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

| Username    | Password    | Role         |
|-------------|-------------|--------------|
| `admin`     | `admin`     | Manager      |
| `reception` | `reception` | Receptionist |
| `user1`     | `user1`     | User         |
| `user2`     | `user1`     | User         |

You can also browse as a **Guest** without logging in.

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

## License

See repository for license details.
