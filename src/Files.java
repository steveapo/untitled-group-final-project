import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class Files {

    public Vector<Bookings> populatebookings(Vector<Room> roomVector, Vector<Bookings> bookings){
        try(Scanner scanner = new Scanner(new File("Bookings"))){
            while (scanner.hasNextLine()){
                String[] data = scanner.nextLine().split(",");
                String name = data[0];
                for(Room rooms: roomVector) {
                    if(rooms.getRoom_no().equals(name)) {
                        Bookings booking = new Bookings(rooms,data[1],data[2]);
                        bookings.add(booking);
                        break;
                    }
                }
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return bookings;
    }
    public Vector<Room> populateRooms(Vector<Room> roomVector){
        try(Scanner scanner = new Scanner(new File("Rooms"))){
            while(scanner.hasNextLine()){
                String[] data = scanner.nextLine().split(",");
                Room room = new Room(data[0], Integer.parseInt(data[1]), Double.parseDouble(data[2]));
                roomVector.add(room);
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return roomVector;
    }

    public boolean checkFile(){
        try{
            File BookingsFile = new File("/Users/milto/IdeaProjects/Hotel_reservation/src/Bookings");
            File RoomsFiles = new File("/Users/milto/IdeaProjects/Hotel_reservation/src/Rooms");
            if(!BookingsFile.exists() || !RoomsFiles.exists()){
                System.out.println("Either both or one of the files was not found");
                return false;
            }else {
                System.out.println("Both file have been found");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void updateBookings( Vector<Bookings> bookingsVector) {
        File original = new File("Bookings");
        File tempfile = new File("Bookings.tmp");
        Boolean renamed;
        try (FileWriter writer = new FileWriter("Bookings.tmp", true)){
            for(int i=0;i<bookingsVector.size();i++){
                String line = bookingsVector.get(i).toString().trim();
                if(!line.isEmpty()){
                    if(i!=bookingsVector.size()-1) {
                        writer.write(line +"\n");
                    }else{
                        writer.write(line);
                    }
                }
            }

            if(original.exists()){
                original.delete();
                System.out.println("File deleted");
            }

        renamed = tempfile.renameTo(original);
            if(renamed){
                System.out.println("File successfully renamed");
            }else{
                System.out.println("File has not been renamed");
            }

        }catch (IOException e){
            System.err.println("Error writing");
        }
    }
}
