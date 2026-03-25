import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;
import java.util.Vector;

public class Files {

    private static final String USERS_FILE    = "Users";
    private static final String BOOKINGS_FILE = "Bookings";
    private static final String ROOMS_FILE    = "Rooms";

    // Load bookings from file — expects 5 fields: room_no,check_in,check_out,username,status
    public Vector<Bookings> populatebookings(Vector<Room> roomVector, Vector<Bookings> bookings) {
        try (Scanner scanner = new Scanner(new File(BOOKINGS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length < 3) continue;
                String roomNo = data[0].trim();
                for (Room room : roomVector) {
                    if (room.getRoom_no().equals(roomNo)) {
                        String username = data.length >= 4 ? data[3].trim() : "unknown";
                        String status   = data.length >= 5 ? data[4].trim() : "CONFIRMED";
                        bookings.add(new Bookings(room, data[1].trim(), data[2].trim(), username, status));
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Load rooms from file — expects 5 fields: room_no,capacity,price,type,status
    public Vector<Room> populateRooms(Vector<Room> roomVector) {
        try (Scanner scanner = new Scanner(new File(ROOMS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length < 3) continue;
                String type   = data.length >= 4 ? data[3].trim() : "Single";
                String status = data.length >= 5 ? data[4].trim() : "AVAILABLE";
                roomVector.add(new Room(data[0].trim(), Integer.parseInt(data[1].trim()),
                        Double.parseDouble(data[2].trim()), type, status));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return roomVector;
    }

    // Check that required data files exist
    public boolean checkFile() {
        try {
            File bookingsFile = new File(BOOKINGS_FILE);
            File roomsFile    = new File(ROOMS_FILE);
            if (!bookingsFile.exists() || !roomsFile.exists()) {
                System.out.println("Either both or one of the files was not found");
                return false;
            } else {
                System.out.println("Data files found.");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Overwrite Bookings file with all current bookings (5 fields per line)
    public void updateBookings(Vector<Bookings> bookingsVector) {
        File original = new File(BOOKINGS_FILE);
        File tempFile = new File(BOOKINGS_FILE + ".tmp");
        try (FileWriter writer = new FileWriter(tempFile, false)) {
            for (int i = 0; i < bookingsVector.size(); i++) {
                String line = bookingsVector.get(i).toString().trim();
                if (!line.isEmpty()) {
                    writer.write(i < bookingsVector.size() - 1 ? line + "\n" : line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing bookings");
            return;
        }
        if (original.exists()) original.delete();
        if (!tempFile.renameTo(original)) {
            System.err.println("Could not save bookings file");
        }
    }

    // Overwrite Rooms file with all current rooms (5 fields per line)
    public void updateRooms(Vector<Room> roomVector) {
        try (FileWriter writer = new FileWriter(ROOMS_FILE, false)) {
            for (Room r : roomVector) {
                writer.write(r.toString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing rooms");
        }
    }

    // Create Users file if it does not exist
    public void createUsersFile() {
        try {
            File file = new File(USERS_FILE);
            if (file.createNewFile()) {
                System.out.println("Users file created");
            }
        } catch (IOException e) {
            System.err.println("Error creating Users file");
        }
    }

    // Append a single new user to the Users file (7 fields)
    public void updateUsersFile(String username, String firstName, String lastName,
                                 String email, String hashedPassword, String salt, String role) {
        try (FileWriter writer = new FileWriter(USERS_FILE, true)) {
            writer.write(username + "," + firstName + "," + lastName + "," + email + ","
                    + hashedPassword + "," + salt + "," + role + "\n");
        } catch (IOException e) {
            System.err.println("Error writing user");
        }
    }

    // Overwrite entire Users file (used when modifying roles or deactivating staff)
    public void updateUsersFileAll(Vector<Account> userVector) {
        try (FileWriter writer = new FileWriter(USERS_FILE, false)) {
            for (Account account : userVector) {
                writer.write(account.toString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing users");
        }
    }

    // Check whether an admin account already exists in the Users file
    public boolean adminExists() {
        File file = new File(USERS_FILE);
        if (!file.exists() || file.length() == 0) return false;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("admin,")) return true;
            }
        } catch (IOException e) {
            // ignore — treated as not found
        }
        return false;
    }

    // Load all user accounts from file — expects 7 fields per line
    public void getUsers(Vector<Account> userVector) {
        File file = new File(USERS_FILE);
        if (!file.exists() || file.length() == 0) {
            System.out.println("No users found.");
            return;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length != 7) {
                    System.out.println("Skipping invalid user record: " + line);
                    continue;
                }
                byte[] hashedPasswordBytes = Base64.getDecoder().decode(data[4].trim());
                byte[] saltBytes           = Base64.getDecoder().decode(data[5].trim());
                userVector.add(new Account(data[0].trim(), data[1].trim(), data[2].trim(),
                        data[3].trim(), hashedPasswordBytes, saltBytes, data[6].trim()));
            }
        } catch (IOException e) {
            System.err.println("Error reading Users file");
        }
    }

    // Create Errors log file if it does not exist
    public void errorLogging() {
        try {
            File file = new File("Errors");
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error creating Errors file");
        }
    }

    // Append an error message to the Errors log
    public void writeErrors(String error) {
        try (FileWriter writer = new FileWriter(new File("Errors"), true)) {
            writer.write(error + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to Errors file");
        }
    }
}
