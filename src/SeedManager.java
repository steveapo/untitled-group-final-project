import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Handles seeding the initial admin and receptionist accounts.
 *
 * seedAdmin(Files) is called automatically on first run from Main — no manual steps needed.
 * The standalone main() is kept for generating additional staff accounts from the command line.
 */
public class SeedManager {

    // Hardcoded default credentials — users should change these after first login
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_RECEPTION_USERNAME = "reception";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_RECEPTION_PASSWORD = "reception";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_USER_USERNAME = "user1";
    @SuppressWarnings("java:S2068")
    private static final String DEFAULT_USER_PASSWORD = "user1";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Seed the default accounts: admin, receptionist, and user.
     * Called automatically by Main on first run.
     */
    public static void seedDefaultAccounts(Files file) {
        try {
            // Admin account
            hashAndCreateUser(file, DEFAULT_ADMIN_USERNAME, "Admin", "User",
                    "admin@hotel.com", DEFAULT_ADMIN_PASSWORD, "MANAGER");

            // Reception account
            hashAndCreateUser(file, DEFAULT_RECEPTION_USERNAME, "Reception", "Staff",
                    "reception@hotel.com", DEFAULT_RECEPTION_PASSWORD, "RECEPTION");

            // User account
            hashAndCreateUser(file, DEFAULT_USER_USERNAME, "John", "Doe",
                    "user1@example.com", DEFAULT_USER_PASSWORD, "USER");
        } catch (Exception e) {
            System.err.println("Failed to seed accounts: " + e.getMessage());
        }
    }

    /**
     * Hash password and create a user account in the Users file.
     */
    private static void hashAndCreateUser(Files file, String username, String firstName,
                                         String lastName, String email, String password, String role) throws Exception {
        byte[] saltBytes = new byte[16];
        SECURE_RANDOM.nextBytes(saltBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(saltBytes);
        byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        String encodedHash = Base64.getEncoder().encodeToString(hashBytes);
        String encodedSalt = Base64.getEncoder().encodeToString(saltBytes);

        file.updateUsersFile(username, firstName, lastName, email, encodedHash, encodedSalt, role);
    }

    /**
     * Write the default admin account to the Users file.
     * Kept for backward compatibility. Use seedDefaultAccounts() instead.
     */
    public static void seedAdmin(Files file) {
        try {
            hashAndCreateUser(file, DEFAULT_ADMIN_USERNAME, "Admin", "User",
                    "admin@hotel.com", DEFAULT_ADMIN_PASSWORD, "MANAGER");
        } catch (Exception e) {
            System.err.println("Failed to seed admin account: " + e.getMessage());
        }
    }

    /**
     * Create and seed the Rooms file with default rooms (one of each type).
     * Called automatically by Main on first run.
     */
    public static void seedRooms(Files file) {
        try {
            file.createRoomsFile();
            file.createNewRoom("R101", 1, 59.99, "Single", "AVAILABLE");
            file.createNewRoom("R102", 2, 89.99, "Double", "AVAILABLE");
            file.createNewRoom("R103", 3, 119.99, "Triple", "AVAILABLE");
            file.createNewRoom("R104", 4, 149.99, "Quad", "AVAILABLE");
        } catch (Exception e) {
            System.err.println("Failed to seed rooms: " + e.getMessage());
        }
    }

    /**
     * Create the Bookings file (empty initially).
     * Called automatically by Main on first run.
     */
    public static void seedBookings(Files file) {
        try {
            file.createBookingsFile();
        } catch (Exception e) {
            System.err.println("Failed to create bookings file: " + e.getMessage());
        }
    }

    /** Standalone CLI tool — print hashed user records to stdout for manual seeding. */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Choose a manager password: ");
        String managerPassword = scanner.nextLine().trim();
        printUserLine(DEFAULT_ADMIN_USERNAME, "Admin", "User", "admin@hotel.com", managerPassword, "MANAGER");

        System.out.print("\nAdd a receptionist account too? (yes/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.print("Receptionist username: ");
            String receptionUsername = scanner.nextLine().trim();
            System.out.print("Receptionist password: ");
            String receptionPassword = scanner.nextLine().trim();
            printUserLine(receptionUsername, "Front", "Desk",
                    "reception@hotel.com", receptionPassword, "RECEPTION");
        }

        System.out.println("\nCopy the line(s) above into your Users file, or re-run with auto-seed.");
        scanner.close();
    }

    /** Hash password with a fresh random salt and print the resulting CSV user record to stdout. */
    private static void printUserLine(String username, String firstName, String lastName,
                                       String email, String password, String role) throws Exception {
        byte[] saltBytes = new byte[16];
        SECURE_RANDOM.nextBytes(saltBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(saltBytes);
        byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        System.out.println(username + "," + firstName + "," + lastName + "," + email + ","
                + Base64.getEncoder().encodeToString(hashBytes) + ","
                + Base64.getEncoder().encodeToString(saltBytes) + ","
                + role);
    }
}
