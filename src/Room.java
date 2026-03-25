public class Room {
    private String room_no;
    private int capacity;
    private double price;
    private String type;    // "Single", "Double", "Triple", "Quad", "Suite"
    private String status;  // "AVAILABLE", "MAINTENANCE"

    public Room(String room_no, int capacity, double price) {
        this.room_no = room_no;
        this.capacity = capacity;
        this.price = price;
        this.type = "Single";
        this.status = "AVAILABLE";
    }

    public Room(String room_no, int capacity, double price, String type, String status) {
        this.room_no = room_no;
        this.capacity = capacity;
        this.price = price;
        this.type = type;
        this.status = status;
    }

    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setRoom_no(String room_no) { this.room_no = room_no; }
    public void setPrice(double price)     { this.price = price; }
    public void setType(String type)       { this.type = type; }
    public void setStatus(String status)   { this.status = status; }

    public String getRoom_no()  { return room_no; }
    public int getCapacity()    { return capacity; }
    public double getPrice()    { return price; }
    public String getType()     { return type; }
    public String getStatus()   { return status; }

    @Override
    public String toString() {
        return room_no + "," + capacity + "," + price + "," + type + "," + status;
    }
}
