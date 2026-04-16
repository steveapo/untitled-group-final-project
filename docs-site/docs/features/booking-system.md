# Booking System

The booking system provides an intuitive, interactive experience for reserving hotel rooms with visual date selection and real-time availability checking.

## Booking Lifecycle

```
CONFIRMED → CHECKED_IN → CHECKED_OUT
   (booked)   (in hotel)    (departed)
```

### Booking States

| State | Meaning | Who Created | Can Cancel? |
|-------|---------|-------------|------------|
| **CONFIRMED** | Room is reserved, ready for check-in | User or Reception | Yes |
| **CHECKED_IN** | Guest has arrived and checked in | Reception | No |
| **CHECKED_OUT** | Guest has departed | Reception | No |

## User Booking Flow

Users can book rooms through the **Search and book a room** option in the User menu.

### Step 1: Number of Guests

```
Enter number of guests (1-9): _
```

Specify how many people will stay. This filters room availability by capacity.

### Step 2: Interactive Date & Room Selection

**Visual Occupancy Calendar** with real-time availability:

```
        MAR 2026        
    Mon Tue Wed Thu Fri Sat Sun
 1   ██  ░░  ██  ░░  ██  ░░  ██
 2   ░░  ██  ░░  ██  ░░  ██  ░░
 3   ██  ░░  ██  ░░  ██  ░░  ██
 4   ░░  ██  ░░  ██  ░░  ██  ░░
```

**Color coding:**
- **Green (██)** = Room available for that date
- **Red (██)** = Room booked/occupied
- **Pink (██)** = Room under maintenance

**Navigation:**
| Key | Action |
|-----|--------|
| ↑ ↓ ← → | Move by one day |
| Shift+← → | Jump one week |
| h j k l | Vim-mode navigation |
| Enter | Select date or room |
| Esc | Cancel booking flow |

**Booking sequence:**
1. Navigate to **check-in date** → Press Enter
2. Navigate to **room** → Press Enter (highlights only available rooms)
3. Navigate to **check-out date** → Press Enter
4. Review and confirm booking

### Step 3: Booking Confirmation

```
Room     : R401  Double  (capacity 2)
Check-in : 15-03-2026
Check-out: 17-03-2026
Nights   : 2
Total    : $179.98

Confirm? (yes/no, Esc to cancel): _
```

- Type `yes`/`y` to confirm
- Type `no`/`n` to cancel
- Type `esc` or press Esc to cancel
- Invalid input re-prompts cleanly

On confirmation:
- Booking is saved to Bookings file
- Status set to CONFIRMED
- Room marked as occupied for those dates
- Success message displayed

## Reception Booking Creation

Reception staff can create bookings on behalf of guests through **Create booking for a guest**:

1. Enter guest's username
2. Enter number of guests
3. Select available room
4. Confirm check-in and check-out dates
5. Booking created immediately with CONFIRMED status

**Use cases:**
- Phone reservations
- Guest assistance (language barrier, elderly guests)
- Group bookings
- Walk-in arrivals

## Check-In / Check-Out

### Check-In
Convert CONFIRMED → CHECKED_IN:
1. Reception enters guest's username
2. Selects which booking to check in
3. Booking status updated
4. Guest can now access room

### Check-Out
Convert CHECKED_IN → CHECKED_OUT:
1. Reception enters guest's username
2. Selects which booking to check out
3. Booking status updated
4. Room becomes available again

## Availability Calculation

The system determines room availability by checking:

1. **Room status** — Not in MAINTENANCE
2. **Booking conflicts** — No existing booking that overlaps with requested dates
3. **Guest capacity** — Room capacity ≥ number of guests

**Date overlap rules:**
- `BOOKED: 2026-03-15 to 2026-03-17`
- Request 2026-03-14 to 2026-03-15 = **Available** (check-out/check-in on same date allowed)
- Request 2026-03-15 to 2026-03-18 = **NOT available** (overlaps existing booking)
- Request 2026-03-17 to 2026-03-19 = **Available** (no overlap)

## Pricing

### Rate Calculation
```
Total Cost = (Check-out Date - Check-in Date) × Room Nightly Rate
Example: 3 nights × $89.99/night = $269.97
```

### Price Display
Prices are shown:
- Per night (in room listings)
- Total cost (in booking confirmation)
- All in USD format with 2 decimal places

## Booking Cancellation

### User Cancellation
Users can cancel CONFIRMED bookings:
1. Select **Cancel a booking**
2. Choose booking from list of active bookings
3. Confirm cancellation
4. Booking is deleted, room becomes available

**Cannot cancel:** CHECKED_IN or CHECKED_OUT bookings (must go through reception)

### Reception Cancellation
Reception can cancel any booking:
1. Select **Cancel a booking**
2. Enter guest's username
3. Choose booking
4. Confirm cancellation
5. Booking removed from system

## Booking Search & Filtering

### Users
Users see only their own bookings:
- All CONFIRMED bookings
- All CHECKED_IN bookings (active stays)
- All CHECKED_OUT bookings (past stays)

### Reception
Reception staff see:
- All bookings in the system
- Filtered by guest username when needed
- Full booking history

### Managers
Managers can:
- View all bookings
- Analyze booking statistics
- Plan capacity based on booking trends

## Date Format

All booking dates use: **dd-MM-yyyy**

Examples:
- 15-03-2026 (March 15, 2026)
- 01-01-2026 (January 1, 2026)
- 31-12-2026 (December 31, 2026)

**Validation:**
- Invalid dates rejected (e.g., 31-02-2026)
- Check-out must be after check-in
- Dates in past rejected for new bookings

## Features

### Smart Availability Display
The interactive calendar shows:
- Real-time occupancy visualization
- Color-coded room status
- Capacity-aware filtering
- Instant feedback on date selection

### Input Validation Loop
- Invalid confirmation input re-prompts (doesn't cancel)
- Clean error messages
- Automatic line cleanup to prevent UI creep
- Flexible input (yes/y/no/n, case-insensitive)

### Cross-Role Booking
- Users can self-book through calendar
- Reception staff can create bookings on behalf of guests
- Managers can review all bookings

---

**Ready to book?** Start with [Guest Mode](/guides/guest), then [Register and Login](/guides/user).
