# Manager / Owner Guide

The Manager menu provides full control over the hotel. Log in with a **MANAGER** role account (default: `admin` / `admin`).

## 1. Room Management

A sub-menu for managing the hotel's room inventory.

### List All Rooms

Shows every room with status indicator, type, price, and availability.

### Add a Room

Create a new room by entering:
- **Room number** (e.g. `R401`) — must be unique
- **Capacity** — number of guests
- **Price per night** — in dollars
- **Type** — Single, Double, Triple, Quad, or Suite

### Edit a Room

Modify an existing room's:
- **Price** — update the nightly rate
- **Type** — change the room category
- **Status** — toggle between AVAILABLE and MAINTENANCE using the arrow-key selector

### Delete a Room

Remove a room from the inventory. Requires confirmation before deletion.

> **Note:** Deleting a room does not automatically cancel existing bookings for that room.

## 2. Staff Management

Manage hotel staff accounts.

### List All Staff

Shows all accounts with RECEPTION or MANAGER roles, including username, name, email, and role.

### Add Receptionist

Creates a new account through the standard registration flow, then automatically promotes it to the RECEPTION role.

### Deactivate Staff Account

Demotes a staff member by changing their role from RECEPTION or MANAGER to USER. The account is not deleted — they can still log in as a regular guest.

## 3. View All Bookings

Shows every booking in the system with room number, status, dates, and guest username.

## 4. View Statistics

Displays a dashboard of booking metrics:

| Metric | Description |
|--------|-------------|
| **Active bookings** | CONFIRMED + CHECKED_IN count |
| **Cancelled** | Total cancelled bookings |
| **Checked out** | Completed stays |
| **Revenue** | Total income from CHECKED_OUT bookings (nights x price) |

Revenue is calculated by multiplying the number of nights by the room's nightly price for each completed booking.

## 5. Logout

Returns to the main menu.
