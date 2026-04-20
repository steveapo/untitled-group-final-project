# Getting Started

## Requirements

- **Java 22** or later ([download from Adoptium](https://adoptium.net/))

Verify your Java installation:

```bash
java -version
```

## Option 1: Download the Release (Recommended)

The easiest way to get started — everything is pre-packaged and ready to run.

- **[📦 Download for macOS/Linux](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.6.4/HotelBooking-v1.6.4.tar.gz)**
- **[📦 Download for Windows](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.6.4/HotelBooking-v1.6.4.zip)**

### macOS / Linux Setup

```bash
# Extract the archive
tar -xzf HotelBooking-v1.6.4.tar.gz

# Run the application
./run.sh
```

### Windows Setup

```cmd
# Extract HotelBooking-v1.6.4.zip (right-click → Extract All)
# Open PowerShell, or Windows Terminal in the extracted folder
# Note: The functionalities work for CMD but there is formatting issues which expose the
# ANSI color codes to the user, making it quite hard to navigate. 
./run.bat
```

### Package Contents

| File | Purpose |
|------|---------|
| `HotelBooking.jar` | The application executable (requires Java 22+) |
| `run.sh` | Launch script for macOS / Linux |
| `run.bat` | Launch script for Windows |

Both launch scripts work whether invoked from the release folder directly or from a containing directory — they locate the JAR automatically.

## Option 2: Build from Source

If you prefer to compile from source code:

```bash
# Clone the repository
git clone https://github.com/steveapo/untitled-group-final-project.git
cd untitled-group-final-project

# Build the project
./build.sh              # macOS / Linux
build.bat              # Windows
```

This compiles all source files and packages them into `dist/HotelBooking.jar`.

Run with:

```bash
./dist/run.sh          # macOS / Linux   (from the repo root)
dist\run.bat           # Windows         (from the repo root)

# or, if you prefer running from inside dist/:
cd dist && ./run.sh
```

## First Run

On first launch, the system automatically seeds a live demo environment:

- **3 default accounts:**
  - `admin` / `admin` (Manager role)
  - `reception` / `reception` (Reception role)
  - `user1` / `user1` (User role)
- **9 rooms** (R101–R109, one of each basic type plus a Suite and a Penthouse)
- **Realistic bookings for `user1`** — a mix of past (CHECKED_OUT), current (CHECKED_IN), upcoming (CONFIRMED), and cancelled reservations, plus a scheduled MAINTENANCE window. Dates are computed relative to today, so the calendar always shows a live spread no matter when you first launch.

⚠ **Important:** change the default passwords before using the system for anything beyond evaluation.

### Resetting the demo

To reset back to a fresh demo state at any time, delete the data files next to the JAR and relaunch — the seeder will recreate them:

```bash
# From inside dist/
rm -f Users Rooms Bookings Errors
./run.sh
```

## Data Files

The application stores data in CSV files in the same directory as the JAR:

| File       | Contents                                      |
|------------|-----------------------------------------------|
| `Users`    | Accounts (username, hashed password, role)    |
| `Rooms`    | Room inventory (number, capacity, price, type, status) |
| `Bookings` | Reservations (past, current, upcoming, cancellations, maintenance) |
| `Errors`   | Malformed-line diagnostics and runtime warnings |

**Format guarantees**

- Strict 5-field CSV for Rooms and Bookings; 7-field for Users. Malformed lines are logged to `Errors` and skipped, never silently accepted.
- All files are read and written as **UTF-8** so round-trips between macOS/Linux (LF) and Windows (CRLF) are safe.
- Writes are **atomic** (`Files.move(... REPLACE_EXISTING, ATOMIC_MOVE)`). A crash mid-write never leaves a half-written file.

## Concurrency

The app is **single-instance only** — there is no file locking. Running two copies against the same data directory can result in one instance silently overwriting the other's changes (last-write-wins). If you need to move the app between machines, close it on the first machine before launching it on the second.

## Troubleshooting

### "Java not found" or class-file version error
Ensure Java **22 or later** is installed and in your `PATH`:

```bash
java -version
```

Most `UnsupportedClassVersionError` messages mean an older JDK is on the `PATH`.

### Application won't start on Windows
- Prefer **Windows Terminal** or **PowerShell 7+** over legacy `cmd.exe` — both honour VT escape sequences for the colour UI.
- Legacy `cmd.exe` still works but falls back to numbered menus and plain text.

### Calendar rendering looks garbled
Your terminal likely doesn't have a UTF-8 locale set. On Windows, `run.bat` already sets `chcp 65001` for you; on macOS/Linux this is usually the default. If you launch the JAR manually, ensure `-Dfile.encoding=UTF-8` is passed.

### Missing data files
If `Users`, `Rooms`, or `Bookings` is missing, the application recreates it on startup via the seeder. To force a full reset, delete the files next to the JAR and relaunch.

---

**Ready?** Start with [Guest Mode](/guides/guest) or [create a User account](/guides/user).
