import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account")
class AccountTest {

    // ── Helper to create a hashed account ────────────────────────────────
    private static Account createAccount(String username, String firstName, String lastName,
                                          String email, String password, String role) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

        return new Account(username, firstName, lastName, email, hash, salt, role);
    }

    // ── Constructor tests ────────────────────────────────────────────────

    @Test
    @DisplayName("no-arg constructor creates empty account")
    void noArgConstructor() {
        Account account = new Account();
        assertNull(account.getUsername());
        assertNull(account.getFirstName());
        assertNull(account.getRole());
    }

    @Test
    @DisplayName("six-arg constructor defaults role to USER")
    void sixArgConstructorDefaultsToUser() {
        byte[] hash = new byte[64];
        byte[] salt = new byte[16];
        Account account = new Account("alice", "Alice", "Smith", "alice@test.com", hash, salt);

        assertEquals("alice", account.getUsername());
        assertEquals("Alice", account.getFirstName());
        assertEquals("Smith", account.getLastName());
        assertEquals("alice@test.com", account.getEmail());
        assertEquals("USER", account.getRole());
        assertSame(hash, account.getHashedPassword());
        assertSame(salt, account.getSalt());
    }

    @Test
    @DisplayName("seven-arg constructor sets explicit role")
    void sevenArgConstructorSetsRole() throws Exception {
        Account manager = createAccount("admin", "Admin", "User", "admin@hotel.com", "secret", "MANAGER");
        assertEquals("MANAGER", manager.getRole());
    }

    // ── Getter tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getters return correct values")
    void gettersReturnValues() throws Exception {
        Account account = createAccount("bob", "Bob", "Jones", "bob@hotel.com", "pass123", "RECEPTION");

        assertEquals("bob", account.getUsername());
        assertEquals("Bob", account.getFirstName());
        assertEquals("Jones", account.getLastName());
        assertEquals("bob@hotel.com", account.getEmail());
        assertEquals("RECEPTION", account.getRole());
        assertNotNull(account.getHashedPassword());
        assertNotNull(account.getSalt());
        assertEquals(64, account.getHashedPassword().length); // SHA-512 = 64 bytes
        assertEquals(16, account.getSalt().length);
    }

    // ── Role setter ──────────────────────────────────────────────────────

    @Test
    @DisplayName("setRole changes role from USER to RECEPTION")
    void setRoleChanges() throws Exception {
        Account account = createAccount("staff", "Staff", "Member", "staff@hotel.com", "pass", "USER");
        account.setRole("RECEPTION");
        assertEquals("RECEPTION", account.getRole());
    }

    // ── Password hashing ─────────────────────────────────────────────────

    @Test
    @DisplayName("same password with different salts produces different hashes")
    void differentSaltsDifferentHashes() throws Exception {
        String password = "testPassword";
        Account a1 = createAccount("u1", "A", "B", "a@b.com", password, "USER");
        Account a2 = createAccount("u2", "C", "D", "c@d.com", password, "USER");

        assertFalse(MessageDigest.isEqual(a1.getHashedPassword(), a2.getHashedPassword()),
                "Same password with different salts should produce different hashes");
    }

    @Test
    @DisplayName("password verification succeeds with correct password")
    void passwordVerificationSucceeds() throws Exception {
        String password = "mySecret";
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] storedHash = md.digest(password.getBytes(StandardCharsets.UTF_8));

        // Re-hash with same salt to verify
        md.reset();
        md.update(salt);
        byte[] attemptHash = md.digest(password.getBytes(StandardCharsets.UTF_8));

        assertTrue(MessageDigest.isEqual(storedHash, attemptHash));
    }

    @Test
    @DisplayName("password verification fails with wrong password")
    void passwordVerificationFails() throws Exception {
        String password = "mySecret";
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] storedHash = md.digest(password.getBytes(StandardCharsets.UTF_8));

        // Attempt with wrong password
        md.reset();
        md.update(salt);
        byte[] attemptHash = md.digest("wrongPassword".getBytes(StandardCharsets.UTF_8));

        assertFalse(MessageDigest.isEqual(storedHash, attemptHash));
    }

    // ── toString / CSV serialisation ─────────────────────────────────────

    @Test
    @DisplayName("toString produces 7-field CSV line")
    void toStringProducesCSV() throws Exception {
        Account account = createAccount("alice", "Alice", "Smith", "alice@test.com", "pass", "USER");
        String csv = account.toString();
        String[] fields = csv.split(",");

        assertEquals(7, fields.length);
        assertEquals("alice", fields[0]);
        assertEquals("Alice", fields[1]);
        assertEquals("Smith", fields[2]);
        assertEquals("alice@test.com", fields[3]);
        // fields[4] = base64 hash, fields[5] = base64 salt
        assertFalse(fields[4].isEmpty());
        assertFalse(fields[5].isEmpty());
        assertEquals("USER", fields[6]);
    }

    @Test
    @DisplayName("toString base64 values can be decoded back")
    void toStringBase64Decodable() throws Exception {
        Account account = createAccount("bob", "Bob", "Jones", "bob@test.com", "secret", "MANAGER");
        String csv = account.toString();
        String[] fields = csv.split(",");

        byte[] decodedHash = Base64.getDecoder().decode(fields[4]);
        byte[] decodedSalt = Base64.getDecoder().decode(fields[5]);

        assertEquals(64, decodedHash.length); // SHA-512
        assertEquals(16, decodedSalt.length);
        assertArrayEquals(account.getHashedPassword(), decodedHash);
        assertArrayEquals(account.getSalt(), decodedSalt);
    }

    // ── checkUsername ─────────────────────────────────────────────────────

    @Test
    @DisplayName("checkUsername returns username when not taken")
    void checkUsernameReturnsNew() throws Exception {
        Vector<Account> users = new Vector<>();
        users.add(createAccount("alice", "Alice", "S", "a@b.com", "p", "USER"));

        Scanner scanner = new Scanner("bob\n");
        Account account = new Account();
        String result = account.checkUsername(users, scanner);

        assertEquals("bob", result);
    }

    @Test
    @DisplayName("checkUsername rejects taken name then accepts new one")
    void checkUsernameRejectsThenAccepts() throws Exception {
        Vector<Account> users = new Vector<>();
        users.add(createAccount("alice", "Alice", "S", "a@b.com", "p", "USER"));

        // alice (taken) → ERR + waitForKey (blank line) → bob (available)
        Scanner scanner = new Scanner("alice\n\nbob\n");
        Account account = new Account();
        String result = account.checkUsername(users, scanner);

        assertEquals("bob", result);
    }

    @Test
    @DisplayName("checkUsername returns null on cancel")
    void checkUsernameReturnsNullOnCancel() throws Exception {
        Vector<Account> users = new Vector<>();
        Scanner scanner = new Scanner("e\n");
        Account account = new Account();

        assertNull(account.checkUsername(users, scanner));
    }

    @Test
    @DisplayName("checkUsername is case-insensitive for existing usernames")
    void checkUsernameCaseInsensitive() throws Exception {
        Vector<Account> users = new Vector<>();
        users.add(createAccount("Alice", "Alice", "S", "a@b.com", "p", "USER"));

        // alice (taken case-insensitive) → ERR + waitForKey (blank line) → bob (accepted)
        Scanner scanner = new Scanner("alice\n\nbob\n");
        Account account = new Account();
        String result = account.checkUsername(users, scanner);

        assertEquals("bob", result);
    }
}
