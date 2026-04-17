# Hotel Room Booking System - v1.4 Release

**Release Date:** April 17, 2026

## What's New

### Critical Bug Fix
- ✅ **Auto-create data files**: Rooms and Bookings files now created automatically on first run
- ✅ **Default room inventory**: 5 sample rooms (R101-R105) seeded on initialization
- ✅ **First-run experience**: No more crashes due to missing data files
- **This resolves:** Windows users (and others) getting "Critical data files are missing. Exiting." error

### Previous Release (v1.3)

### Calendar & Booking Flow Refinements

#### Occupancy Calendar
- ✅ **Fixed header alignment**: Day names and date numbers now properly aligned with column separators
- ✅ **Improved cell highlighting**: Selected cells now show solid green (available) or red (unavailable) glyphs
- ✅ **Clean visual distinction**: Dotted glyphs (░░) for normal state, solid glyphs (██) for selected state
- ✅ **Maintenance display**: Proper pink solid glyphs for maintenance status

#### Booking Confirmation
- ✅ **Input validation loop**: Invalid input no longer cancels the booking flow
- ✅ **Clean re-prompting**: Error messages displayed with automatic cleanup on next input
- ✅ **Flexible input**: Accepts 'y', 'yes', 'n', 'no' (case-insensitive)
- ✅ **ESC support**: Press ESC to cancel at any time

### Bug Fixes
- Fixed UI creep: Confirmation prompt no longer loses lines with repeated invalid input
- Fixed cell styling: Removed ANSI code layering that caused visibility issues
- Fixed header-to-grid alignment: Column separators now consistent throughout

## Features

### Core Functionality
- **User Booking**: Browse rooms, select dates visually, confirm bookings
- **Staff Management**: View all bookings, manage occupancy across all rooms
- **Admin Control**: Full system administration and data management

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

## Installation

### macOS/Linux
```bash
tar -xzf HotelBooking-v1.0.tar.gz
./run.sh
```

### Windows
```cmd
Expand-Archive HotelBooking-v1.0.zip
cd dist
run.bat
```

### Manual (All Platforms)
```bash
java -jar HotelBooking.jar
```

## System Requirements

- **Java Runtime**: JDK 11 or higher
- **Terminal**: Any modern terminal (macOS Terminal, Windows Terminal, cmd.exe)
- **Disk Space**: ~2 MB for JAR file

## Known Limitations

- Session data stored locally (not cloud-synced)
- No concurrent user support (single-user application)
- Date input limited to dd-MM-yyyy format

## Support

For issues or feature requests, please open an issue on the project repository.

---

**Happy Booking!** 🎉
