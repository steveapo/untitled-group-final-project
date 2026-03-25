import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

public class Account {

    // Default credential for the seeded admin — change after first login
    @SuppressWarnings("java:S2068")
    private static final String ADMIN_USERNAME = "admin";
    @SuppressWarnings("java:S2068")
    private static final String ADMIN_PASSWORD = "admin";
    private static final String LINE_SUFFIX    = " - Line: ";

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private byte[] hashedPassword;
    private byte[] salt;
    private String role;  // "USER", "RECEPTION", "MANAGER"

    public Account() {}

    public Account(String username, String firstName, String lastName,
                   String email, byte[] hashedPassword, byte[] salt) {
        this.username       = username;
        this.firstName      = firstName;
        this.lastName       = lastName;
        this.email          = email;
        this.hashedPassword = hashedPassword;
        this.salt           = salt;
        this.role           = "USER";
    }

    public Account(String username, String firstName, String lastName,
                   String email, byte[] hashedPassword, byte[] salt, String role) {
        this.username       = username;
        this.firstName      = firstName;
        this.lastName       = lastName;
        this.email          = email;
        this.hashedPassword = hashedPassword;
        this.salt           = salt;
        this.role           = role;
    }

    public String getUsername()        { return username; }
    public String getFirstName()       { return firstName; }
    public String getLastName()        { return lastName; }
    public String getEmail()           { return email; }
    public byte[] getHashed_password() { return hashedPassword; }
    public byte[] getSalt()            { return salt; }
    public String getRole()            { return role; }

    public void setUsername(String value)        { this.username = value; }
    public void setFirstName(String value)       { this.firstName = value; }
    public void setLastName(String value)        { this.lastName = value; }
    public void setEmail(String value)           { this.email = value; }
    public void setPassword(byte[] value)        { this.hashedPassword = value; }
    public void setSalt(byte[] value)            { this.salt = value; }
    public void setRole(String value)            { this.role = value; }

    // Registration — new users always get "USER" role.
    // Returns false if the user typed 'e' to cancel at any prompt.
    public boolean register(Vector<Account> users, Scanner scanner) throws Exception {
        Files file = new Files();

        String newUsername = checkUsername(users, scanner);
        if (newUsername == null) return false;

        String newFirstName;
        while (true) {
            System.out.println("Enter your first name (or 'e' to cancel): ");
            newFirstName = scanner.nextLine().trim();
            if (newFirstName.equalsIgnoreCase("e")) return false;
            if (newFirstName.matches("[A-Za-z]+")) break;
            System.err.println("Names cannot have numbers.");
            file.writeErrors("Names cannot have numbers - " + getClass() + LINE_SUFFIX
                    + Thread.currentThread().getStackTrace()[1].getLineNumber());
        }

        String newLastName;
        while (true) {
            System.out.println("Enter your last name (or 'e' to cancel): ");
            newLastName = scanner.nextLine().trim();
            if (newLastName.equalsIgnoreCase("e")) return false;
            if (newLastName.matches("[A-Za-z]+")) break;
            System.err.println("Surnames cannot have numbers.");
            file.writeErrors("Surnames cannot have numbers - " + getClass() + LINE_SUFFIX
                    + Thread.currentThread().getStackTrace()[1].getLineNumber());
        }

        String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        String newEmail;
        while (true) {
            System.out.println("Enter your email (or 'e' to cancel): ");
            newEmail = scanner.nextLine().trim();
            if (newEmail.equalsIgnoreCase("e")) return false;
            if (Pattern.matches(emailRegex, newEmail)) break;
            System.err.println("Invalid email. Please try again.");
            file.writeErrors("Invalid email - " + getClass() + LINE_SUFFIX
                    + Thread.currentThread().getStackTrace()[1].getLineNumber());
        }

        String[] hashAndSalt = hashPassword(scanner);
        if (hashAndSalt.length == 0) return false;

        System.out.println("Registration complete.");
        System.out.println("Name: " + newFirstName + " " + newLastName + "  |  Email: " + newEmail);
        file.createUsersFile();
        file.updateUsersFile(newUsername, newFirstName, newLastName, newEmail,
                hashAndSalt[0], hashAndSalt[1], "USER");
        file.getUsers(users);
        return true;
    }

