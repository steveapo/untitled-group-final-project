/** Represents a hotel room with its number, capacity, price, type, and availability status. */
public class Room {

    private String roomNumber;
    private int    capacity;
    private double price;
    private String type;    // "Single", "Double", "Triple", "Quad", "Suite"
    private String status;  // "AVAILABLE", "MAINTENANCE"

    /** Construct a room with all five attributes. */
    public Room(String roomNumber, int capacity, double price, String type, String status) {
        this.roomNumber = roomNumber;
        this.capacity   = capacity;
        this.price      = price;
        this.type       = type;
        this.status     = status;
    }

    // ── Getters ──────────────────────────────────────────────────────────
    /** Return the room number identifier (e.g. "R101"). */
    public String getRoomNumber() { return roomNumber; }

    /** Return the maximum number of guests the room can accommodate. */
    public int getCapacity()      { return capacity; }

    /** Return the nightly price in dollars. */
    public double getPrice()      { return price; }

    /** Return the room type (Single, Double, Triple, Quad, Suite). */
    public String getType()       { return type; }

    /** Return the current status (AVAILABLE or MAINTENANCE). */
    public String getStatus()     { return status; }

    // ── Setters ──────────────────────────────────────────────────────────
    /** Update the room number. */
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    /** Update the room capacity. */
    public void setCapacity(int capacity)        { this.capacity = capacity; }

    /** Update the nightly price. */
    public void setPrice(double price)           { this.price = price; }

    /** Update the room type. */
    public void setType(String type)             { this.type = type; }

    /** Update the room status. */
    public void setStatus(String status)         { this.status = status; }

    /** Serialise to CSV: roomNumber,capacity,price,type,status */
    @Override
    public String toString() {
        return roomNumber + "," + capacity + "," + price + "," + type + "," + status;
    }
}
