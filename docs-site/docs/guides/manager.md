# Manager (Administrator) Guide

**Manager role** is for hotel owners and administrators. You have full system control including room inventory, staff management, booking oversight, and analytics.

## Logging In

From the main menu:
1. Select **"2. Login"**
2. Enter your username and password

**Demo account:** username `admin`, password `admin`

## Manager Menu

```
╔══════════════════════════════════════╗
║     MANAGER MENU                     ║
║  Logged in as: admin                 ║
╚══════════════════════════════════════╝

  1. Room Management
  2. Staff Management
  3. View all bookings
  4. View statistics
  C. Occupancy calendar

─────────────────────────────────────────
  [Esc] Logout
```

## Room Management

Select **"1. Room Management"**:

```
╔══════════════════════════════════════╗
║    ROOM MANAGEMENT                   ║
╚══════════════════════════════════════╝

  1. List all rooms
  2. Add a room
  3. Edit a room
  4. Delete a room

─────────────────────────────────────────
  [Esc] Back
```

### List All Rooms

```
╔══════════════════════════════════════╗
║     ALL ROOMS                        ║
╚══════════════════════════════════════╝

  ● R101   | Single   | $59.99/night  | AVAILABLE
  ● R102   | Double   | $89.99/night  | AVAILABLE
  ● R103   | Triple   | $119.99/night | AVAILABLE
  ● R104   | Quad     | $149.99/night | AVAILABLE
  ● R105   | Suite    | $199.99/night | MAINTENANCE

─────────────────────────────────────────
```

### Add a Room

```
╔══════════════════════════════════════╗
║      ADD ROOM                        ║
╚══════════════════════════════════════╝

Room number (e.g. R401, Esc to go back): R201
Capacity (Esc to go back): 2
Nightly price (Esc to go back): 89.99
Room type (e.g. Double, Esc to go back): Double
Status (AVAILABLE/MAINTENANCE, Esc to go back): AVAILABLE

✔ Room R201 added successfully!

Press any key to continue...
```

**Room number format:** R### (e.g., R101, R205, R999)

### Edit a Room

```
╔══════════════════════════════════════╗
║     EDIT ROOM                        ║
╚══════════════════════════════════════╝

  All Rooms:
  
  1. R101 | Single   | $59.99/night
  2. R102 | Double   | $89.99/night
  3. R103 | Triple   | $119.99/night

Select room (1-3, Esc to go back): 2

Editing R102 (Double, $89.99/night, AVAILABLE)

Choose property to edit:
  1. Capacity
  2. Price
  3. Type
  4. Status

Choice: 2

New nightly price: 94.99

✔ Room R102 updated!
```

### Delete a Room

```
╔══════════════════════════════════════╗
║    DELETE ROOM                       ║
╚══════════════════════════════════════╝

  Select room to delete (1-3, Esc to go back): 1

  ⚠ Delete R101 permanently?
  
  Confirm? (yes/no): yes
  
  ✔ Room R101 deleted!
```

**Warning:** Ensure the room has no active bookings before deletion.

## Staff Management

Select **"2. Staff Management"**:

```
╔══════════════════════════════════════╗
║   STAFF MANAGEMENT                   ║
╚══════════════════════════════════════╝

  1. View all staff
  2. Add a staff member
  3. Edit staff account
  4. Delete staff account

─────────────────────────────────────────
  [Esc] Back
```

### View All Staff

```
╔══════════════════════════════════════╗
║     ALL STAFF                        ║
╚══════════════════════════════════════╝

  user1       | John        | Doe     | john@example.com      | USER
  user2       | Jane        | Smith   | jane@example.com      | USER
  reception   | Reception   | Staff   | reception@hotel.com   | RECEPTION
  admin       | Admin       | User    | admin@hotel.com       | MANAGER

─────────────────────────────────────────
```

### Add a Staff Member

```
╔══════════════════════════════════════╗
║   ADD STAFF MEMBER                   ║
╚══════════════════════════════════════╝

Username (Esc to go back): newstaff
First name (Esc to go back): Alice
Last name (Esc to go back): Johnson
Email (Esc to go back): alice@hotel.com
Password (masked, Esc to go back): ████████

Choose role:
  1. USER
  2. RECEPTION
  3. MANAGER

Choice: 2

⠙ Creating account...
✔ Staff member newstaff added as RECEPTION!
```

### Edit Staff Account