    // Hash the password entered at the prompt.
    // Returns {base64Hash, base64Salt}, or an empty array if the user typed 'e' to cancel.
    public String[] hashPassword(Scanner scanner) throws Exception {
        System.out.println("Enter your password (or 'e' to cancel): ");
        String inputPassword = scanner.nextLine();
        if (inputPassword.equalsIgnoreCase("e")) return new String[0];

        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(saltBytes);
        byte[] hashBytes = md.digest(inputPassword.getBytes(StandardCharsets.UTF_8));

        return new String[]{
            Base64.getEncoder().encodeToString(hashBytes),
            Base64.getEncoder().encodeToString(saltBytes)
        };
    }

    // Check username availability.
    // Returns null if the user typed 'e' to cancel.
    public String checkUsername(Vector<Account> users, Scanner scanner) {
        while (true) {
            System.out.println("Enter your username (or 'e' to cancel): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("e")) return null;

            boolean taken = false;
            for (Account user : users) {
                if (user.getUsername().equalsIgnoreCase(input)) {
                    taken = true;
                    break;
                }
            }
            if (taken) {
                System.err.println("Username already taken.");
                Files file = new Files();
                file.writeErrors("Username already taken - " + getClass() + LINE_SUFFIX
                        + Thread.currentThread().getStackTrace()[1].getLineNumber());
            } else {
                return input;
            }
        }
    }

    // Login — checks hardcoded admin first, then file-based accounts.
    // Re-asks only the password after a wrong attempt (not the username).
    // Returns null if the user types 'e' to go back.
    public Account login(Vector<Account> users, Scanner scanner) throws Exception {
        Files file = new Files();

        System.out.println("Enter your username (or 'e' to go back): ");
        String inputUsername = scanner.nextLine().trim();
        if (inputUsername.equalsIgnoreCase("e")) return null;

        // Hardcoded admin shortcut
        if (inputUsername.equals(ADMIN_USERNAME)) {
            return loginAdmin(users, scanner, file);
        }

        // File-based account lookup
        int matchIndex = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(inputUsername)) {
                matchIndex = i;
                break;
            }
        }

        if (matchIndex == -1) {
            System.out.println("Username not found.");
            return null;
        }

        return loginWithPassword(users.get(matchIndex), scanner, file);
    }

    private Account loginAdmin(Vector<Account> users, Scanner scanner, Files file) throws Exception {
        while (true) {
            System.out.println("Enter your password (or 'e' to go back): ");
            String inputPassword = scanner.nextLine();
            if (inputPassword.equalsIgnoreCase("e")) return null;
            if (inputPassword.equals(ADMIN_PASSWORD)) {
                for (Account user : users) {
                    if (user.getUsername().equals(ADMIN_USERNAME)) return user;
                }
            }
            System.err.println("Wrong password. Try again.");
            file.writeErrors("Wrong admin password attempt - " + getClass());
        }
    }

    private Account loginWithPassword(Account matched, Scanner scanner, Files file) throws Exception {
        while (true) {
            System.out.println("Enter your password (or 'e' to go back): ");
            String inputPassword = scanner.nextLine();
            if (inputPassword.equalsIgnoreCase("e")) return null;

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(matched.getSalt());
            byte[] attemptHash = md.digest(inputPassword.getBytes(StandardCharsets.UTF_8));

            if (MessageDigest.isEqual(attemptHash, matched.getHashed_password())) {
                return matched;
            }
            System.err.println("Wrong password. Try again.");
            file.writeErrors("Wrong password - " + getClass() + " - user: " + matched.getUsername());
        }
    }

    @Override
    public String toString() {
        return username + "," + firstName + "," + lastName + "," + email + ","
                + Base64.getEncoder().encodeToString(hashedPassword) + ","
                + Base64.getEncoder().encodeToString(salt) + ","
                + role;
    }
}
