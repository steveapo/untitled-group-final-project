import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CLI — utility methods")
class CLITest {

    // ── Colour helpers (in CI / piped output, ANSI is disabled → plain text) ──

    @Test
    @DisplayName("colour methods return non-null strings")
    void colourMethodsReturnNonNull() {
        assertNotNull(CLI.red("test"));
        assertNotNull(CLI.green("test"));
        assertNotNull(CLI.yellow("test"));
        assertNotNull(CLI.blue("test"));
        assertNotNull(CLI.cyan("test"));
        assertNotNull(CLI.magenta("test"));
        assertNotNull(CLI.white("test"));
        assertNotNull(CLI.bold("test"));
        assertNotNull(CLI.dim("test"));
        assertNotNull(CLI.success("test"));
        assertNotNull(CLI.error("test"));
        assertNotNull(CLI.warning("test"));
        assertNotNull(CLI.header("test"));
        assertNotNull(CLI.prompt("test"));
    }

    @Test
    @DisplayName("colour methods always contain the original text")
    void colourMethodsContainOriginalText() {
        String text = "hello world";
        assertTrue(CLI.red(text).contains(text));
        assertTrue(CLI.green(text).contains(text));
        assertTrue(CLI.yellow(text).contains(text));
        assertTrue(CLI.blue(text).contains(text));
        assertTrue(CLI.cyan(text).contains(text));
        assertTrue(CLI.magenta(text).contains(text));
        assertTrue(CLI.white(text).contains(text));
        assertTrue(CLI.bold(text).contains(text));
        assertTrue(CLI.dim(text).contains(text));
        assertTrue(CLI.success(text).contains(text));
        assertTrue(CLI.error(text).contains(text));
        assertTrue(CLI.warning(text).contains(text));
        assertTrue(CLI.header(text).contains(text));
        assertTrue(CLI.prompt(text).contains(text));
    }

    @Test
    @DisplayName("colour with empty string returns non-null")
    void colourWithEmptyString() {
        assertNotNull(CLI.red(""));
        assertNotNull(CLI.bold(""));
    }

    // ── In CI (no terminal), ANSI is disabled → returns plain text ─────

    @Test
    @DisplayName("in non-ANSI environment, colour returns plain text")
    void nonAnsiReturnsPlainText() {
        // When running in CI/tests, System.console() is null → ANSI disabled
        // so the colour helpers should return the plain text (no escape codes)
        if (System.console() == null) {
            assertEquals("test", CLI.red("test"));
            assertEquals("test", CLI.bold("test"));
            // success() always prepends the ✔ glyph; only the colour codes are
            // suppressed when ANSI is unavailable.
            assertEquals("\u2714  test", CLI.success("test"));
        }
    }

    // ── randomDelayMs ────────────────────────────────────────────────────

    @Test
    @DisplayName("randomDelayMs returns value between 500 and 2000")
    void randomDelayMsInRange() {
        for (int i = 0; i < 100; i++) {
            int delay = CLI.randomDelayMs();
            assertTrue(delay >= 500 && delay <= 2000,
                    "Delay " + delay + " should be between 500 and 2000");
        }
    }

    // ── selectFromList fallback ──────────────────────────────────────────

    @Test
    @DisplayName("selectFromList returns -1 for empty labels array")
    void selectFromListEmptyLabels() {
        java.util.Scanner scanner = new java.util.Scanner("1\n");
        int result = CLI.selectFromList(new String[]{}, "Test", scanner);
        assertEquals(-1, result);
    }
}
