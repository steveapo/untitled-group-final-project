import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Files class.
 *
 * The Files class reads/writes files relative to the working directory,
 * so each test changes the working dir to a temp folder to avoid polluting
 * the real data files. We restore the original directory after each test.
 */
@DisplayName("Files — I/O operations")
class FilesTest {

    @TempDir
    Path tempDir;

    private Files files;

    @BeforeEach
    void setUp() {
        files = new Files(tempDir.toFile());
    }

    // ── Helper: write content to a file in tempDir ───────────────────────
    private void writeFile(String name, String content) throws IOException {
        File f = new File(tempDir.toFile(), name);
        try (FileWriter w = new FileWriter(f)) {
            w.write(content);
        }
    }

    private String readFile(String name) throws IOException {
        File f = new File(tempDir.toFile(), name);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    // ── populateRooms ────────────────────────────────────────────────────

    @Test
    @DisplayName("populateRooms loads rooms from CSV file")
    void populateRoomsLoadsCSV() throws IOException {
        writeFile("Rooms",
                "R101,1,70.0,Single,AVAILABLE\nR102,2,90.0,Double,MAINTENANCE\n");

        Vector<Room> rooms = new Vector<>();
        files.populateRooms(rooms);

        assertEquals(2, rooms.size());
        assertEquals("R101", rooms.get(0).getRoomNumber());
        assertEquals(1, rooms.get(0).getCapacity());
        assertEquals(70.0, rooms.get(0).getPrice());
        assertEquals("Single", rooms.get(0).getType());
        assertEquals("AVAILABLE", rooms.get(0).getStatus());

        assertEquals("R102", rooms.get(1).getRoomNumber());
        assertEquals("MAINTENANCE", rooms.get(1).getStatus());
    }

    @Test
    @DisplayName("populateRooms skips empty lines")
    void populateRoomsSkipsEmptyLines() throws IOException {
        writeFile("Rooms", "R101,1,70.0,Single,AVAILABLE\n\n\nR102,2,90.0,Double,AVAILABLE\n");

        Vector<Room> rooms = new Vector<>();
        files.populateRooms(rooms);

        assertEquals(2, rooms.size());
    }

    @Test
    @DisplayName("populateRooms defaults type and status for short lines")
    void populateRoomsDefaultsForShortLines() throws IOException {
        writeFile("Rooms", "R101,1,70.0\n");

        Vector<Room> rooms = new Vector<>();
        files.populateRooms(rooms);

        assertEquals(1, rooms.size());
        assertEquals("Single", rooms.get(0).getType());
        assertEquals("AVAILABLE", rooms.get(0).getStatus());
    }

    // ── populateBookings ─────────────────────────────────────────────────

    @Test
    @DisplayName("populateBookings loads bookings and links to room objects")
    void populateBookingsLinksRooms() throws IOException {
        writeFile("Rooms", "R101,1,70.0,Single,AVAILABLE\n");
        writeFile("Bookings", "R101,01-05-2026,05-05-2026,alice,CONFIRMED\n");

        Vector<Room> rooms = new Vector<>();
        files.populateRooms(rooms);
        Vector<Bookings> bookings = new Vector<>();
        files.populateBookings(rooms, bookings);

        assertEquals(1, bookings.size());
        assertSame(rooms.get(0), bookings.get(0).getRoom());
        assertEquals("01-05-2026", bookings.get(0).getCheckIn());
        assertEquals("05-05-2026", bookings.get(0).getCheckOut());
        assertEquals("alice", bookings.get(0).getUsername());
        assertEquals("CONFIRMED", bookings.get(0).getStatus());
    }

    @Test
    @DisplayName("populateBookings defaults username and status for short lines")
    void populateBookingsDefaultsShortLines() throws IOException {
        writeFile("Rooms", "R101,1,70.0,Single,AVAILABLE\n");
        writeFile("Bookings", "R101,01-05-2026,05-05-2026\n");

        Vector<Room> rooms = new Vector<>();
        files.populateRooms(rooms);
        Vector<Bookings> bookings = new Vector<>();
        files.populateBookings(rooms, bookings);

        assertEquals(1, bookings.size());
        assertEquals("unknown", bookings.get(0).getUsername());
        assertEquals("CONFIRMED", bookings.get(0).getStatus());
    }

    @Test
    @DisplayName("populateBookings ignores bookings for non-existent rooms")
    void populateBookingsIgnoresOrphanRooms() throws IOException {
        writeFile("Rooms", "R101,1,70.0,Single,AVAILABLE\n");
        writeFile("Bookings", "R999,01-05-2026,05-05-2026,bob,CONFIRMED\n");

        Vector<Room> rooms = new Vector<>();
        files.populateRooms(rooms);
        Vector<Bookings> bookings = new Vector<>();
        files.populateBookings(rooms, bookings);

        assertTrue(bookings.isEmpty(), "Booking for non-existent room should be skipped");
    }

    // ── updateRooms ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateRooms writes all rooms to Rooms file")
    void updateRoomsWritesFile() throws IOException {
        // Create Rooms file first so Files can write to it
        writeFile("Rooms", "");

        Vector<Room> rooms = new Vector<>();
        rooms.add(new Room("R201", 2, 100.0, "Double", "AVAILABLE"));
        rooms.add(new Room("R202", 3, 130.0, "Triple", "MAINTENANCE"));
        files.updateRooms(rooms);

        String content = readFile("Rooms");
        assertTrue(content.contains("R201,2,100.0,Double,AVAILABLE"));
        assertTrue(content.contains("R202,3,130.0,Triple,MAINTENANCE"));
    }

    // ── updateBookings ───────────────────────────────────────────────────

    @Test
    @DisplayName("updateBookings writes all bookings to Bookings file")
    void updateBookingsWritesFile() throws IOException {
        writeFile("Bookings", "");

        Room room = new Room("R101", 1, 70.0, "Single", "AVAILABLE");
        Vector<Bookings> bookings = new Vector<>();
        bookings.add(new Bookings(room, "01-05-2026", "05-05-2026", "alice", "CONFIRMED"));
        bookings.add(new Bookings(room, "10-05-2026", "15-05-2026", "bob", "CANCELLED"));
        files.updateBookings(bookings);

        String content = readFile("Bookings");
        assertTrue(content.contains("R101,01-05-2026,05-05-2026,alice,CONFIRMED"));
        assertTrue(content.contains("R101,10-05-2026,15-05-2026,bob,CANCELLED"));
    }

    // ── Users file operations ────────────────────────────────────────────

    @Test
    @DisplayName("createUsersFile creates Users file if absent")
    void createUsersFileCreatesFile() {
        files.createUsersFile();
        assertTrue(new File(tempDir.toFile(), "Users").exists());
    }

    @Test
    @DisplayName("updateUsersFile appends a user record")
    void updateUsersFileAppends() throws IOException {
        writeFile("Users", "");
        files.updateUsersFile("alice", "Alice", "Smith", "alice@test.com", "hash1", "salt1", "USER");
        files.updateUsersFile("bob", "Bob", "Jones", "bob@test.com", "hash2", "salt2", "RECEPTION");

        String content = readFile("Users");
        assertTrue(content.contains("alice,Alice,Smith,alice@test.com,hash1,salt1,USER"));
        assertTrue(content.contains("bob,Bob,Jones,bob@test.com,hash2,salt2,RECEPTION"));
    }

    @Test
    @DisplayName("getUsers loads accounts from Users file")
    void getUsersLoadsAccounts() throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] hash = md.digest("pass".getBytes(StandardCharsets.UTF_8));
        String b64Hash = Base64.getEncoder().encodeToString(hash);
        String b64Salt = Base64.getEncoder().encodeToString(salt);

