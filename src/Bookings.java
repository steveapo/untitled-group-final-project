import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class Bookings {
    private Room room;
    private String check_in, check_out;

    public Bookings(Room room,String check_in, String check_out){
        this.room =room;
        this.check_in = check_in;
        this.check_out = check_out;
    }
    public void formatString(){

    }

    public String getCheck_in() {
        return this.check_in;
    }

    public Room getRoom() {
        return this.room;
    }
    public String getCheck_out(){
        return this.check_out;
    }

    public void setCheck_in(String check_in) {
        this.check_in = check_in;
    }

    public void setCheck_out(String check_out) {
        this.check_out = check_out;
    }

    public void setRoom(String id) {
        this.room = room;
    }

    @Override
    public String toString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
        return this.room.getRoom_no() +","+this.check_in+","+this.check_out;
    }
}
