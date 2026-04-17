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
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Write the default admin account to the Users file.
     * Called automatically by Main when no admin record exists.
     */
    public static void seedAdmin(Files file) {
        try {
            byte[] saltBytes = new byte[16];
            SECURE_RANDOM.nextBytes(saltBytes);

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(saltBytes);
            byte[] hashBytes = md.digest(DEFAULT_ADMIN_PASSWORD.getBytes(StandardCharsets.UTF_8));

            // Encode hash and salt as Base64 strings for CSV file storage
            String encodedHash = Base64.getEncoder().encodeToString(hashBytes);
            String encodedSalt = Base64.getEncoder().encodeToString(saltBytes);

            file.updateUsersFile(DEFAULT_ADMIN_USERNAME, "Admin", "User",
                    "admin@hotel.com", encodedHash, encodedSalt, "MANAGER");
        } catch (Exception e) {
            System.err.println("Failed to seed admin account: " + e.getMessage());
        }
    }

    /**
     * Create and seed the Rooms file with default rooms.
     * Called automatically by Main on first run.
     */
    public static void seedRooms(Files file) {
        try {
            file.createRoomsFile();
            file.createNewRoom("R101", 1, 59.99, "Single", "AVAILABLE");
            file.createNewRoom("R102", 2, 89.99, "Double", "AVAILABLE");
            file.createNewRoom("R103", 3, 119.99, "Triple", "AVAILABLE");
            file.createNewRoom("R104", 4, 149.99, "Quad", "AVAILABLE");
            file.createNewRoom("R105", 2, 199.99, "Suite", "AVAILABLE");
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
