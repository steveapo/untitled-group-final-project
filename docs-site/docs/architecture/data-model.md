# Data Model

## Entities

### Room

Represents a hotel room in the inventory.

| Field | Type | Example | Description |
|-------|------|---------|-------------|
| `roomNumber` | String | `R101` | Unique room identifier |
| `capacity` | int | `2` | Maximum guests |
| `price` | double | `90.0` | Nightly rate in dollars |
| `type` | String | `Double` | Room category |
| `status` | String | `AVAILABLE` | Current availability |

**CSV format:** `R101,2,90.0,Double,AVAILABLE`

### Bookings

Represents a reservation linking a room to a guest.

| Field | Type | Example | Description |
|-------|------|---------|-------------|
| `room` | Room | (reference) | The booked room |
| `checkIn` | String | `01-04-2026` | Check-in date (dd-MM-yyyy) |
| `checkOut` | String | `05-04-2026` | Check-out date (dd-MM-yyyy) |
| `username` | String | `user1` | Guest who made the booking |
| `status` | String | `CONFIRMED` | Booking state |

**CSV format:** `R101,01-04-2026,05-04-2026,user1,CONFIRMED`

**Status values:** `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`

### Account

Represents a user account with hashed credentials.

| Field | Type | Example | Description |
|-------|------|---------|-------------|
| `username` | String | `user1` | Login identifier |
| `firstName` | String | `Alice` | First name |
| `lastName` | String | `Smith` | Last name |
| `email` | String | `alice@hotel.com` | Email address |
| `hashedPassword` | byte[] | (binary) | SHA-512 hash |
| `salt` | byte[] | (binary) | Random salt |
| `role` | String | `USER` | Access level |

**CSV format:** `username,first,last,email,base64Hash,base64Salt,role`

**Role values:** `USER`, `RECEPTION`, `MANAGER`

## Relationships

```
Account (1) ──── (many) Bookings
  │                        │
  │ username               │ room reference
  │                        │
  └── role determines ──→  Room (1) ──── (many) Bookings
      which menus
      are accessible
```

- An Account can have many Bookings (identified by username)
- A Room can have many Bookings (identified by room number)
- Bookings link accounts to rooms with a date range and status
