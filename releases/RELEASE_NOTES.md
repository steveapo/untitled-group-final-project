# Hotel Room Booking System - v1.5 Release

**Release Date:** April 17, 2026

## What's New

### Pre-Loaded Minimal Data
- ✅ **3 accounts pre-configured:** admin (Manager), reception (Reception), user1 (User)
- ✅ **4 rooms pre-loaded:** One of each type (Single, Double, Triple, Quad)
- ✅ **Clean start:** No unnecessary dummy data in the system
- ✅ **Optional demo data:** `./seed.sh` (macOS/Linux) or `seed.bat` (Windows) to add sample bookings, extra rooms, and statistics

### Simplified Initialization
- ✅ **All three core accounts initialized on first run** - No need for manual setup
- ✅ **Consistent data structure** - All files created automatically with proper seeding
- ✅ **Flexible scaling:** Use seed scripts to add demo/test data without cluttering the main application

### Previous Release (v1.4)

#### Critical Bug Fix
- ✅ **Auto-create data files**: Rooms and Bookings files now created automatically on first run
- ✅ **Default room inventory**: Sample rooms seeded on initialization
- ✅ **First-run experience**: No more crashes due to missing data files

## Features

### Core Functionality
- **User Booking**: Browse rooms, select dates visually, confirm bookings
- **Staff Management**: View all bookings, manage occupancy across all rooms
- **Admin Control**: Full system administration and data management
- **Role-based Access Control**: Guest, User, Reception, Manager with distinct menus

### Interactive Calendar
- Arrow keys (↑↓←→) for navigation
- Shift+Arrow keys for week navigation
- Enter to select dates in booking flow
- Esc to go back or cancel
- Vim keybindings supported (h/l/k/j)

### Cross-Platform Support
- **macOS/Linux**: Full ANSI support with colors and Unicode
- **Windows 10+**: Terminal/PowerShell 7+ with VT mode support
- **Windows Legacy**: Graceful degradation to numbered menus in cmd.exe
- **IDE Consoles**: Fallback rendering in IntelliJ, VS Code, etc.

## Package Contents

- `HotelBooking.jar` - Executable application (requires Java 11+)
- `run.sh` - Launch script for macOS/Linux
- `run.bat` - Launch script for Windows
- `seed.sh` - Optional seed script for demo data (macOS/Linux)
- `seed.bat` - Optional seed script for demo data (Windows)

## Installation

### macOS/Linux
```bash
tar -xzf HotelBooking-v1.5.tar.gz
./run.sh
```

### Windows
```cmd
Expand-Archive HotelBooking-v1.5.zip
cd dist
run.bat
```

### Manual (All Platforms)
```bash
java -jar HotelBooking.jar
```

### Adding Demo Data (Optional)

After running the application at least once:

**macOS/Linux:**
```bash
./seed.sh
```

**Windows:**
```cmd
seed.bat
```

## System Requirements

- **Java Runtime**: JDK 11 or higher
- **Terminal**: Any modern terminal (macOS Terminal, Windows Terminal, cmd.exe)
- **Disk Space**: ~1.3 MB for JAR file

## Data Files

On first run, the system automatically creates:

| File | Contents |
|------|----------|
| `Users` | 3 pre-loaded accounts (admin, reception, user1) |
| `Rooms` | 4 default rooms (R101-R104, one of each type) |
| `Bookings` | Empty file, ready for reservations |
| `Errors` | Error log (created as needed) |

Running the optional seed script adds more accounts, rooms, and sample bookings.

## Known Limitations

- Session data stored locally (not cloud-synced)
- No concurrent user support (single-user application)
- Date input limited to dd-MM-yyyy format
- Seed scripts append to files (no duplicate checking)

## Support

For issues or feature requests, please open an issue on the project repository.

---

**Happy Booking!** 🎉