```
╔══════════════════════════════════════╗
║    EDIT STAFF ACCOUNT                ║
╚══════════════════════════════════════╝

  1. user1       | John        | Doe        | USER
  2. reception   | Reception   | Staff      | RECEPTION
  3. admin       | Admin       | User       | MANAGER

Select staff (1-3, Esc to go back): 1

Editing user1 (John Doe, USER)

Choose property:
  1. First name
  2. Last name
  3. Email
  4. Role
  5. Password

Choice: 4

Current role: USER
New role:
  1. USER
  2. RECEPTION
  3. MANAGER

Choice: 2

✔ user1 role updated to RECEPTION!
```

### Delete Staff Account

```
╔══════════════════════════════════════╗
║   DELETE STAFF ACCOUNT               ║
╚══════════════════════════════════════╝

Select staff to delete (1-3, Esc to go back): 1

⚠ Delete user1 permanently?

Confirm? (yes/no): yes

✔ Staff member user1 deleted!
```

## View All Bookings

Select **"3. View all bookings"**:

```
╔══════════════════════════════════════╗
║    ALL BOOKINGS                      ║
╚══════════════════════════════════════╝

  user1 | R102 | 15-03-2026 → 17-03-2026 | CONFIRMED | $179.98
  user2 | R104 | 15-03-2026 → 18-03-2026 | CONFIRMED | $449.97
  user1 | R101 | 10-03-2026 → 12-03-2026 | CHECKED_OUT | $119.98
  admin | R105 | 20-03-2026 → 23-03-2026 | CONFIRMED | $599.97

─────────────────────────────────────────

Press any key to continue...
```

Complete overview of all bookings with revenue per booking.

## View Statistics

Select **"4. View statistics"**:

```
╔══════════════════════════════════════╗
║    STATISTICS                        ║
╚══════════════════════════════════════╝

  Total Bookings        : 47
  Confirmed Bookings    : 12
  Checked In            : 3
  Checked Out           : 32
  
  Total Revenue (all-time): $9,847.23
  Average Booking Value  : $209.30
  Average Stay Length    : 2.4 nights
  
  Occupancy by Room Type:
    Single    : 8 bookings
    Double    : 18 bookings
    Triple    : 12 bookings
    Quad      : 7 bookings
    Suite     : 2 bookings

─────────────────────────────────────────

Press any key to continue...
```

Use these metrics for:
- Monthly/seasonal performance reports
- Room type popularity analysis
- Revenue projections
- Staffing and capacity planning

## Interactive Occupancy Calendar

Press **C** at any menu:

```
╔══════════════════════════════════════╗
║  MARCH 2026 - OCCUPANCY CALENDAR     ║
╚══════════════════════════════════════╝

       Mon Tue Wed Thu Fri Sat Sun
  1   │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  2   │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
  3   │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  ...
```

Navigate by month to:
- Plan maintenance windows
- Identify occupancy trends
- Plan seasonal staffing
- Analyze availability patterns

## Administrative Workflows

### Opening Day Setup
1. Add all rooms to inventory
2. Set initial room statuses
3. Create reception staff accounts
4. Configure nightly pricing

```
Setup Checklist:
✓ Add rooms (Room Management → Add a room)
✓ Create reception staff (Staff Management → Add a staff member)
✓ Set pricing
✓ Test booking workflow
```

### Monthly Review
1. View all bookings
2. Check statistics
3. Generate revenue report
4. Analyze occupancy trends
5. Plan next month's pricing/staffing

### Seasonal Planning
```
High Season (Summer):
  - Increase prices
  - Ensure sufficient staffing
  - Plan maintenance in off-peak times

Low Season (Winter):
  - Reduce prices for promotion
  - Reduce staff if needed
  - Schedule maintenance
```

## Security & Best Practices

### Admin Account Security
- **Change default password immediately:** Default is `admin/admin`
- **Limit manager accounts:** Only assign to trusted administrators
- **Monitor staff changes:** Audit who has manager access

### Data Protection
- **Regular backups:** Copy Users, Rooms, Bookings files daily
- **Protect Users file:** Contains password hashes (don't commit to git)
- **Audit errors:** Review Errors file for suspicious activity

### Staff Management
- **Assign roles appropriately:**
  - USER = Guest accounts (minimal access)
  - RECEPTION = Front-desk staff (booking/check-in operations)
  - MANAGER = Admin only (system control)
  
- **Onboarding checklist:**
  1. Create staff account with RECEPTION role
  2. Provide login credentials securely
  3. Have them read [Reception Guide](/guides/reception)
  4. Monitor initial bookings for errors

---

**Questions?** Refer to [Architecture](/architecture/overview) for technical system details.
