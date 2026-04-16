# Manager (Administrator) Guide

**Manager role** is for hotel owners and administrators. You have full system control including room inventory, staff management, booking oversight, and analytics.

## Logging In

From the main menu:
1. Select **"2. Login"**
2. Enter your username and password

**Demo account:** username `admin`, password `admin`

## Manager Menu Options

| Option | Function |
|--------|----------|
| **1. Room Management** | Create, edit, delete, and manage room inventory |
| **2. Staff Management** | Manage user accounts and staff roles |
| **3. View all bookings** | See all hotel bookings with full details |
| **4. View statistics** | Analyze booking data and occupancy trends |
| **C. Occupancy calendar** | View the complete interactive occupancy calendar |

---

## Room Management

Access the complete room management suite:

### List All Rooms

View all rooms with full details:
```
● R401   | Double   | $89.99/night  | AVAILABLE
● R402   | Single   | $59.99/night  | AVAILABLE
● R403   | Suite    | $149.99/night | MAINTENANCE
```

### Add a Room

Create a new room with:
- **Room number** (format: R### e.g., R401)
- **Capacity** (1-4 guests per room, or more for suites)
- **Price** (nightly rate in dollars)
- **Type** (Single, Double, Triple, Quad, Suite)
- **Status** (AVAILABLE or MAINTENANCE)

**Room number format:** Must follow R### pattern (R followed by 3 digits)
- Example: R101, R201, R1001

**Room types:**
| Type | Typical Use | Capacity |
|------|-------------|----------|
| Single | Solo travelers | 1 |
| Double | Couples/two guests | 2 |
| Triple | Small families | 3 |
| Quad | Groups | 4+ |
| Suite | Premium guests | 2+ |

### Edit a Room

Modify existing room properties:
- Update price
- Change capacity
- Modify room type
- Update status
- Cannot change room number (identified by number)

**Use case:** Adjust pricing seasonally or re-classify room type

### Delete a Room

Permanently remove a room from inventory.

**Warning:** Deleting a room with active bookings may cause system issues. Ensure the room has no CONFIRMED or CHECKED_IN bookings before deletion.

---

## Staff Management

Manage all user accounts and roles:

### View All Staff

See complete roster with:
- Username
- Full name (first + last)
- Email address
- Current role

### Add a Staff Member

Create new staff/user account:
1. Enter username (unique identifier)
2. Enter first name
3. Enter last name
4. Enter email address
5. Enter password
6. Assign role:
   - **USER** = Guest account
   - **RECEPTION** = Front-desk staff
   - **MANAGER** = Administrator (rare)

### Edit Staff Account

Modify existing account:
- Change name
- Update email
- **Change role** (USER ↔ RECEPTION ↔ MANAGER)

**Use case:** Promote a USER to RECEPTION when hiring new staff

### Delete Staff Account

Remove a staff or user account from the system.

**Note:** If the account has active bookings, those bookings will become orphaned. Consider disabling instead of deleting for active accounts.

---

## Viewing All Bookings

See every booking in the hotel:

| Field | Information |
|-------|-------------|
| Guest | Username |
| Room | Room number |
| Check-in | Date (dd-MM-yyyy) |
| Check-out | Date (dd-MM-yyyy) |
| Status | CONFIRMED, CHECKED_IN, or CHECKED_OUT |

**Useful for:**
- Daily occupancy overview
- Identifying no-shows
- Revenue tracking
- Guest history analysis

---

## Statistics

Analyze hotel performance metrics:

**Available statistics include:**
- Total bookings (all-time)
- Current occupancy rate
- Occupancy by room type
- Average booking duration
- Revenue summary (total nights × rates)

**Use cases:**
- Monthly performance reports
- Seasonal trend analysis
- Room type popularity analysis
- Staff shift planning based on occupancy

---

## Occupancy Calendar

Press **C** to view the interactive calendar covering all rooms and dates:

**Navigation:**
- **Arrow keys** (↑↓←→) = Move day-by-day
- **Shift+Arrows** = Jump by week
- **Esc** = Exit

**Color coding:**
- **Green (██)** = Room available
- **Red (██)** = Room booked/occupied
- **Pink (██)** = Room under maintenance

View multiple months to plan capacity and maintenance schedules.

---

## Administrative Workflows

### Opening Day Setup
1. Add all rooms to inventory
2. Set initial room statuses
3. Create reception staff accounts
4. Set nightly pricing

### Daily Operations
1. Check occupancy (occupancy calendar)
2. Review today's check-ins/check-outs
3. Monitor staff activity
4. Adjust room status as needed

### Seasonal Management
1. Update pricing for high/low seasons
2. Plan maintenance windows
3. Adjust room inventory if needed
4. Analyze booking trends

### End-of-Month Review
1. View statistics and revenue
2. Generate occupancy reports
3. Plan staffing for next month
4. Identify under-utilized room types

### Staff Onboarding
1. Create new RECEPTION role account
2. Provide login credentials
3. Guide them through reception guide
4. Monitor initial bookings

---

## Security Notes

- **Manager accounts should be limited** — Only hotel owners/administrators
- **Password management** — Encourage strong passwords
- **Data integrity** — Regular backups recommended (save CSV files)
- **Audit trail** — Error logs are written to `Errors` file for all major operations

---

**Questions?** Refer to [Architecture](/architecture/overview) for technical details about the system design.
