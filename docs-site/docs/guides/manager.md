# Manager (Administrator) Guide

**Manager role** is for hotel owners and administrators. You have full system control including room inventory, staff management, booking oversight, and analytics.

## Quick Navigation Tip

**Press ESC at any time to:**
- Return to Manager menu from any submenu
- Exit input fields (room numbers, names, prices, etc.)
- Cancel room/staff selection
- Go back and discard unsaved changes
- Logout from the system

ESC is your safety net — it always takes you back to where you were without making unwanted changes.

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

Arrow-key selectors pick both the target room and the field to edit:

```
╔══════════════════════════════════════╗
║              EDIT ROOM               ║
╚══════════════════════════════════════╝

  ● R101  Single  $60/n  1p
  ● R102  Double  $90/n  2p
▸ R105  Suite   $200/n 2p
  ● R106  Single  $75/n  1p
  ↑↓ Navigate  Enter Select  Esc Cancel

  Price  ($199.99/night)
  Type   (Suite)
▸ Status (AVAILABLE)
  ↑↓ Navigate  Enter Select  Esc Cancel
```

Pick **Price** → enter a new amount. Pick **Type** → arrow through Single/Double/Triple/Quad/Suite. Pick **Status** → arrow between AVAILABLE and MAINTENANCE.

### Scheduling Maintenance

Picking **MAINTENANCE** under *Status* opens a small date-range prompt, instead of flipping a permanent flag:

```
▸ MAINTENANCE
  ↑↓ Navigate  Enter Select  Esc Cancel

Maintenance start date (dd-MM-yyyy, Esc to go back): 20-04-2026
Maintenance end date   (dd-MM-yyyy, Esc to go back): 23-04-2026

⠙ Scheduling maintenance...
✔ Maintenance scheduled for R105: 20-04-2026 → 23-04-2026 (3 nights).
  The room stays AVAILABLE outside this window. Check the calendar or
  cancel the booking to clear it.
```

Under the hood, the system records a dated `MAINTENANCE` booking — the same shape the calendar's `M` hotkey has always produced. Benefits:

- **No permanently stuck rooms.** The block expires on the end date automatically.
- **Visible on the calendar.** The window shows as a purple range, with the cursor rendering as a solid purple block when it lands on a maintenance cell.
- **Clearable.** Cancel the maintenance booking to free the room up early.

If you want a room off-service indefinitely, pick a far-future end date.

### Delete a Room

Before confirming, the system shows the full blast radius — every booking attached to the room, bucketed by status. If any are active or upcoming you'll see a loud warning.

```
╔══════════════════════════════════════╗
║            DELETE ROOM               ║
╚══════════════════════════════════════╝

  Room    : R105   Suite   $199.99/night   AVAILABLE
  Bookings: 3 total  (1 checked-in, 1 upcoming, 1 past, 0 cancelled)
  ✘  This room has 2 active/upcoming booking(s). Deleting will remove
     every booking linked to it.

Type 'yes' to confirm delete R105 (Esc to go back): yes

⠙ Deleting room...
✔ Room R105 deleted (removed 3 linked booking(s)).
```

**How it cascades:** deletion removes the room **and** every booking that references it, writing both files atomically. If the disk write fails partway, you'll see a warning pointing at the Errors log — no half-committed state.

Prefer `MAINTENANCE` with a date range if you want to take the room off-service temporarily; deletion is destructive.

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

## Booking Statistics

Select **"4. View statistics"** for a multi-section operations dashboard with inline bar charts:

```
╔══════════════════════════════════════╗
║         BOOKING STATISTICS           ║
╚══════════════════════════════════════╝

  ── OVERVIEW ───────────────────────────────
  Bookings    10  total
  Rooms        9  total
  Today       ██████░░░░░░░░░░░░░░   2/9  (22%)

  ── BOOKING STATUS ─────────────────────────
  ● Confirmed       ██████░░░░░░░░░░░░░░    3   (30%)
  ● Checked in      ████░░░░░░░░░░░░░░░░    2   (20%)
  ● Checked out     ██████░░░░░░░░░░░░░░    3   (30%)
  ● Cancelled       ██░░░░░░░░░░░░░░░░░░    1   (10%)
  ● Maintenance     ██░░░░░░░░░░░░░░░░░░    1   (10%)

  ── REVENUE ────────────────────────────────
  Realised    $1,230.45   completed stays
  Booked      $2,400.00   upcoming + in-house
  Lost        $  150.00   cancellations
  ─────────────────────────────────────────────
  Total       $3,780.45

  ── OCCUPANCY — NEXT 7 DAYS ────────────────
  Fri Apr 17 (today)   ██████████████░░░░░░   6/9  (67%)
  Sat Apr 18           ████████████░░░░░░░░   5/9  (56%)
  Sun Apr 19           ██████░░░░░░░░░░░░░░   3/9  (33%)
  Mon Apr 20           ████████░░░░░░░░░░░░   4/9  (44%)
  Tue Apr 21           ██████████░░░░░░░░░░   5/9  (56%)
  Wed Apr 22           ██████████░░░░░░░░░░   5/9  (56%)
  Thu Apr 23           ████████░░░░░░░░░░░░   4/9  (44%)

  ── TOP ROOMS BY REVENUE ───────────────────
  1.  R103    $840.00    1 stay
  2.  R102    $450.00    2 stays
  3.  R101    $180.00    2 stays
```

### Section Reference

| Section | What it tells you |
|---|---|
| **Overview** | Total booking count, room inventory size, and a live today-occupancy bar. |
| **Booking Status** | Proportional breakdown of every booking in the system across five states. Each row's bar matches the colour of its dot. |
| **Revenue** | *Realised* = completed (CHECKED_OUT) stays. *Booked* = future value locked in (CONFIRMED + CHECKED_IN). *Lost* = revenue forfeited to cancellations. *Total* = Realised + Booked. |
| **Next 7 Days** | Day-by-day occupancy heat map, starting from today. Colours go cyan → green → yellow → red as the day fills up. |
| **Top Rooms** | The five highest-grossing rooms (realised + booked revenue), with stay counts. |

### How occupancy is computed

The Overview and Next-7-Days sections call directly into the same cell-resolver used by the interactive calendar (`OccupancyCalendar.cellFor(...)`), so the numbers on this screen always agree with what you'd see navigating the calendar yourself. Rooms in MAINTENANCE or occupied by any non-cancelled booking count as "occupied" for the day.

### Using the dashboard

- **Heat colours are the fastest read.** A week that glances cyan is quiet; one that glances red is nearly full. You don't need to read the numbers to spot a problem day.
- **Lost vs. Realised** is the headline KPI — a growing Lost line signals friction in the booking flow or an aggressive cancellation pattern worth investigating.
- **Top Rooms by Revenue** surfaces both high-priced and high-demand rooms, pointing at where repricing or capacity changes will have the biggest impact.

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
