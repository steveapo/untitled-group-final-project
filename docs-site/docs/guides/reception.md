# Reception (Staff) Guide

**Reception role** is for front-desk staff. You can manage bookings, check guests in/out, search for availability, manage the staff roster, and control room status.

## Logging In

From the main menu:
1. Select **"2. Login"**
2. Enter your username and password

**Demo account:** username `reception`, password `reception`

## Reception Menu

```
╔══════════════════════════════════════╗
║     RECEPTION MENU                   ║
║  Logged in as: reception             ║
╚══════════════════════════════════════╝

  1. View all rooms
  2. Search available rooms by dates
  3. Create booking for a guest
  4. View all bookings
  5. Cancel a booking
  6. Check in guest
  7. Check out guest
  8. View all guests
  9. Mark room maintenance / available
  C. Occupancy calendar

─────────────────────────────────────────
  [Esc] Logout
```

## Typical Reception Workflow

### Morning: Check Today's Arrivals

Select **"4. View all bookings"**:

```
╔══════════════════════════════════════╗
║     ALL BOOKINGS                     ║
╚══════════════════════════════════════╝

  1. user1      | R102 | 15-03-2026 → 17-03-2026 | CONFIRMED
  2. user2      | R104 | 15-03-2026 → 18-03-2026 | CONFIRMED
  3. user1      | R101 | 10-03-2026 → 12-03-2026 | CHECKED_OUT
  4. admin      | R105 | 20-03-2026 → 23-03-2026 | CONFIRMED

─────────────────────────────────────────
```

You can see all guests checking in today (CONFIRMED bookings for today's date).

### Throughout Day: Create Booking for Walk-In Guest

Select **"3. Create booking for a guest"**:

```
╔══════════════════════════════════════╗
║    CREATE BOOKING                    ║
╚══════════════════════════════════════╝

Enter guest username (Esc to go back): user1
Number of guests (1-9, Esc to go back): 3

[Interactive calendar appears - select dates]

Select check-in date: 20-03-2026
Select room (for 3 guests): R103 (Triple, $119.99/night)
Select check-out date: 23-03-2026

╔══════════════════════════════════════╗
║     CONFIRM BOOKING                  ║
╚══════════════════════════════════════╝

  Room    : R103  Triple  (capacity 3)
  Check-in: 20-03-2026
  Check-out: 23-03-2026
  Nights  : 3
  Total   : $359.97

Confirm? (yes/no, Esc to cancel): yes

⠙ Saving booking...
✔ Booking created!
```

### Guest Check-In

Select **"6. Check in guest"**:

```
╔══════════════════════════════════════╗
║    CHECK IN GUEST                    ║
╚══════════════════════════════════════╝

Enter guest username (Esc to go back): user1

  Bookings for user1:
  
  1. Room R102 | 15-03-2026 → 17-03-2026 | CONFIRMED
  2. Room R101 | 20-03-2026 → 22-03-2026 | CONFIRMED

Select booking (1-2, Esc to go back): 1

✔ Guest checked in!

Press any key to continue...
```

**After check-in:**
- Room R102 status changes to CHECKED_IN
- Guest can access their room
- Booking moves from CONFIRMED to CHECKED_IN

### Guest Check-Out

Select **"7. Check out guest"**:

```
╔══════════════════════════════════════╗
║    CHECK OUT GUEST                   ║
╚══════════════════════════════════════╝

Enter guest username (Esc to go back): user1

  Checked-in bookings for user1:
  
  1. Room R102 | 15-03-2026 → 17-03-2026 | CHECKED_IN

Select booking (1, Esc to go back): 1

✔ Guest checked out!

Press any key to continue...
```

**After check-out:**
- Room R102 status changes to CHECKED_OUT
- Booking is archived
- Room becomes available for next guests

## Room Management

### View All Rooms

Select **"1. View all rooms"**:

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

Quick overview of all rooms and their status.

### Search Available Rooms by Date

Select **"2. Search available rooms by dates"**:

```
╔══════════════════════════════════════╗
║    SEARCH ROOMS BY DATE              ║
╚══════════════════════════════════════╝

Enter check-in date (dd-MM-yyyy, Esc to go back): 20-03-2026
Enter check-out date (dd-MM-yyyy, Esc to go back): 23-03-2026

⠙ Searching...

  Available Rooms
  
  ● R101   | Single   | $59.99/night
  ● R102   | Double   | $89.99/night
  ● R103   | Triple   | $119.99/night

─────────────────────────────────────────
```

Shows only rooms available for the requested date range. Useful for assisting guests with date-specific enquiries.

### Mark Room Maintenance

Select **"9. Mark room maintenance / available"**:

```
╔══════════════════════════════════════╗
║  ROOM STATUS MANAGEMENT              ║
╚══════════════════════════════════════╝

  All Rooms:
  
  1. R101 | Single   | AVAILABLE
  2. R102 | Double   | AVAILABLE
  3. R103 | Triple   | AVAILABLE
  4. R104 | Quad     | AVAILABLE
  5. R105 | Suite    | MAINTENANCE

Select room (1-5, Esc to go back): 3

Choose status:
  1. AVAILABLE
  2. MAINTENANCE

Choice: 2

✔ Room R103 status updated to MAINTENANCE!
```

Mark rooms for maintenance (cleaning, repairs) or back to AVAILABLE.

## Staff & Guest Management

### View All Guests

Select **"8. View all guests"**:

```
╔══════════════════════════════════════╗
║     ALL GUESTS                       ║
╚══════════════════════════════════════╝

  user1           | John        | Doe     | USER
  user2           | Jane        | Smith   | USER
  reception       | Reception   | Staff   | RECEPTION
  admin           | Admin       | User    | MANAGER

─────────────────────────────────────────
```

Complete staff and guest roster. Shows roles for context.

## Interactive Occupancy Calendar

Press **C** at any menu to view the occupancy calendar:

```
╔══════════════════════════════════════╗
║  MARCH 2026 - OCCUPANCY CALENDAR     ║
╚══════════════════════════════════════╝

       Mon Tue Wed Thu Fri Sat Sun
  1   │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  2   │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │
  ...
```

- **Green (██)** = Available
- **Red (██)** = Booked
- **Pink (██)** = Maintenance

Use for capacity planning and occupancy overview.

## Daily Reception Workflow

### Morning Checklist
1. View all bookings → Check today's arrivals
2. View all rooms → Verify status
3. Note any maintenance issues
4. Prepare for check-ins

### Throughout Day
1. Handle new booking requests → Create booking
2. Assist guests with enquiries → Search availability
3. Mark rooms for maintenance as needed
4. Update room status

### Evening Routine
1. Check in arriving guests
2. Check out departing guests
3. Review next day's bookings
4. Note any issues in error log

---

**Questions?** Check [Booking System](/features/booking-system) for detailed procedures.
