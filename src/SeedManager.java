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
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Write the default admin account to the Users file.
     * Called automatically by Main when no admin record exists.
     */
    public static void seedAdmin(Files file) {
        try {
            byte[] saltBytes = new byte[16];
            RANDOM.nextBytes(saltBytes);

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(saltBytes);
            byte[] hashBytes = md.digest(
                    DEFAULT_ADMIN_PASSWORD.getBytes(StandardCharsets.UTF_8));

            String b64Hash = Base64.getEncoder().encodeToString(hashBytes);
            String b64Salt = Base64.getEncoder().encodeToString(saltBytes);

            file.updateUsersFile("admin", "Admin", "User",
                    "admin@hotel.com", b64Hash, b64Salt, "MANAGER");
        } catch (Exception e) {
            System.err.println("Failed to seed admin account: " + e.getMessage());
        }
    }

    // ── Standalone CLI tool for generating additional staff accounts ─────
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.print("Choose a manager password: ");
        String managerPassword = sc.nextLine().trim();
        printUserLine("admin", "Admin", "User", "admin@hotel.com", managerPassword, "MANAGER");

        System.out.print("\nAdd a receptionist account too? (yes/no): ");
        if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.print("Receptionist username: ");
            String receptionUsername = sc.nextLine().trim();
            System.out.print("Receptionist password: ");
            String receptionPassword = sc.nextLine().trim();
            printUserLine(receptionUsername, "Front", "Desk",
                    "reception@hotel.com", receptionPassword, "RECEPTION");
        }

        System.out.println("\nCopy the line(s) above into your Users file, or re-run with auto-seed.");
        sc.close();
    }

    private static void printUserLine(String username, String firstName, String lastName,
                                       String email, String password, String role) throws Exception {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(saltBytes);
        byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        System.out.println(username + "," + firstName + "," + lastName + "," + email + ","
                + Base64.getEncoder().encodeToString(hashBytes) + ","
                + Base64.getEncoder().encodeToString(saltBytes) + ","
                + role);
    }
}
