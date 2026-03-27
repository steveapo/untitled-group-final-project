# System Overview

## Architecture Layers

The application follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│              Presentation Layer              │
│  Main  │  GuestMenu  │  UserMenu            │
│         ReceptionMenu │  ManagerMenu         │
├─────────────────────────────────────────────┤
│              UI Utilities                    │
│  CLI (colours, spinners, selectors, input)  │
├─────────────────────────────────────────────┤
│              Business Logic                  │
│  Account (auth, registration, hashing)      │
│  DateInput (validation, availability)       │
│  SeedManager (initial data seeding)         │
├─────────────────────────────────────────────┤
│              Data Model                      │
│  Room  │  Bookings  │  Account              │
├─────────────────────────────────────────────┤
│              Persistence Layer               │
│  Files (CSV read/write for all entities)    │
├─────────────────────────────────────────────┤
│              Storage                         │
│  Users  │  Rooms  │  Bookings  │  Errors    │
└─────────────────────────────────────────────┘
```

## Class Responsibilities

| Class | Layer | Responsibility |
|-------|-------|---------------|
| `Main` | Entry point | Initialisation, main menu loop, role-based dispatch |
| `CLI` | UI utilities | ANSI colours, screen clearing, spinners, password masking, selectors |
| `GuestMenu` | Presentation | Anonymous browsing, login/register gateway |
| `UserMenu` | Presentation | Room search, booking, cancellation, profile |
| `ReceptionMenu` | Presentation | Full booking lifecycle, check-in/out, room status |
| `ManagerMenu` | Presentation | Room CRUD, staff management, statistics |
| `Account` | Business logic | Registration, login, password hashing, validation |
| `DateInput` | Business logic | Date validation, room availability filtering |
| `SeedManager` | Business logic | Admin account seeding on first run |
| `Room` | Data model | Room entity with getters/setters and CSV serialisation |
| `Bookings` | Data model | Booking entity linking room, guest, dates, and status |
| `Files` | Persistence | All file I/O operations for CSV data files |

## Control Flow

```
Main.main()
  ├── Initialise: create files, seed admin
  ├── Load: rooms, bookings, users from CSV
  └── Menu loop:
        ├── 1. Guest  → GuestMenu.show()
        ├── 2. Login  → Account.login() → dispatch()
        ├── 3. Register → Account.register()
        └── 4. Exit

dispatch(account)
  ├── USER      → UserMenu.show()
  ├── RECEPTION → ReceptionMenu.show()
  └── MANAGER   → ManagerMenu.show()
```

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 11+ |
| Collections | `Vector` (thread-safe, legacy) |
| Password hashing | SHA-512 with `SecureRandom` salt |
| Encoding | Base64 for hash/salt storage |
| Date handling | `LocalDate` with `DateTimeFormatter` (strict mode) |
| Terminal I/O | ANSI escape codes, `stty` for raw input |
| Persistence | CSV flat files |
| Build | `javac` + `jar` (no build tool dependencies) |
