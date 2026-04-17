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
╔══════════════════════════════════════╗
║      BOOK A ROOM                     ║
╚══════════════════════════════════════╝

Number of guests (1-9, Esc to go back): █
```

Specify how many people will stay. This filters room availability by capacity.

### Step 2: Interactive Occupancy Calendar

The system displays a **real-time visual calendar** showing room availability:

```
╔══════════════════════════════════════╗
║    MARCH 2026 - OCCUPANCY CALENDAR   ║
╚══════════════════════════════════════╝

       Mon Tue Wed Thu Fri Sat Sun
  1  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  2  │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
  3  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  4  │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
  5  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │

  ↑↓ Navigate  Enter Select  Shift+→ Week  Esc Cancel
```

**Cell Meanings:**
- **Green solid (██)** = Room available for booking
- **Red solid (██)** = Room booked or occupied
- **Pink solid (██)** = Room under maintenance
- **Dotted (░░)** = Normal cell (not selected)
- **Solid (██)** = Cursor/selected cell

**Navigation:**

| Key | Action |
|-----|--------|
| **↑ ↓** | Navigate up/down by day |
| **← →** | Navigate left/right by day |
| **Shift+← →** | Jump forward/backward by week |
| **h/j/k/l** | Vim-mode navigation (alternate) |
| **Enter** | Select date or room |
| **Esc** | Cancel booking flow |

**Booking Sequence:**

```
1. Navigate to check-in date
   ↓ Press Enter
   ↓
2. Calendar filters to show only available rooms
   Navigate to your desired room
   ↓ Press Enter
   ↓
3. Navigate to check-out date
   ↓ Press Enter
   ↓
4. View confirmation screen
```

### Step 3: Booking Confirmation

The system shows a summary of your booking and asks for confirmation:

```
╔══════════════════════════════════════╗
║     CONFIRM BOOKING                  ║
╚══════════════════════════════════════╝

  Room     : R401  Double  (capacity 2)
  Check-in : 15-03-2026
  Check-out: 17-03-2026
  Nights   : 2
  Total    : $179.98

Confirm? (yes/no, Esc to cancel): █
```

**Valid responses:**
- `yes` or `y` → Confirm and create booking
- `no` or `n` → Cancel booking
- `Esc` → Cancel and return to User menu
- Any other input → Re-prompt (doesn't cancel)

On confirmation:
- Booking is saved to Bookings file with CONFIRMED status
- Room marked as occupied for those dates
- Success message displayed

```
✔ Booking confirmed!

Press any key to continue...
```

## Reception Booking Creation

Reception staff can create bookings on behalf of guests through **Create booking for a guest**:

```
╔══════════════════════════════════════╗
║      CREATE BOOKING                  ║
╚══════════════════════════════════════╝

  Enter guest username (Esc to go back): user1
  Number of guests (1-9, Esc to go back): 2
  
  [Calendar appears - select dates and room]
  
  ✔ Booking created!
```

**Use cases:**
- Phone reservations
- Guest assistance (language barrier, elderly guests)
- Group bookings
- Walk-in arrivals

## Check-In / Check-Out

### Check-In

Convert CONFIRMED → CHECKED_IN:

```
╔══════════════════════════════════════╗
║     CHECK IN GUEST                   ║
╚══════════════════════════════════════╝

  Enter guest username (Esc to go back): user1
  
  1. Room R401 | 15-03-2026 → 17-03-2026 | CONFIRMED
  
  Select booking: █
  
  ✔ Guest checked in!
```

### Check-Out

Convert CHECKED_IN → CHECKED_OUT:

```
╔══════════════════════════════════════╗
║    CHECK OUT GUEST                   ║
╚══════════════════════════════════════╝

  Enter guest username (Esc to go back): user1
  
  1. Room R401 | 15-03-2026 → 17-03-2026 | CHECKED_IN
  
  Select booking: █
  
  ✔ Guest checked out!
```

## Availability Calculation

The system determines room availability by checking:

1. **Room status** — Not permanently MAINTENANCE.
2. **Booking conflicts** — No existing non-cancelled booking (including dated MAINTENANCE windows) that overlaps with the requested dates.
3. **Guest capacity** — Room capacity ≥ number of guests.

**Scheduled maintenance** is stored as a booking with status `MAINTENANCE` (guest `SYSTEM`), not as a permanent flag. This means a scheduled window blocks the room only for its date range — the day after the window ends, the room is available again automatically. Managers schedule windows either by pressing `M` in the occupancy calendar or by picking MAINTENANCE under *Edit Room → Status* and entering start + end dates.

**Date overlap rules:**
- `BOOKED: 2026-03-15 to 2026-03-17`
- Request 2026-03-14 to 2026-03-15 = **Available** (check-out / check-in on the same date is allowed).
- Request 2026-03-15 to 2026-03-18 = **Not available** (overlaps the existing booking).
- Request 2026-03-17 to 2026-03-19 = **Available** (no overlap — check-out day is not an occupied night).

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

```
╔══════════════════════════════════════╗
║   CANCEL A BOOKING                   ║
╚══════════════════════════════════════╝

  Your active bookings:
  
  1. Room R401 | 15-03-2026 → 17-03-2026 | $179.98
  2. Room R102 | 20-03-2026 → 22-03-2026 | $179.98
  
  Select booking to cancel: █
  
  ✔ Booking cancelled!
```

**Cannot cancel:** CHECKED_IN or CHECKED_OUT bookings (must go through reception)

### Reception Cancellation
Reception can cancel any booking:

```
╔══════════════════════════════════════╗
║   CANCEL A BOOKING                   ║
╚══════════════════════════════════════╝

  Enter guest username (Esc to go back): user1
  
  1. Room R401 | 15-03-2026 → 17-03-2026 | CONFIRMED
  
  Select booking to cancel: █
  
  ✔ Booking cancelled!
```

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
- Color-coded room status (green/red/pink)
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
