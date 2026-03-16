import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;
import java.util.Vector;

public class Files {

    public Vector<Bookings> populatebookings(Vector<Room> roomVector, Vector<Bookings> bookings){
        try(Scanner scanner = new Scanner(new File("Bookings"))){

            while (scanner.hasNextLine()){

                String[] data = scanner.nextLine().split(",");
                String name = data[0];
                //For each Room check if its included in the bookings
                for(Room rooms: roomVector) {

                    //Check the room name
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
            File BookingsFile = new File("Bookings");
            File RoomsFiles = new File("Rooms");
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

    public void CreateUsersFile(){
        try {

            File file = new File("Users");
            //Create
            if(file.createNewFile()){
                System.out.println("File has been created");
            }else{
                System.out.println("File already exists");
            }

        }catch (IOException e){
            System.err.println("Error creating file");
            }
        }
        //Added any new users in the file
public void updateUsersFile(String username, String fname,String lname, String email, String hashed_pass, String salt ) {
        //Write to file the users
        try (FileWriter writer = new FileWriter("Users", true)){
            writer.write(username+",");
            writer.write(fname+",");
            writer.write(lname+",");
            writer.write(email+",");
            writer.write(hashed_pass+",");
            writer.write(salt);
            writer.write("\n");
        }catch (IOException e){
            System.err.println("Error writing");
        }
}

    //Get all the users in the file
    public void getUsers(Vector<Account> userVector) {
        File file = new File("Users");

        // Check if the file exists and is not empty
        if (!file.exists() || file.length() == 0) {
            System.out.println("No users found.");
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Skip blank lines
                if (line.isEmpty()) continue;

                // Split the line into exactly 6 fields: username, fname, lname, email, hashed_password, salt
                String[] data = line.split(",");
                if (data.length != 6) {
                    System.out.println("Invalid data: " + line);
                    continue;
                }

                String username = data[0].trim();
                String fname = data[1].trim();
                String lname = data[2].trim();
                String email = data[3].trim();
                String hashed_password = data[4].trim();
                String stored_salt = data[5].trim();

                // Decode Base64 strings into byte arrays
                byte[] hashed_password_bytes = Base64.getDecoder().decode(hashed_password);
                byte[] stored_salt_bytes = Base64.getDecoder().decode(stored_salt);

                // Create Account object and add it to the vector
                Account account = new Account(username, fname, lname, email, hashed_password_bytes, stored_salt_bytes);
                userVector.add(account);
            }
        } catch (IOException e) {
            System.err.println("Error reading Users file");
        }
    }

    //Add another function for error logging.
    public void errorLogging(){
        try{
            File file = new File("Errors");
            if(!file.exists()){
                file.createNewFile();
                System.out.println("File has been created");
            }else{
                System.out.println("File already exists");
            }
        }catch (IOException e){
            System.err.println("Error creating 'Errors' file");
        }
    }
    public void writeErrors(String error){
        File file = new File("Errors");

        try(FileWriter writer = new FileWriter(file, true);){
            writer.write(error + "\n");
        }catch (IOException e){
            System.err.println("Error writing");
        }
    }
}
