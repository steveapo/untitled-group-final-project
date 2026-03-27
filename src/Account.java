import java.util.*;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/** Represents a user account with credentials, personal details, and a role. */
public class Account {

    private static final String LINE_SUFFIX = " - Line: ";

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private byte[] hashedPassword;
    private byte[] salt;
    private String role; // "USER", "RECEPTION", "MANAGER"

    /** Default no-arg constructor used when creating a temporary account for login/register. */
    public Account() {}

    /** Construct a new USER-role account from raw credential data. */
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

    /** Construct an account with an explicit role — used when loading from file. */
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

    // ── Getters ──────────────────────────────────────────────────────────
    /** Return the account username. */
    public String getUsername()       { return username; }

    /** Return the user's first name. */
    public String getFirstName()      { return firstName; }

    /** Return the user's last name. */
    public String getLastName()       { return lastName; }

    /** Return the user's email address. */
    public String getEmail()          { return email; }

    /** Return the stored SHA-512 password hash bytes. */
    public byte[] getHashedPassword() { return hashedPassword; }

    /** Return the password salt bytes. */
    public byte[] getSalt()           { return salt; }

    /** Return the account role (USER, RECEPTION, or MANAGER). */
    public String getRole()           { return role; }

    // ── Setters ──────────────────────────────────────────────────────────
    /** Update the account role. */
    public void setRole(String role) { this.role = role; }

    // ── Business logic ───────────────────────────────────────────────────

    /**
     * Interactive registration flow — prompts for username, name, email, and password.
     * New accounts always receive the USER role.
     * Returns false if the user types 'e' to cancel at any prompt.
     */
    public boolean register(Vector<Account> users, Scanner scanner) throws Exception {
        Files file = new Files();

        String newUsername = checkUsername(users, scanner);
        if (newUsername == null) return false;

        String newFirstName;
        while (true) {
            System.out.print(CLI.prompt("Enter your first name (or 'e' to cancel): "));
            newFirstName = scanner.nextLine().trim();
            if (newFirstName.equalsIgnoreCase("e")) return false;
            if (newFirstName.matches("[A-Za-z]+")) break;
            System.err.println("Names cannot have numbers.");
            file.writeErrors("Names cannot have numbers - " + getClass() + LINE_SUFFIX
                    + Thread.currentThread().getStackTrace()[1].getLineNumber());
        }

        String newLastName;
        while (true) {
            System.out.print(CLI.prompt("Enter your last name (or 'e' to cancel): "));
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
            System.out.print(CLI.prompt("Enter your email (or 'e' to cancel): "));
            newEmail = scanner.nextLine().trim();
            if (newEmail.equalsIgnoreCase("e")) return false;
            if (Pattern.matches(emailRegex, newEmail)) break;
            System.err.println("Invalid email. Please try again.");
            file.writeErrors("Invalid email - " + getClass() + LINE_SUFFIX
                    + Thread.currentThread().getStackTrace()[1].getLineNumber());
        }

        String[] credentials = hashPassword(scanner);
        if (credentials.length == 0) return false;

        System.out.println("Registration complete.");
        System.out.println("Name: " + newFirstName + " " + newLastName + "  |  Email: " + newEmail);
        file.createUsersFile();
        file.updateUsersFile(newUsername, newFirstName, newLastName, newEmail,
                credentials[0], credentials[1], "USER");
        file.getUsers(users);
        return true;
    }

    /**
     * Hash the password entered interactively at the prompt using SHA-512 with a random salt.
     * Returns {base64Hash, base64Salt}, or an empty array if the user types 'e' to cancel.
     */
    public String[] hashPassword(Scanner scanner) throws Exception {
        System.out.print(CLI.prompt("Enter your password (or 'e' to cancel): "));
        String inputPassword = CLI.readPassword(scanner);
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

    /**
     * Prompt for a username and verify it is not already taken.
     * Returns null if the user types 'e' to cancel.
     */
    public String checkUsername(Vector<Account> users, Scanner scanner) {
        while (true) {
            System.out.print(CLI.prompt("Enter your username (or 'e' to cancel): "));
            String candidateUsername = scanner.nextLine().trim();
            if (candidateUsername.equalsIgnoreCase("e")) return null;

            boolean isAlreadyTaken = false;
            for (Account user : users) {
                if (user.getUsername().equalsIgnoreCase(candidateUsername)) {
                    isAlreadyTaken = true;
                    break;
                }
            }
            if (isAlreadyTaken) {
                System.err.println("Username already taken.");
                Files file = new Files();
                file.writeErrors("Username already taken - " + getClass() + LINE_SUFFIX
                        + Thread.currentThread().getStackTrace()[1].getLineNumber());
            } else {
                return candidateUsername;
            }
        }
    }

    /**
     * Interactive login — looks up username in the users list, then verifies password hash.
     * Re-asks only the password after a wrong attempt (not the username).
     * Returns null if the user types 'e' to go back.
     */
    public Account login(Vector<Account> users, Scanner scanner) throws Exception {
        Files file = new Files();

        System.out.print(CLI.prompt("Enter your username (or 'e' to go back): "));
        String inputUsername = scanner.nextLine().trim();
        if (inputUsername.equalsIgnoreCase("e")) return null;

        // File-based account lookup
        Account matchedAccount = null;
        for (Account user : users) {
            if (user.getUsername().equals(inputUsername)) {
                matchedAccount = user;
                break;
            }
        }

        if (matchedAccount == null) {
            System.out.println("Username not found.");
            return null;
        }

        return loginWithPassword(matchedAccount, scanner, file);
    }

    /** Hash the provided password attempt and compare against the stored hash. */
    private Account loginWithPassword(Account matchedAccount, Scanner scanner, Files file)
            throws Exception {
        while (true) {
            System.out.print(CLI.prompt("Enter your password (or 'e' to go back): "));
            String inputPassword = CLI.readPassword(scanner);
            if (inputPassword.equalsIgnoreCase("e")) return null;

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(matchedAccount.getSalt());
            byte[] attemptHash = md.digest(inputPassword.getBytes(StandardCharsets.UTF_8));

            if (MessageDigest.isEqual(attemptHash, matchedAccount.getHashedPassword())) {
                return matchedAccount;
            }
            System.err.println("Wrong password. Try again.");
            file.writeErrors("Wrong password - " + getClass() + " - user: " + matchedAccount.getUsername());
        }
    }

    /** Serialise to CSV: username,firstName,lastName,email,hashedPassword,salt,role */
    @Override
    public String toString() {
        return username + "," + firstName + "," + lastName + "," + email + ","
                + Base64.getEncoder().encodeToString(hashedPassword) + ","
                + Base64.getEncoder().encodeToString(salt) + ","
                + role;
    }
}
