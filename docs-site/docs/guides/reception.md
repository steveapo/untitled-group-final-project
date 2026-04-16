# Reception (Staff) Guide

**Reception role** is for front-desk staff. You can manage bookings, check guests in/out, search for availability, manage the staff roster, and control room status.

## Logging In

From the main menu:
1. Select **"2. Login"**
2. Enter your username and password
3. Press Enter

**Demo account:** username `reception`, password `reception`

## Reception Menu Options

| Option | Function |
|--------|----------|
| **1. View all rooms** | See complete room inventory with status |
| **2. Search available rooms by dates** | Find rooms available for a given date range |
| **3. Create booking for a guest** | Manually create a booking on behalf of a guest |
| **4. View all bookings** | See all hotel bookings across all guests |
| **5. Cancel a booking** | Cancel any booking (guest or staff-created) |
| **6. Check in guest** | Mark a CONFIRMED booking as CHECKED_IN |
| **7. Check out guest** | Mark a CHECKED_IN booking as CHECKED_OUT |
| **8. View all guests** | See the complete guest roster and staff accounts |
| **9. Mark room maintenance / available** | Change room status for maintenance or availability |
| **C. Occupancy calendar** | View the full interactive occupancy calendar |

## Managing Rooms

### View All Rooms

Displays every room with:
```
● R401   | Double   | $89.99/night  | AVAILABLE
● R402   | Single   | $59.99/night  | AVAILABLE
● R403   | Suite    | $149.99/night | MAINTENANCE
```

**Color coding:**
- **Green ●** = AVAILABLE (ready for guests)
- **Red ●** = MAINTENANCE or OCCUPIED

### Search Available Rooms

Find rooms for a specific date range:
1. Enter check-in date (dd-MM-yyyy format)
2. Enter check-out date
3. System shows only available rooms for those dates

Useful for helping guests find suitable options or for front-desk planning.

## Booking Management

### Create Booking for a Guest

Manually create a booking without requiring guest login:
1. Enter the guest's username
2. Select number of guests
3. Choose room from availability list
4. Enter check-in and check-out dates
5. System creates CONFIRMED booking immediately

**Useful for:**
- Phone/in-person bookings
- Group reservations
- Guest assistance with difficulty using terminal

### View All Bookings

See every booking in the system with:
- Guest username
- Room number
- Check-in and check-out dates
- Current status (CONFIRMED, CHECKED_IN, CHECKED_OUT)

### Cancel Booking

Cancel any booking. After cancellation:
- Booking is removed from the system
- Room becomes available again
- Action is logged in error logs

## Guest Check-In/Check-Out

### Check In Guest

Convert a CONFIRMED booking to CHECKED_IN:
1. Enter guest's username
2. Select their booking from the list
3. System updates booking status

**After check-in:**
- Guest can access room
- Booking status changes to CHECKED_IN

### Check Out Guest

Convert a CHECKED_IN booking to CHECKED_OUT:
1. Enter guest's username
2. Select their booking
3. System marks as CHECKED_OUT

**After check-out:**
- Room becomes available for next guests
- Booking is archived

## Managing Staff & Guests

### View All Guests

See complete roster of:
- **Users** (registered guests with USER role)
- **Reception staff** (other staff members)
- **Managers** (hotel administrators)

Displays: username, first name, last name, email, role

### Room Status Management

Mark rooms for maintenance or availability:

1. Select a room
2. Choose status:
   - **AVAILABLE** — Room is ready for guests
   - **MAINTENANCE** — Room is under maintenance (not available for booking)

**Maintenance rooms:**
- Show as red (●) in room lists
- Do not appear in availability searches
- Can be marked AVAILABLE again once maintenance is complete

## Occupancy Calendar

Press **C** to view the interactive calendar with full color coding:
- **Green (██)** = Available rooms
- **Red (██)** = Booked/occupied rooms
- **Pink (██)** = Maintenance status

Navigate with arrow keys to check occupancy by date.

## Daily Workflow

**Morning:**
1. View all bookings to see today's check-ins
2. Check in arriving guests
3. Verify room status

**Throughout day:**
1. Handle new booking requests (create manual bookings)
2. Assist guests with enquiries (search availability)
3. Mark rooms for maintenance as needed

**Evening:**
1. Check out departing guests
2. Verify all rooms accounted for
3. Review next day's arrivals

---

**Manager contact:** For staffing changes or system issues, contact the manager.
