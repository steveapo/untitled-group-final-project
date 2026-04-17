import java.io.File;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Scanner;

/**
 * Handles all file I/O for rooms, bookings, users, and error logs.
 * <p>All files are read and written as UTF-8 so data survives round-trips across
 * macOS/Linux (LF) and Windows (CRLF) — {@code nextLine()} strips the terminator
 * and a final {@code .trim()} removes any stray {@code \r}.
 */
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

    /** Create a UTF-8 scanner over the given file. */
    private static Scanner utf8Scanner(File f) throws IOException {
        return new Scanner(new FileInputStream(f), StandardCharsets.UTF_8);
    }

    /** Create a UTF-8 writer over the given file (truncate or append). */
    private static Writer utf8Writer(File f, boolean append) throws IOException {
        return new OutputStreamWriter(new FileOutputStream(f, append), StandardCharsets.UTF_8);
    }

    /**
     * Load bookings from file — strict 5-field CSV:
     * {@code roomNumber,checkIn,checkOut,username,status}.
     * Malformed lines are logged to the Errors file and skipped.
     * Bookings whose room is missing from {@code rooms} are likewise skipped with a warning.
     */
    public List<Bookings> populateBookings(List<Room> rooms, List<Bookings> bookings) {
        if (!BOOKINGS_FILE.exists()) return bookings;
        int lineNo = 0;
        try (Scanner scanner = utf8Scanner(BOOKINGS_FILE)) {
            while (scanner.hasNextLine()) {
                lineNo++;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length != 5) {
                    writeErrors("Skipping malformed booking line " + lineNo
                            + " (expected 5 fields, got " + fields.length + "): " + line);
                    continue;
                }
                String roomNumber = fields[0].trim();
                Room match = null;
                for (Room room : rooms) {
                    if (room.getRoomNumber().equals(roomNumber)) { match = room; break; }
                }
                if (match == null) {
                    writeErrors("Skipping booking for unknown room '" + roomNumber
                            + "' at line " + lineNo + ": " + line);
                    continue;
                }
                bookings.add(new Bookings(match, fields[1].trim(), fields[2].trim(),
                        fields[3].trim(), fields[4].trim()));
            }
        } catch (IOException e) {
            writeErrors("Error reading Bookings file: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Load rooms from file — strict 5-field CSV:
     * {@code roomNumber,capacity,price,type,status}.
     * Malformed or unparsable numeric fields are logged and the line is skipped.
     */
    public List<Room> populateRooms(List<Room> rooms) {
        if (!ROOMS_FILE.exists()) return rooms;
        int lineNo = 0;
        try (Scanner scanner = utf8Scanner(ROOMS_FILE)) {
            while (scanner.hasNextLine()) {
                lineNo++;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length != 5) {
                    writeErrors("Skipping malformed room line " + lineNo
                            + " (expected 5 fields, got " + fields.length + "): " + line);
                    continue;
                }
                try {
                    rooms.add(new Room(
                            fields[0].trim(),
                            Integer.parseInt(fields[1].trim()),
                            Double.parseDouble(fields[2].trim()),
                            fields[3].trim(),
                            fields[4].trim()));
                } catch (NumberFormatException e) {
                    writeErrors("Skipping room line " + lineNo + " (bad number): " + line);
                }
            }
        } catch (IOException e) {
            writeErrors("Error reading Rooms file: " + e.getMessage());
        }
        return rooms;
    }

    /** Check that the required data files (Rooms and Bookings) exist. */
    public boolean checkFile() {
        return BOOKINGS_FILE.exists() && ROOMS_FILE.exists();
    }

    /** Check if the Rooms file exists. */
    public boolean roomsFileExists() {
        return ROOMS_FILE.exists();
    }

    /** Check if the Bookings file exists. */
    public boolean bookingsFileExists() {
        return BOOKINGS_FILE.exists();
    }

    /**
     * Overwrite the Bookings file atomically. Returns {@code true} on success.
     * Uses a temp file + {@link java.nio.file.Files#move} with
     * {@link StandardCopyOption#REPLACE_EXISTING} so a crash mid-write never
     * leaves a half-written target.
     */
    public boolean updateBookings(List<Bookings> bookings) {
        return atomicWriteLines(BOOKINGS_FILE, bookings, Bookings::toString, "Bookings");
    }

    /** Overwrite the Rooms file atomically. */
    public boolean updateRooms(List<Room> rooms) {
        return atomicWriteLines(ROOMS_FILE, rooms, Room::toString, "Rooms");
    }

    /** Overwrite the entire Users file atomically. */
    public boolean updateUsersFileAll(List<Account> users) {
        return atomicWriteLines(USERS_FILE, users, Account::toString, "Users");
    }

    /** Atomic write helper — serialise each element on its own line with \n terminators. */
    private <T> boolean atomicWriteLines(File target, java.util.List<T> items,
                                          java.util.function.Function<T, String> lineFn,
                                          String label) {
        File parent = target.getParentFile();
        File tempFile;
        try {
            tempFile = File.createTempFile(target.getName() + ".", ".tmp",
                    parent != null ? parent : new File("."));
        } catch (IOException e) {
            writeErrors("Could not create temp file for " + label + ": " + e.getMessage());
            return false;
        }
        try (Writer writer = utf8Writer(tempFile, false)) {
            for (T item : items) {
                String line = lineFn.apply(item);
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty()) continue;
                writer.write(line);
                writer.write('\n');
            }
        } catch (IOException e) {
            writeErrors("Error writing " + label + ": " + e.getMessage());
            tempFile.delete();
            return false;
        }
        try {
            try {
                java.nio.file.Files.move(tempFile.toPath(), target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                java.nio.file.Files.move(tempFile.toPath(), target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            writeErrors("Could not replace " + label + " file: " + e.getMessage());
            tempFile.delete();
            return false;
        }
    }

    /** Create the Users file if it does not already exist. */
    public void createUsersFile() {
        try {
            USERS_FILE.createNewFile();
        } catch (IOException e) {
            writeErrors("Error creating Users file: " + e.getMessage());
        }
    }

    /** Create the Rooms file (empty). */
    public void createRoomsFile() {
        try {
            ROOMS_FILE.createNewFile();
        } catch (IOException e) {
            writeErrors("Error creating Rooms file: " + e.getMessage());
        }
    }

    /** Create the Bookings file (empty). */
    public void createBookingsFile() {
        try {
            BOOKINGS_FILE.createNewFile();
        } catch (IOException e) {
            writeErrors("Error creating Bookings file: " + e.getMessage());
        }
    }

    /** Append a new room record to the Rooms file (5 CSV fields). */
    public void createNewRoom(String roomNumber, int capacity, double price, String type, String status) {
        try (Writer writer = utf8Writer(ROOMS_FILE, true)) {
            writer.write(roomNumber + "," + capacity + "," + price + "," + type + "," + status + "\n");
        } catch (IOException e) {
            writeErrors("Error writing room: " + e.getMessage());
        }
    }

    /** Append a new booking record to the Bookings file (5 CSV fields). */
    public void createNewBooking(String roomNumber, String checkIn, String checkOut,
                                  String username, String status) {
        try (Writer writer = utf8Writer(BOOKINGS_FILE, true)) {
            writer.write(roomNumber + "," + checkIn + "," + checkOut + ","
                    + username + "," + status + "\n");
        } catch (IOException e) {
            writeErrors("Error writing booking: " + e.getMessage());
        }
    }

    /** Append a single new user record to the Users file (7 CSV fields). */
    public void updateUsersFile(String username, String firstName, String lastName,
                                 String email, String hashedPassword, String salt, String role) {
        try (Writer writer = utf8Writer(USERS_FILE, true)) {
            writer.write(username + "," + firstName + "," + lastName + "," + email + ","
                    + hashedPassword + "," + salt + "," + role + "\n");
        } catch (IOException e) {
            writeErrors("Error writing user: " + e.getMessage());
        }
    }

    /** Return true if an admin account already exists in the Users file. */
    public boolean adminExists() {
        if (!USERS_FILE.exists() || USERS_FILE.length() == 0) return false;
        try (Scanner scanner = utf8Scanner(USERS_FILE)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("admin,")) return true;
            }
        } catch (IOException ignored) { /* treat as not found */ }
        return false;
    }

    /**
     * Load all user accounts — strict 7-field CSV:
     * {@code username,firstName,lastName,email,hashedPasswordB64,saltB64,role}.
     * Malformed lines are logged and skipped.
     */
    public void getUsers(List<Account> users) {
        users.clear();
        if (!USERS_FILE.exists() || USERS_FILE.length() == 0) return;
        int lineNo = 0;
        try (Scanner scanner = utf8Scanner(USERS_FILE)) {
            while (scanner.hasNextLine()) {
                lineNo++;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length != 7) {
                    writeErrors("Skipping malformed user line " + lineNo
                            + " (expected 7 fields, got " + fields.length + "): " + line);
                    continue;
                }
                try {
                    byte[] hashedPasswordBytes = Base64.getDecoder().decode(fields[4].trim());
                    byte[] saltBytes           = Base64.getDecoder().decode(fields[5].trim());
                    users.add(new Account(fields[0].trim(), fields[1].trim(), fields[2].trim(),
                            fields[3].trim(), hashedPasswordBytes, saltBytes, fields[6].trim()));
                } catch (IllegalArgumentException e) {
                    writeErrors("Skipping user line " + lineNo + " (bad base64): " + line);
                }
            }
        } catch (IOException e) {
            writeErrors("Error reading Users file: " + e.getMessage());
        }
    }

    /** Create the Errors log file if it does not already exist. */
    public void errorLogging() {
        try {
            if (!ERRORS_FILE.exists()) ERRORS_FILE.createNewFile();
        } catch (IOException e) {
            System.err.println("Could not create Errors file: " + e.getMessage());
        }
    }

    /** Append an error message to the Errors log. */
    public void writeErrors(String errorMessage) {
        try (Writer writer = utf8Writer(ERRORS_FILE, true)) {
            writer.write(errorMessage + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to Errors file: " + e.getMessage());
        }
    }
}
