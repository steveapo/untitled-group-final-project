# User (Registered Guest) Guide

**User role** is for registered guests. You can search for available rooms, make bookings, manage reservations, and view your profile.

## Logging In

From the main menu:
1. Select **"2. Login"**
2. Enter your username
3. Enter your password (masked with *)
4. Press Enter

**Demo account:** username `user1`, password `user1`

### Login Screen

```
╔══════════════════════════════════════╗
║   HOTEL ROOM BOOKING SYSTEM          ║
╚══════════════════════════════════════╝

  1. Continue as Guest
  2. Login
  3. Register
  4. User Guide ↗

─────────────────────────────────────────
  [Esc] Exit

Choice: 2

Enter your username (Esc to go back): user1
Enter your password (Esc to go back): ****
⠙ Logging in...
✔ Logging in
```

## User Menu

After login, you'll see your personalized menu:

```
╔══════════════════════════════════════╗
║      USER MENU                       ║
║  Welcome, John                       ║
╚══════════════════════════════════════╝

  1. Search and book a room
  2. View my bookings
  3. Cancel a booking
  4. View my profile
  C. Occupancy calendar

─────────────────────────────────────────
  [Esc] Logout
```

## Complete Booking Journey

### Step 1: Start Booking

Select **"1. Search and book a room"**:

```
╔══════════════════════════════════════╗
║      BOOK A ROOM                     ║
╚══════════════════════════════════════╝

Number of guests (1-9, Esc to go back): 2
```

Enter the number of guests (1-9).

### Step 2: Visual Calendar Selection

The system shows an **interactive occupancy calendar**:

```
╔══════════════════════════════════════╗
║  MARCH 2026 - OCCUPANCY CALENDAR     ║
╚══════════════════════════════════════╝

       Mon Tue Wed Thu Fri Sat Sun
  10  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  11  │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
  12  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  13  │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
  14  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  15  │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │
  16  │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │
  17  │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │

  ↑↓ Navigate  Enter Select  Shift+→ Week  Esc Cancel
```

**Color Meanings:**
- **Green (██)** = Room available
- **Red (██)** = Room booked
- **Pink (██)** = Room maintenance
- **Dotted (░░)** = Normal display
- **Solid (██)** = Your cursor

**Navigation:**
- Use **↑↓←→** arrows to navigate day-by-day
- Use **Shift+Arrows** to jump by week
- Press **Enter** to select check-in date

### Step 3: Select Room

After selecting check-in date, calendar filters to show only available rooms:

```
Filtered for check-in: 15-03-2026, 2 guests

     Mon Tue Wed Thu Fri Sat Sun
15  │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ R101
     │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ R102
     │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ ░░ │ R103
```

Navigate to your preferred room and press **Enter**.

### Step 4: Select Check-Out Date

Now select the **check-out date**:

```
Selected: R102 (Double, $89.99/night)
Check-in: 15-03-2026
Select check-out date:

    Mon Tue Wed Thu Fri Sat Sun
16  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
17  │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
18  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
19  │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
20  │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
```

Navigate to check-out date and press **Enter**.

### Step 5: Review & Confirm

The system shows your booking summary:

```
╔══════════════════════════════════════╗
║     CONFIRM BOOKING                  ║
╚══════════════════════════════════════╝

  Room    : R102  Double  (capacity 2)
  Check-in: 15-03-2026
  Check-out: 17-03-2026
  Nights  : 2
  Total   : $179.98

Confirm? (yes/no, Esc to cancel): yes

✔ Booking confirmed!

Press any key to continue...
```

Type **"yes"** or **"y"** to confirm, **"no"** or **"n"** to cancel.

Invalid input re-prompts without cancelling the booking.

## View My Bookings

Select **"2. View my bookings"**:

```
╔══════════════════════════════════════╗
║      MY BOOKINGS                     ║
╚══════════════════════════════════════╝

  1. Room R102 | 15-03-2026 → 17-03-2026 | CONFIRMED
  2. Room R104 | 20-03-2026 → 23-03-2026 | CHECKED_IN
  3. Room R101 | 05-03-2026 → 07-03-2026 | CHECKED_OUT

─────────────────────────────────────────
```

### Booking Statuses
- **CONFIRMED** — Booking confirmed, ready to check in
- **CHECKED_IN** — You've checked in for this booking
- **CHECKED_OUT** — Your stay has ended

## Cancel a Booking

Select **"3. Cancel a booking"**:

```
╔══════════════════════════════════════╗
║    CANCEL A BOOKING                  ║
╚══════════════════════════════════════╝

  Your active bookings:
  
  1. Room R102 | 15-03-2026 → 17-03-2026 | $179.98
  2. Room R104 | 20-03-2026 → 23-03-2026 | $189.98

Select booking to cancel (1-2, Esc to go back): 1

✔ Booking cancelled!

Press any key to continue...
```

Only **CONFIRMED** bookings can be cancelled. Once cancelled, the booking cannot be restored.

## View Your Profile

Select **"4. View my profile"**:

```
╔══════════════════════════════════════╗
║      MY PROFILE                      ║
╚══════════════════════════════════════╝

  Username : user1
  Name     : John Doe
  Email    : john@example.com

─────────────────────────────────────────

Press any key to continue...
```

## Occupancy Calendar

Press **C** to view the full interactive calendar:

```
╔══════════════════════════════════════╗
║  MARCH 2026 - OCCUPANCY CALENDAR     ║
╚══════════════════════════════════════╝

       Mon Tue Wed Thu Fri Sat Sun
  1   │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  2   │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
  3   │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
```

Explore room availability across months to plan your stay.

---

**Questions?** Check [Booking System](/features/booking-system) for detailed feature info.
