public class Room {
    private String room_no;
    private int capacity;
    private double price;


    public Room(String room_no, int capacity,double price){
        this.room_no = room_no;
        this.capacity = capacity;
        this.price = price;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setRoom_no(String room_no) {
        this.room_no = room_no;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRoom_no() {
        return room_no;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getPrice() {
        return price;
    }
    @Override
    public String toString(){
        return getRoom_no() + "," + getCapacity() + "," + getPrice();
    }
}
