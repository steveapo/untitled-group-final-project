# File Structure

## Project Layout

```
untitled-group-final-project/
├── src/                          # Java source files
│   ├── Main.java                 # Entry point and main menu
│   ├── CLI.java                  # Terminal UI utilities
│   ├── Account.java              # User accounts and auth
│   ├── Bookings.java             # Booking data model
│   ├── Room.java                 # Room data model
│   ├── DateInput.java            # Date validation and availability
│   ├── Files.java                # CSV file I/O
│   ├── SeedManager.java          # Admin account seeding
│   ├── GuestMenu.java            # Guest browsing menu
│   ├── UserMenu.java             # Registered user menu
│   ├── ReceptionMenu.java        # Receptionist menu
│   └── ManagerMenu.java          # Manager/owner menu
├── Bookings                      # Booking data (CSV)
├── Rooms                         # Room inventory (CSV)
├── Users                         # User accounts (CSV, hashed passwords)
├── Errors                        # Validation error log
├── build.sh / build.bat          # Build scripts
├── run.sh / run.bat              # Launch scripts
├── docs/                         # Project documentation (PDFs)
└── docs-site/                    # This documentation website
```

## Source Files by Category

### Data Models (3 files)
- `Room.java` — Room entity with CSV serialisation
- `Bookings.java` — Booking entity with status tracking
- `Account.java` — User entity with password hashing and login logic

### Business Logic (2 files)
- `DateInput.java` — Date parsing, validation, and room availability engine
- `SeedManager.java` — First-run admin seeding and CLI hash generator

### Presentation (5 files)
- `Main.java` — Application entry point and role-based routing
- `GuestMenu.java` — Anonymous room browsing
- `UserMenu.java` — Guest booking management
- `ReceptionMenu.java` — Front-desk operations
- `ManagerMenu.java` — Administrative functions

### Infrastructure (2 files)
- `CLI.java` — Cross-platform terminal utilities
- `Files.java` — Centralised CSV file I/O

## Data Files

All data files are plain CSV stored alongside the JAR. No database is required.

| File | Fields per line | Example |
|------|----------------|---------|
| `Users` | 7 | `user1,Alice,Smith,alice@hotel.com,hash,salt,USER` |
| `Rooms` | 5 | `R101,1,70.0,Single,AVAILABLE` |
| `Bookings` | 5 | `R101,01-04-2026,05-04-2026,user1,CONFIRMED` |
| `Errors` | 1 | `Invalid date format - class DateInput - Line: 41` |

### File Update Strategy

- **Rooms and Users** — full file overwrite when any record changes
- **Bookings** — atomic write via temporary file + rename (prevents corruption)
- **Errors** — append-only log
