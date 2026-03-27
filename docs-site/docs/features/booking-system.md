# Booking System

## Booking Lifecycle

Every booking follows a defined state machine:

```
CONFIRMED  →  CHECKED_IN  →  CHECKED_OUT
    ↓
CANCELLED
```

| Status | Meaning | Who can trigger |
|--------|---------|-----------------|
| CONFIRMED | Reservation made, awaiting arrival | User or Reception |
| CHECKED_IN | Guest has arrived and is staying | Reception only |
| CHECKED_OUT | Guest has departed | Reception only |
| CANCELLED | Reservation cancelled | User or Reception |

## Availability Engine

When searching for rooms, the system checks for **date overlaps** against all active bookings:

- A room is available if no active booking overlaps the requested date range
- **Cancelled bookings are ignored** — they don't block availability
- **Same-day turnaround is allowed** — a new check-in on the same day as another booking's check-out is permitted (check-out day is not an occupied night)

### Overlap Detection

Two date ranges overlap when:

```
requestedStart < bookedEnd AND requestedEnd > bookedStart
```

This correctly handles all edge cases including partial overlaps, containment, and adjacent bookings.

## Room Types

| Type | Typical Capacity | Price Range |
|------|-----------------|-------------|
| Single | 1 guest | $70 - $75 |
| Double | 2 guests | $90 - $110 |
| Triple | 3 guests | $120 - $140 |
| Quad | 4 guests | $150 - $180 |
| Suite | 5 guests | $500 |

## Room Status

Rooms have two possible statuses:

- **AVAILABLE** — can be booked by guests
- **MAINTENANCE** — hidden from searches and booking flows

Reception and Manager roles can toggle room status using an interactive arrow-key selector.

## Revenue Tracking

The Manager statistics dashboard calculates revenue from completed stays:

```
Revenue = sum of (nights × price per night) for all CHECKED_OUT bookings
```

Where `nights` is the number of days between check-in and check-out dates.
