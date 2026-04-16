# Data Model

The system uses four CSV tables for persistent data storage. All files are human-readable text and can be edited manually if needed.

## Users Table

**File:** `Users`

Stores user accounts with credentials and roles.

### Schema

```csv
username,firstName,lastName,email,hashedPassword,salt,role
```

| Field | Type | Description |
|-------|------|-------------|
| **username** | String | Unique user identifier (alphanumeric) |
| **firstName** | String | User's first name (letters only) |
| **lastName** | String | User's last name (letters only) |
| **email** | String | Email address (must contain @) |
| **hashedPassword** | String (hex) | SHA-512 hash of (password + salt) |
| **salt** | String (hex) | 16-byte random salt (hex-encoded) |
| **role** | String | USER, RECEPTION, or MANAGER |

### Example

```csv
username,firstName,lastName,email,hashedPassword,salt,role
admin,Admin,User,admin@hotel.com,8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918,7e5e8f6d4b3c2a1f0e9d8c7b6a5f4e3d,MANAGER
user1,John,Doe,john@example.com,6b0d31c0d563223024da45691584643ac78f7bfa8f1b5e3f629aac2953eb13a7,1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d,USER
reception,Jane,Smith,jane@hotel.com,a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3,0d1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c,RECEPTION
```

### Security Notes
- Passwords never stored in plain text
- Hashes are irreversible (one-way)
- Each account has unique salt
- Even identical passwords produce different hashes

---

## Rooms Table

**File:** `Rooms`

Stores the hotel's room inventory.

### Schema

```csv
roomNumber,capacity,price,type,status
```

