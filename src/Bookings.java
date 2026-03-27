/** Represents a hotel room booking, linking a room to a guest and date range. */
public class Bookings {

    private Room   room;
    private String checkIn;
    private String checkOut;
    private String username; // guest who made the booking
    private String status;   // "CONFIRMED", "CANCELLED", "CHECKED_IN", "CHECKED_OUT"

    /** Construct a booking with all five attributes. */
    public Bookings(Room room, String checkIn, String checkOut, String username, String status) {
        this.room     = room;
        this.checkIn  = checkIn;
        this.checkOut = checkOut;
        this.username = username;
        this.status   = status;
    }

    // ── Getters ──────────────────────────────────────────────────────────
    /** Return the booked room. */
    public Room   getRoom()     { return room; }

    /** Return the check-in date string (dd-MM-yyyy). */
    public String getCheckIn()  { return checkIn; }

    /** Return the check-out date string (dd-MM-yyyy). */
    public String getCheckOut() { return checkOut; }

    /** Return the username of the guest who made this booking. */
    public String getUsername() { return username; }

    /** Return the current booking status. */
    public String getStatus()   { return status; }

    // ── Setters ──────────────────────────────────────────────────────────
    /** Update the booked room. */
    public void setRoom(Room room)             { this.room = room; }

    /** Update the check-in date. */
    public void setCheckIn(String checkIn)     { this.checkIn = checkIn; }

    /** Update the check-out date. */
    public void setCheckOut(String checkOut)   { this.checkOut = checkOut; }

    /** Update the guest username. */
    public void setUsername(String username)   { this.username = username; }

    /** Update the booking status. */
    public void setStatus(String status)       { this.status = status; }

    /** Serialise to CSV: roomNumber,checkIn,checkOut,username,status */
    @Override
    public String toString() {
        return room.getRoomNumber() + "," + checkIn + "," + checkOut + "," + username + "," + status;
    }
}
