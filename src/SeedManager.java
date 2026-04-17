import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * First-run data seeder — populates the Users, Rooms, and Bookings files with a
 * realistic demo environment so fresh installs are immediately usable.
 *
 * <p>All dates in the seed bookings are computed relative to {@link LocalDate#now()},
 * so the demo calendar always shows a current mix of past, present, and future
 * reservations no matter when the app is run.
 *
 * <p>Called automatically from {@link Main} on first run (when the respective data
 * file does not exist). There is no user-facing seed command — the shell scripts
 * that used to append extra demo data were removed because they drifted from the
 * CSV schema and corrupted the Bookings file.
 */
public class SeedManager {

    // Hardcoded default credentials — users should change these after first login.
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_ADMIN_USERNAME     = "admin";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_ADMIN_PASSWORD     = "admin";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_RECEPTION_USERNAME = "reception";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_RECEPTION_PASSWORD = "reception";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_USER_USERNAME      = "user1";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_USER_PASSWORD      = "user1";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Seed the three default accounts: admin (MANAGER), reception (RECEPTION), user1 (USER). */
    public static void seedDefaultAccounts(Files file) {
        try {
            hashAndCreateUser(file, DEFAULT_ADMIN_USERNAME, "Admin", "User",
                    "admin@hotel.com",     DEFAULT_ADMIN_PASSWORD,     "MANAGER");
            hashAndCreateUser(file, DEFAULT_RECEPTION_USERNAME, "Reception", "Staff",
                    "reception@hotel.com", DEFAULT_RECEPTION_PASSWORD, "RECEPTION");
            hashAndCreateUser(file, DEFAULT_USER_USERNAME, "John", "Doe",
                    "user1@example.com",   DEFAULT_USER_PASSWORD,      "USER");
        } catch (Exception e) {
            file.writeErrors("Failed to seed accounts: " + e.getMessage());
        }
    }

    /** Hash password with a fresh random salt, then append the user record. */
    private static void hashAndCreateUser(Files file, String username, String firstName,
                                          String lastName, String email, String password, String role)
            throws Exception {
        byte[] saltBytes = new byte[16];
        SECURE_RANDOM.nextBytes(saltBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(saltBytes);
        byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        file.updateUsersFile(username, firstName, lastName, email,
                Base64.getEncoder().encodeToString(hashBytes),
                Base64.getEncoder().encodeToString(saltBytes),
                role);
    }

    /**
     * Seed just the admin account. Thin wrapper kept for backward-compatible tests —
     * production first-run flow uses {@link #seedDefaultAccounts(Files)}.
     */
    public static void seedAdmin(Files file) {
        try {
            hashAndCreateUser(file, DEFAULT_ADMIN_USERNAME, "Admin", "User",
                    "admin@hotel.com", DEFAULT_ADMIN_PASSWORD, "MANAGER");
        } catch (Exception e) {
            file.writeErrors("Failed to seed admin: " + e.getMessage());
        }
    }

    /** Create the Rooms file and populate with the 9 demo rooms (R101–R109). */
    public static void seedRooms(Files file) {
        try {
            file.createRoomsFile();
            // Core rooms — one of each basic type
            file.createNewRoom("R101", 1, 59.99,  "Single", "AVAILABLE");
            file.createNewRoom("R102", 2, 89.99,  "Double", "AVAILABLE");
            file.createNewRoom("R103", 3, 119.99, "Triple", "AVAILABLE");
            file.createNewRoom("R104", 4, 149.99, "Quad",   "AVAILABLE");
            // Extra demo rooms
            file.createNewRoom("R105", 2, 199.99, "Suite",  "AVAILABLE");
            file.createNewRoom("R106", 1, 75.00,  "Single", "AVAILABLE");
            file.createNewRoom("R107", 3, 149.99, "Triple", "AVAILABLE");
            file.createNewRoom("R108", 4, 199.99, "Quad",   "AVAILABLE");
            file.createNewRoom("R109", 6, 299.99, "Suite",  "AVAILABLE");
        } catch (Exception e) {
            file.writeErrors("Failed to seed rooms: " + e.getMessage());
        }
    }

    /**
     * Create the Bookings file and populate it with a realistic spread of demo
     * reservations for {@code user1}. Dates are offsets from today so the demo
     * always shows a live mix of past/current/future activity.
     */
    public static void seedBookings(Files file) {
        try {
            file.createBookingsFile();
            LocalDate today = LocalDate.now();

            // Past (checked out)
            addBooking(file, "R101", today.minusDays(14), today.minusDays(11), "CHECKED_OUT");
            addBooking(file, "R103", today.minusDays(9),  today.minusDays(5),  "CHECKED_OUT");
            addBooking(file, "R106", today.minusDays(6),  today.minusDays(3),  "CHECKED_OUT");

            // Currently in-house
            addBooking(file, "R102", today.minusDays(1),  today.plusDays(2),   "CHECKED_IN");
            addBooking(file, "R105", today.minusDays(2),  today.plusDays(1),   "CHECKED_IN");

            // Upcoming
            addBooking(file, "R104", today.plusDays(3),   today.plusDays(7),   "CONFIRMED");
            addBooking(file, "R107", today.plusDays(5),   today.plusDays(9),   "CONFIRMED");
            addBooking(file, "R109", today.plusDays(10),  today.plusDays(14),  "CONFIRMED");

            // Cancelled
            addBooking(file, "R108", today.plusDays(4),   today.plusDays(6),   "CANCELLED");

            // Scheduled maintenance window (recognised by the calendar renderer)
            addBooking(file, "R103", today.plusDays(15),  today.plusDays(18),  "MAINTENANCE");
        } catch (Exception e) {
            file.writeErrors("Failed to seed bookings: " + e.getMessage());
        }
    }

    private static void addBooking(Files file, String room,
                                    LocalDate checkIn, LocalDate checkOut, String status) {
        // MAINTENANCE bookings belong to the system; everything else to the demo guest.
        String guest = "MAINTENANCE".equals(status) ? "SYSTEM" : DEFAULT_USER_USERNAME;
        file.createNewBooking(room, checkIn.format(DATE_FMT), checkOut.format(DATE_FMT), guest, status);
    }
}