| Field | Type | Description |
|-------|------|-------------|
| **roomNumber** | String | Unique room ID (format: R###, e.g., R401) |
| **capacity** | Integer | Number of guests the room accommodates (1-4) |
| **price** | Double | Nightly rate in USD |
| **type** | String | Single, Double, Triple, Quad, or Suite |
| **status** | String | AVAILABLE or MAINTENANCE |

### Example

```csv
roomNumber,capacity,price,type,status
R101,1,59.99,Single,AVAILABLE
R102,2,89.99,Double,AVAILABLE
R103,3,119.99,Triple,AVAILABLE
R104,4,149.99,Quad,AVAILABLE
R105,2,199.99,Suite,MAINTENANCE
R201,1,59.99,Single,AVAILABLE
R202,2,89.99,Double,AVAILABLE
R203,3,119.99,Triple,AVAILABLE
```

### Constraints
- Room numbers must be unique
- Room number format: R### (R followed by 3 digits)
- Capacity must be 1-4 (or more for suites)
- Price must be positive
- Status must be AVAILABLE or MAINTENANCE

### Room Types

| Type | Capacity | Typical Use | Price Range |
|------|----------|-------------|------------|
| Single | 1 | Solo travelers | $50-70 |
| Double | 2 | Couples, two guests | $80-120 |
| Triple | 3 | Small families | $110-160 |
| Quad | 4 | Groups, families | $140-200 |
| Suite | 2+ | Premium guests | $180-300 |

---

## Bookings Table

**File:** `Bookings`

Stores all room reservations.

### Schema

```csv
roomNumber,checkIn,checkOut,username,status
```

| Field | Type | Description |
|-------|------|-------------|
| **roomNumber** | String | Reference to Rooms.roomNumber |
| **checkIn** | String | Check-in date (dd-MM-yyyy format) |
| **checkOut** | String | Check-out date (dd-MM-yyyy format) |
| **username** | String | Reference to Users.username |
| **status** | String | CONFIRMED, CHECKED_IN, or CHECKED_OUT |

### Example

```csv
roomNumber,checkIn,checkOut,username,status
R101,15-03-2026,17-03-2026,user1,CONFIRMED
R102,14-03-2026,18-03-2026,user2,CONFIRMED
R103,16-03-2026,19-03-2026,user1,CHECKED_IN
R104,10-03-2026,12-03-2026,user2,CHECKED_OUT
R101,20-03-2026,22-03-2026,user1,CONFIRMED
```

### Constraints
- Check-out date must be after check-in date
- Room must exist in Rooms table
- Username must exist in Users table
- Status must be one of: CONFIRMED, CHECKED_IN, CHECKED_OUT
- Date format must be dd-MM-yyyy

### Booking Lifecycle

```
CONFIRMED (initial state)
    ↓
CHECKED_IN (guest arrives)
    ↓
CHECKED_OUT (guest leaves)

Alternative: CONFIRMED → (cancelled) = deleted from file
```

### Availability Rules

A room is **available** for dates X-Y if:
1. Room status is AVAILABLE (not MAINTENANCE)
2. No existing booking has overlap with [X, Y)
3. Room capacity ≥ requested guest count

**Overlap logic:**
- Existing: [2026-03-15, 2026-03-17)
- Request:  [2026-03-14, 2026-03-15) = Available (no overlap)
- Request:  [2026-03-15, 2026-03-18) = NOT available (overlaps)
- Request:  [2026-03-17, 2026-03-19) = Available (no overlap)

---

## Errors Table

**File:** `Errors`

Logs validation errors and system events for debugging.

### Schema

Plain text, one message per line:

```
[timestamp implied by line order]
Error message text - Class: class.name Line: lineNumber
```

### Example

```
Names cannot have numbers - Class: class Account Line: 91
Room number must follow R### format - Class: class ManagerMenu Line: 87
Email must contain @ symbol - Class: class Account Line: 109
Unknown role: SUPERUSER - user: hacker - Class: class Main Line: 101
```

### Purpose
- Debugging aid for developers
- Audit trail for user actions
- Error rate analysis
- System health monitoring

---

## Data File Locations

All data files are stored in the same directory as `HotelBooking.jar`:

```
dist/
├── HotelBooking.jar
├── run.sh
├── run.bat
├── Users              ← Accounts
├── Rooms              ← Room inventory
├── Bookings           ← Reservations
└── Errors             ← Error log
```

## Initialization

### First Run
When the application starts:
1. Check if `Users` file exists
2. If not, create it with auto-seeded admin account:
   - Username: `admin`
   - Password: `admin` (hashed with random salt)
   - Role: `MANAGER`
3. Verify `Rooms` and `Bookings` files exist (error if missing)
4. Load data into memory

### Data Validation

On load, system validates:
- All CSV files are well-formed
- Foreign keys exist (room references, user references)
- Dates are parseable (dd-MM-yyyy)
- Prices are positive numbers
- Statuses are valid

Invalid data causes application to exit with error message.

## Manual Editing

CSV files can be edited manually with any text editor:

**Example: Manually add a room**
```csv
R999,2,79.99,Double,AVAILABLE
```

**Requirements:**
- Follow exact CSV format (comma-separated)
- Keep columns in order
- No extra/missing columns
- Valid values for constrained fields

**After editing:**
1. Save the file
2. Restart application
3. System re-validates on load

## Backup & Recovery

### Regular Backups
```bash
# Linux/macOS
cp Users Users.backup.$(date +%Y%m%d_%H%M%S)
cp Rooms Rooms.backup.$(date +%Y%m%d_%H%M%S)
cp Bookings Bookings.backup.$(date +%Y%m%d_%H%M%S)
```

### Recovery
```bash
cp Users.backup.20260315_140000 Users
# Restart application
```

## Performance

### Scalability
- **File I/O:** Full re-write on every change (not optimal for large datasets)
- **In-memory loading:** Entire dataset held in Vector<T> during execution
- **Suitable for:** Small-to-medium hotels (100-500 rooms, 1000-10000 bookings)

### Optimization
- Consider database (SQLite, PostgreSQL) for 1000+ bookings
- Use transactions for concurrent access (not currently supported)
- Archive old bookings to separate file for faster queries

---

**Next:** See [File Structure](/architecture/file-structure) for project layout.
