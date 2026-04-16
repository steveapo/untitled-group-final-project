# System Overview

The Hotel Room Booking System is a terminal-based Java application with a role-based architecture, file-based data persistence, and cross-platform terminal support via JLine 3.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│              Main Menu                              │
│  (Guest / Login / Register / User Guide)            │
└───────────────┬─────────────────────────────────────┘
                │
        ┌───────┴────────┬──────────┬────────────┐
        │                │          │            │
    ┌───▼──┐      ┌──────▼──┐  ┌───▼────┐  ┌───▼─────┐
    │Guest │      │   User   │  │Reception│  │ Manager │
    │Menu  │      │   Menu   │  │  Menu   │  │  Menu   │
    └──────┘      └─────────┘   └────────┘   └────────┘
        │              │          │            │
        └──────────────┴──────────┴────────────┘
                       │
            ┌──────────┴──────────┐
            │                     │
        ┌───▼────────┐    ┌──────▼──────┐
        │   Data     │    │     CLI     │
        │Persistence │    │  Interface  │
        │ (CSV Files)│    │ (JLine 3)   │
        └────────────┘    └─────────────┘
```

## Core Components

### 1. Main Entry Point
- **Class:** `Main.java`
- **Responsibility:** Application initialization, data loading, top-level menu loop
- **Key functions:**
  - Auto-seed admin account on first run
  - Load users, rooms, and bookings from files
  - Route authenticated users to role-specific menus
  - Provide guest mode access

### 2. Authentication System
- **Class:** `Account.java`
- **Responsibility:** User credentials, authentication, account management
- **Key features:**
  - Login/register flow
  - SHA-512 password hashing with random salt
  - Role assignment (USER, RECEPTION, MANAGER)
  - Profile management

### 3. Menu Systems (Role-Based)
| Menu | Class | Access | Functions |
|------|-------|--------|-----------|
| Guest | GuestMenu.java | No auth | View rooms, login, register |
| User | UserMenu.java | After login | Book rooms, manage bookings |
| Reception | ReceptionMenu.java | RECEPTION role | Handle check-in/out, manage all bookings |
| Manager | ManagerMenu.java | MANAGER role | Room/staff management, statistics |

### 4. Data Models
- **Room.java** — Room entity (number, capacity, price, type, status)
- **Bookings.java** — Booking entity (room, guest, dates, status)
- **Account.java** — User account entity (credentials, role)

### 5. Business Logic
- **OccupancyCalendar.java** — Interactive calendar rendering and navigation
- **DateInput.java** — Date validation and parsing
- **SeedManager.java** — Initial data seeding

### 6. Data Persistence
- **Files.java** — CSV file I/O for rooms, bookings, users, and error logs
- **Data format:** Comma-separated values (human-readable)
- **Files created:** Users, Rooms, Bookings, Errors

### 7. CLI Framework
- **CLI.java** — Cross-platform terminal utilities
- **Powered by:** JLine 3
- **Features:**
  - ANSI color codes
  - Raw input (arrow keys, single keypresses)
  - Password masking
  - Spinners and loading indicators
  - Interactive list selection
  - Cross-OS terminal detection

## Data Flow

### Booking a Room (User)
```
1. User selects "Search and book a room"
   ↓
2. User specifies number of guests
   ↓
3. OccupancyCalendar displays interactive calendar
   - Shows rooms available for that guest count
   - User navigates and selects check-in date
   - User selects room
   - User selects check-out date
   ↓
4. System calculates cost and shows confirmation
   ↓
5. User confirms (yes/no/esc)
   ↓
6. If confirmed:
   - New Bookings object created
   - File.updateBookings(bookings) writes to CSV
   - Success message shown
   
7. If cancelled:
   - Return to user menu
```

### Check-In Guest (Reception)
```
1. Reception selects "Check in guest"
   ↓
2. Reception enters guest username
   ↓
3. System finds all CONFIRMED bookings for that user
   ↓
4. Reception selects which booking to check in
   ↓
5. Booking status changed: CONFIRMED → CHECKED_IN
   ↓
6. File.updateBookings(bookings) writes to CSV
```

## Design Patterns

### Model-View-Controller (Loose)
- **Models:** Room, Account, Bookings (data classes)
- **Views:** *Menu classes (GuestMenu, UserMenu, etc.)
- **Controllers:** Main, Files (orchestration and persistence)

### Role-Based Dispatch
```java
switch (account.getRole()) {
    case "USER":      UserMenu.show(...)
    case "RECEPTION": ReceptionMenu.show(...)
    case "MANAGER":   ManagerMenu.show(...)
}
```

### Functional Input Validation
```java
CLI.promptUntilValid(promptText, scanner, input -> {
    // Validate and return Result.ok() or Result.err()
});
```

Re-prompts on error, never cancels (unless user presses ESC).

### Singleton Terminal
```java
private static final Terminal TERMINAL = buildTerminal();
private static final boolean ANSI_SUPPORTED = detectAnsiSupport();
```

JLine terminal is built once and reused (singleton pattern).

## Cross-Platform Execution Path

```
JVM starts → CLI.buildTerminal()
  ↓
Is JLine available?
  ├─ Yes: Try to detect terminal type
  │  ├─ macOS/Linux: Use POSIX termios via JNA
  │  ├─ Windows 10+: Enable VT mode in conhost
  │  └─ IDE console: Gracefully fall back (dumb terminal)
  │
  └─ No: Use System.console() then Scanner fallback
  
Result: ANSI_SUPPORTED flag set, used for all color output
```

## File Organization

### Source Structure
```
src/
  Main.java                   # Entry point
  Account.java                # User accounts
  Room.java                   # Room model
  Bookings.java               # Booking model
  CLI.java                    # Terminal utilities (JLine 3)
  Files.java                  # CSV persistence
  GuestMenu.java              # Guest role menu
  UserMenu.java               # User role menu
  ReceptionMenu.java          # Reception role menu
  ManagerMenu.java            # Manager role menu
  OccupancyCalendar.java      # Interactive calendar
  DateInput.java              # Date validation
  SeedManager.java            # Initial data seeding
```

### Data Files (CSV)
```
Users                # Accounts (hashed passwords, roles)
Rooms                # Room inventory
Bookings             # All reservations
Errors               # Error log
```

## Security Model

### Password Storage
- **Never stored:** Plain-text passwords
- **Always hashed:** SHA-512 with unique salt per account
- **Salt:** 16-byte random value (SecureRandom)

### Access Control
- **Session model:** Stateless (no tokens)
- **Per-request auth:** Account object passed through method calls
- **Logout:** Return to main menu (implicit)

### Data Validation
- **At boundary:** All user input validated
- **Lenient fallback:** Graceful degradation for non-ANSI terminals

---

**Next:** Explore [Data Model](/architecture/data-model) for database schema details.
