import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeedManager")
class SeedManagerTest {

    @TempDir
    Path tempDir;

    private Files files;

    @BeforeEach
    void setUp() {
        files = new Files(tempDir.toFile());
    }

    private void writeFile(String name, String content) throws IOException {
        try (FileWriter w = new FileWriter(new File(tempDir.toFile(), name))) {
            w.write(content);
        }
    }

    private String readFile(String name) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(new File(tempDir.toFile(), name)))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }

    @Test
    @DisplayName("seedAdmin creates admin record in Users file")
    void seedAdminCreatesRecord() throws IOException {
        writeFile("Users", "");
        SeedManager.seedAdmin(files);

        String content = readFile("Users");
        assertTrue(content.startsWith("admin,Admin,User,admin@hotel.com,"),
                "Admin record should start with expected fields");
        assertTrue(content.contains(",MANAGER"), "Admin should have MANAGER role");
    }

    @Test
    @DisplayName("seedAdmin password hash is valid SHA-512")
    void seedAdminHashIsValid() throws Exception {
        writeFile("Users", "");
        SeedManager.seedAdmin(files);

        String content = readFile("Users").trim();
        String[] fields = content.split(",");
        assertEquals(7, fields.length, "Should have 7 CSV fields");

        byte[] hash = Base64.getDecoder().decode(fields[4]);
        byte[] salt = Base64.getDecoder().decode(fields[5]);

        assertEquals(64, hash.length, "SHA-512 hash should be 64 bytes");
        assertEquals(16, salt.length, "Salt should be 16 bytes");

        // Verify the hash matches "admin" password
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] expectedHash = md.digest("admin".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(expectedHash, hash, "Hash should match 'admin' password");
    }

    @Test
    @DisplayName("seedAdmin can be loaded back by getUsers")
    void seedAdminRoundTrips() throws IOException {
        writeFile("Users", "");
        SeedManager.seedAdmin(files);

        Vector<Account> users = new Vector<>();
        files.getUsers(users);

        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).getUsername());
        assertEquals("Admin", users.get(0).getFirstName());
        assertEquals("User", users.get(0).getLastName());
        assertEquals("admin@hotel.com", users.get(0).getEmail());
        assertEquals("MANAGER", users.get(0).getRole());
    }

    @Test
    @DisplayName("multiple seedAdmin calls append multiple records")
    void multipleSeedAdminCalls() throws IOException {
        writeFile("Users", "");
        SeedManager.seedAdmin(files);
        SeedManager.seedAdmin(files);

        Vector<Account> users = new Vector<>();
        files.getUsers(users);

        assertEquals(2, users.size(), "Each seedAdmin call appends a new record");
    }
}
