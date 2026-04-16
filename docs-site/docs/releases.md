# Releases

## v1.0 - April 17, 2026

**Download:** 
- [HotelBooking-v1.0.tar.gz](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.0/HotelBooking-v1.0.tar.gz) (macOS/Linux)
- [HotelBooking-v1.0.zip](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.0/HotelBooking-v1.0.zip) (Windows)

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

This is the first public release. For development history, see the [GitHub commit log](https://github.com/steveapo/untitled-group-final-project/commits/main).