        writeFile("Users", "alice,Alice,Smith,alice@test.com," + b64Hash + "," + b64Salt + ",USER\n");

        Vector<Account> users = new Vector<>();
        files.getUsers(users);

        assertEquals(1, users.size());
        assertEquals("alice", users.get(0).getUsername());
        assertEquals("Alice", users.get(0).getFirstName());
        assertEquals("USER", users.get(0).getRole());
    }

    @Test
    @DisplayName("getUsers skips lines with wrong field count")
    void getUsersSkipsInvalidLines() throws Exception {
        writeFile("Users", "alice,Alice\nonly,two,fields,here\n");

        Vector<Account> users = new Vector<>();
        files.getUsers(users);

        assertTrue(users.isEmpty(), "Invalid records should be skipped");
    }

    @Test
    @DisplayName("getUsers clears vector before loading")
    void getUsersClearsExisting() throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] hash = md.digest("x".getBytes(StandardCharsets.UTF_8));
        String b64Hash = Base64.getEncoder().encodeToString(hash);
        String b64Salt = Base64.getEncoder().encodeToString(salt);

        writeFile("Users", "bob,Bob,J,b@b.com," + b64Hash + "," + b64Salt + ",USER\n");

        Vector<Account> users = new Vector<>();
        // Pre-populate with a dummy — should be cleared
        users.add(new Account("old", "Old", "User", "old@old.com", hash, salt, "USER"));
        files.getUsers(users);

        assertEquals(1, users.size());
        assertEquals("bob", users.get(0).getUsername());
    }

    // ── adminExists ──────────────────────────────────────────────────────

    @Test
    @DisplayName("adminExists returns false when Users file is missing")
    void adminExistsFalseWhenMissing() {
        assertFalse(files.adminExists());
    }

    @Test
    @DisplayName("adminExists returns false when Users file is empty")
    void adminExistsFalseWhenEmpty() throws IOException {
        writeFile("Users", "");
        assertFalse(files.adminExists());
    }

    @Test
    @DisplayName("adminExists returns true when admin line is present")
    void adminExistsTrueWhenPresent() throws IOException {
        writeFile("Users", "admin,Admin,User,admin@hotel.com,hash,salt,MANAGER\n");
        assertTrue(files.adminExists());
    }

    @Test
    @DisplayName("adminExists returns false when no admin line")
    void adminExistsFalseForOtherUsers() throws IOException {
        writeFile("Users", "alice,Alice,Smith,alice@test.com,hash,salt,USER\n");
        assertFalse(files.adminExists());
    }

    // ── checkFile ────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkFile returns true when both data files exist")
    void checkFileReturnsTrueWhenBothExist() throws IOException {
        writeFile("Rooms", "R101,1,70.0,Single,AVAILABLE\n");
        writeFile("Bookings", "");
        assertTrue(files.checkFile());
    }

    @Test
    @DisplayName("checkFile returns false when Rooms is missing")
    void checkFileReturnsFalseWhenRoomsMissing() throws IOException {
        writeFile("Bookings", "");
        assertFalse(files.checkFile());
    }

    @Test
    @DisplayName("checkFile returns false when Bookings is missing")
    void checkFileReturnsFalseWhenBookingsMissing() throws IOException {
        writeFile("Rooms", "");
        assertFalse(files.checkFile());
    }

    // ── Error logging ────────────────────────────────────────────────────

    @Test
    @DisplayName("writeErrors appends to Errors file")
    void writeErrorsAppendsToFile() throws IOException {
        writeFile("Errors", "");
        files.writeErrors("test error 1");
        files.writeErrors("test error 2");

        String content = readFile("Errors");
        assertTrue(content.contains("test error 1"));
        assertTrue(content.contains("test error 2"));
    }

    // ── updateUsersFileAll ───────────────────────────────────────────────

    @Test
    @DisplayName("updateUsersFileAll overwrites entire Users file")
    void updateUsersFileAllOverwrites() throws Exception {
        writeFile("Users", "old,Old,Data,old@old.com,hash,salt,USER\n");

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] hash = md.digest("p".getBytes(StandardCharsets.UTF_8));

        Vector<Account> users = new Vector<>();
        users.add(new Account("new", "New", "User", "new@n.com", hash, salt, "MANAGER"));
        files.updateUsersFileAll(users);

        String content = readFile("Users");
        assertFalse(content.contains("old,Old"));
        assertTrue(content.contains("new,New,User,new@n.com"));
    }
}
