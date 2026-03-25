public class Bookings {
    private Room room;
    private String check_in, check_out;
    private String username;  // which user made the booking
    private String status;    // "CONFIRMED", "CANCELLED", "CHECKED_IN", "CHECKED_OUT"

    public Bookings(Room room, String check_in, String check_out) {
        this.room = room;
        this.check_in = check_in;
        this.check_out = check_out;
        this.username = "unknown";
        this.status = "CONFIRMED";
    }

    public Bookings(Room room, String check_in, String check_out, String username, String status) {
        this.room = room;
        this.check_in = check_in;
        this.check_out = check_out;
        this.username = username;
        this.status = status;
    }

    public String getCheck_in()  { return check_in; }
    public String getCheck_out() { return check_out; }
    public Room getRoom()        { return room; }
    public String getUsername()  { return username; }
    public String getStatus()    { return status; }

    public void setCheck_in(String check_in)   { this.check_in = check_in; }
    public void setCheck_out(String check_out) { this.check_out = check_out; }
    public void setRoom(Room room)             { this.room = room; }  // fixed: was self-assign
    public void setUsername(String username)   { this.username = username; }
    public void setStatus(String status)       { this.status = status; }

    @Override
    public String toString() {
        return room.getRoom_no() + "," + check_in + "," + check_out + "," + username + "," + status;
    }
}
