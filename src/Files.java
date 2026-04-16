import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;
import java.util.Vector;

/** Handles all file I/O for rooms, bookings, users, and error logs. */
public class Files {

    private final File USERS_FILE;
    private final File BOOKINGS_FILE;
    private final File ROOMS_FILE;
    private final File ERRORS_FILE;

    /** Default constructor — resolves files next to the JAR (or CWD when not packaged). */
    public Files() {
        this(defaultBaseDir());
    }

    /**
     * Test- and embedding-friendly constructor — every data file is resolved
     * inside the supplied {@code baseDir}. This avoids the static-state and
     * {@code user.dir} pitfalls that make {@link Files} hard to test.
     */
    public Files(File baseDir) {
        this.USERS_FILE    = new File(baseDir, "Users");
        this.BOOKINGS_FILE = new File(baseDir, "Bookings");
        this.ROOMS_FILE    = new File(baseDir, "Rooms");
        this.ERRORS_FILE   = new File(baseDir, "Errors");
    }

    /**
     * Default base directory for data files.
     * Prefers the directory containing the running JAR (so double-clicking
     * works on Windows). Falls back to the current working directory
     * when running from class files (IDE, {@code mvn test}, etc.).
     */
    private static File defaultBaseDir() {
        try {
            File codeSource = new File(
                Files.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
            if (codeSource.isFile() && codeSource.getName().endsWith(".jar")) {
                File jarDir = codeSource.getParentFile();
                if (jarDir != null) return jarDir;
            }
        } catch (Exception ignored) { /* fall through */ }
        return new File(".");
    }

    /** Load bookings from file — expects 5 CSV fields: roomNumber,checkIn,checkOut,username,status */
    public Vector<Bookings> populateBookings(Vector<Room> rooms, Vector<Bookings> bookings) {
        try (Scanner scanner = new Scanner(BOOKINGS_FILE)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length < 3) continue;

                String roomNumber = fields[0].trim();
                for (Room room : rooms) {
                    if (room.getRoomNumber().equals(roomNumber)) {
                        String username = fields.length >= 4 ? fields[3].trim() : "unknown";
                        String status   = fields.length >= 5 ? fields[4].trim() : "CONFIRMED";
                        bookings.add(new Bookings(room, fields[1].trim(), fields[2].trim(), username, status));
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /** Load rooms from file — expects 5 CSV fields: roomNumber,capacity,price,type,status */
    public Vector<Room> populateRooms(Vector<Room> rooms) {
        try (Scanner scanner = new Scanner(ROOMS_FILE)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length < 3) continue;

                String type   = fields.length >= 4 ? fields[3].trim() : "Single";
                String status = fields.length >= 5 ? fields[4].trim() : "AVAILABLE";
                rooms.add(new Room(fields[0].trim(), Integer.parseInt(fields[1].trim()),
                        Double.parseDouble(fields[2].trim()), type, status));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    /** Check that the required data files (Rooms and Bookings) exist. */
    public boolean checkFile() {
        try {
            if (!BOOKINGS_FILE.exists() || !ROOMS_FILE.exists()) {
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

    /** Overwrite the Bookings file with all current in-memory bookings (5 fields per line). */
    public void updateBookings(Vector<Bookings> bookings) {
        File original = BOOKINGS_FILE;
        File tempFile = new File(original.getParentFile(), original.getName() + ".tmp");
        try (FileWriter writer = new FileWriter(tempFile, false)) {
            for (int i = 0; i < bookings.size(); i++) {
                String line = bookings.get(i).toString().trim();
                if (!line.isEmpty()) {
                    writer.write(i < bookings.size() - 1 ? line + "\n" : line);
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

    /** Overwrite the Rooms file with all current in-memory rooms (5 fields per line). */
    public void updateRooms(Vector<Room> rooms) {
        try (FileWriter writer = new FileWriter(ROOMS_FILE, false)) {
            for (Room room : rooms) {
                writer.write(room.toString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing rooms");
        }
    }

    /** Create the Users file if it does not already exist. */
    public void createUsersFile() {
        try {
            if (USERS_FILE.createNewFile()) {
                System.out.println("Users file created");
            }
        } catch (IOException e) {
            System.err.println("Error creating Users file");
        }
    }

    /** Append a single new user record to the Users file (7 CSV fields). */
    public void updateUsersFile(String username, String firstName, String lastName,
                                 String email, String hashedPassword, String salt, String role) {
        try (FileWriter writer = new FileWriter(USERS_FILE, true)) {
            writer.write(username + "," + firstName + "," + lastName + "," + email + ","
                    + hashedPassword + "," + salt + "," + role + "\n");
        } catch (IOException e) {
            System.err.println("Error writing user");
        }
    }

    /** Overwrite the entire Users file — used when modifying roles or deactivating staff. */
    public void updateUsersFileAll(Vector<Account> users) {
        try (FileWriter writer = new FileWriter(USERS_FILE, false)) {
            for (Account account : users) {
                writer.write(account.toString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing users");
        }
    }

    /** Return true if an admin account already exists in the Users file. */
    public boolean adminExists() {
        if (!USERS_FILE.exists() || USERS_FILE.length() == 0) return false;
        try (Scanner scanner = new Scanner(USERS_FILE)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("admin,")) return true;
            }
        } catch (IOException e) {
            // Treat as not found
        }
        return false;
    }

    /** Load all user accounts from the Users file into the provided vector — expects 7 CSV fields per line. */
    public void getUsers(Vector<Account> users) {
        users.clear();
        if (!USERS_FILE.exists() || USERS_FILE.length() == 0) {
            System.out.println("No users found.");
            return;
        }
        try (Scanner scanner = new Scanner(USERS_FILE)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length != 7) {
                    System.out.println("Skipping invalid user record: " + line);
                    continue;
                }
                byte[] hashedPasswordBytes = Base64.getDecoder().decode(fields[4].trim());
                byte[] saltBytes           = Base64.getDecoder().decode(fields[5].trim());
                users.add(new Account(fields[0].trim(), fields[1].trim(), fields[2].trim(),
                        fields[3].trim(), hashedPasswordBytes, saltBytes, fields[6].trim()));
            }
        } catch (IOException e) {
            System.err.println("Error reading Users file");
        }
    }

    /** Create the Errors log file if it does not already exist. */
    public void errorLogging() {
        try {
            if (!ERRORS_FILE.exists() && !ERRORS_FILE.createNewFile()) {
                System.err.println("Could not create Errors file");
            }
        } catch (IOException e) {
            System.err.println("Error creating Errors file");
        }
    }

    /** Append an error message to the Errors log. */
    public void writeErrors(String errorMessage) {
        try (FileWriter writer = new FileWriter(ERRORS_FILE, true)) {
            writer.write(errorMessage + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to Errors file");
        }
    }
}
