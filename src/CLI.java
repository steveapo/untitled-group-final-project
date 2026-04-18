/**
 * Cross-platform CLI utilities: ANSI colours, screen clearing, progress spinners,
 * raw-mode interactive input (arrow-key menus, single-keypress choices, masked
 * passwords, ESC hotkey), and clickable hyperlinks.
 *
 * Powered by JLine 3 for true cross-OS terminal handling:
 *   - macOS / Linux: termios via JNA
 *   - Windows 10+ Terminal / PowerShell 7+: conhost VT mode auto-enabled
 *   - Legacy cmd.exe: graceful degradation (numbered fallback)
 *   - IDE consoles (IntelliJ, VS Code): "dumb" terminal — falls back to Scanner
 *
 * Colour output is automatically disabled when:
 *   - the terminal type is "dumb" (IDEs, redirected stdout)
 *   - the TERM environment variable is "dumb"
 *   - NO_COLOR is set (https://no-color.org)
 */
import java.util.List;
import java.util.Scanner;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

public class CLI {

    // ── ANSI escape codes ────────────────────────────────────────────────
    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String DIM     = "\033[2m";

    // Foreground colours
    private static final String RED     = "\033[31m";
    private static final String GREEN   = "\033[32m";
    private static final String YELLOW  = "\033[33m";
    private static final String BLUE    = "\033[34m";
    private static final String MAGENTA = "\033[35m";
    private static final String CYAN    = "\033[36m";
    private static final String WHITE   = "\033[37m";

    // ── JLine terminal singleton ────────────────────────────────────────
    private static final Terminal TERMINAL = buildTerminal();
    private static final boolean ANSI_SUPPORTED = detectAnsiSupport();


    private static Terminal buildTerminal() {
        try {
            Terminal t = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)           // never throw — fall back gracefully in IDEs
                    .encoding("UTF-8")
                    .build();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { t.close(); } catch (Exception ignored) {}
            }));
            return t;
        } catch (Exception e) {
            return null; // pure Scanner fallback
        }
    }

    private static boolean rawModeAvailable() {
        return TERMINAL != null && !"dumb".equals(TERMINAL.getType());
    }

    private static boolean detectAnsiSupport() {
        if (System.getenv("NO_COLOR") != null) return false;
        if ("dumb".equals(System.getenv("TERM"))) return false;
        if (TERMINAL == null) return false;
        // JLine returns "dumb" for IDE consoles and unsupported cmd.exe,
        // and a proper name (xterm-256color, windows, etc.) when VT is available.
        return !"dumb".equals(TERMINAL.getType());
    }

    /** Whether the current terminal supports ANSI escape codes (colours, cursor moves). */
    public static boolean supportsAnsi() { return ANSI_SUPPORTED; }

    /** Functional interface for raw-mode bodies that may throw checked exceptions. */
    @FunctionalInterface
    private interface RawAction<T> {
        T run() throws Exception;
    }

    /**
     * Run a block of code with the terminal in raw, no-echo mode and
     * guarantee restoration of the original attributes on exit.
     * Returns the action's value, or {@code fallback} if anything goes wrong.
     */
    private static <T> T withRawMode(RawAction<T> action, T fallback) {
        if (!rawModeAvailable()) return fallback;
        Attributes original = null;
        try {
            original = TERMINAL.enterRawMode();
            return action.run();
        } catch (Exception e) {
            return fallback;
        } finally {
            if (original != null) {
                try { TERMINAL.setAttributes(original); } catch (Exception ignored) {}
            }
        }
    }

    // Wrap text in an ANSI code only if the terminal supports it
    private static String ansi(String code, String text) {
        return ANSI_SUPPORTED ? code + text + RESET : text;
    }

    // ── Public colour helpers ────────────────────────────────────────────
    public static String red(String text)     { return ansi(RED,            text); }
    public static String green(String text)   { return ansi(GREEN,          text); }
    public static String yellow(String text)  { return ansi(YELLOW,         text); }
    public static String blue(String text)    { return ansi(BLUE,           text); }
    public static String cyan(String text)    { return ansi(CYAN,           text); }
    public static String magenta(String text) { return ansi(MAGENTA,        text); }
    public static String white(String text)   { return ansi(WHITE,          text); }
    public static String bold(String text)    { return ansi(BOLD,           text); }
    public static String dim(String text)     { return ansi(DIM,            text); }
    public static String success(String text) { return ansi(GREEN  + BOLD,  "\u2714  " + text); }
    public static String error(String text)   { return ansi(RED    + BOLD,  "\u2718  " + text); }
    public static String warning(String text) { return ansi(RED    + BOLD,  "\u2718  " + text); }
    public static String header(String text)  { return ansi(CYAN   + BOLD,  text); }
    public static String prompt(String text)  { return ansi(MAGENTA,        text); }

    /** Wrap text as a clickable terminal hyperlink (OSC 8 with BEL terminator). */
    public static String link(String text, String url) {
        if (!ANSI_SUPPORTED) return text;
        return "\033]8;;" + url + "\007" + ansi(CYAN + "\033[4m", text) + "\033]8;;\007";
    }

    /** Open a URL in the default browser. Works on macOS, Linux, and Windows. */
    public static void openUrl(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception ignored) {
            System.out.println(dim("  Could not open browser. Visit: " + url));
        }
    }

    // ── Screen clearing ──────────────────────────────────────────────────
    public static void clearScreen() {
        if (ANSI_SUPPORTED) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } else {
            for (int i = 0; i < 40; i++) System.out.println();
        }
    }

    // ── Spinner / loader ─────────────────────────────────────────────────
    private static final String[] SPINNER_FRAMES = { "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };
    private static final String[] SPINNER_ASCII  = { "|", "/", "-", "\\" };

    public static void withSpinner(String label, Runnable task) {
        Thread worker = new Thread(task);
        worker.start();

        String[] frames = ANSI_SUPPORTED ? SPINNER_FRAMES : SPINNER_ASCII;
        int frameIndex = 0;

        while (worker.isAlive()) {
            String frame = ANSI_SUPPORTED
                ? cyan(frames[frameIndex % frames.length])
                : frames[frameIndex % frames.length];

            System.out.print("\r" + frame + "  " + label + "...");
            System.out.flush();
            frameIndex++;
            try { Thread.sleep(80); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.print("\r" + ansi(GREEN + BOLD, "\u2714") + "  " + label + "   \n");
        System.out.flush();
    }

    public static void spinner(String label, int durationMs) {
        withSpinner(label, () -> {
            try { Thread.sleep(durationMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
    }

    private static final java.util.Random RANDOM = new java.util.Random();

    public static int randomDelayMs() {
        return 500 + RANDOM.nextInt(1501);
    }

    public static void randomSpinner(String label) {
        spinner(label, randomDelayMs());
    }

    // ── Password input ──────────────────────────────────────────────────
    /**
     * Read a password while masking each keystroke with '*'.
     * Returns the password, or {@code null} if the user pressed ESC / Ctrl+C.
     */
    public static String readPassword(Scanner fallbackScanner) {
        String result = withRawMode(() -> {
            NonBlockingReader reader = TERMINAL.reader();
            StringBuilder pw = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == '\r' || ch == '\n' || ch == -1) {
                    TERMINAL.writer().println();
                    TERMINAL.writer().flush();
                    return pw.toString();
                } else if (ch == 27) { // ESC
                    int next = reader.read(50L);
                    if (next == -2) { // standalone ESC
                        TERMINAL.writer().println();
                        TERMINAL.writer().flush();
                        return null;
                    }
                    while (reader.read(10L) >= 0) { /* drain CSI sequence */ }
                } else if (ch == 127 || ch == 8) { // backspace
                    if (pw.length() > 0) {
                        pw.deleteCharAt(pw.length() - 1);
                        TERMINAL.writer().print("\b \b");
                        TERMINAL.writer().flush();
                    }
                } else if (ch == 3) { // Ctrl+C
                    TERMINAL.writer().println();
                    TERMINAL.writer().flush();
                    return null;
                } else if (ch >= 32) {
                    pw.append((char) ch);
                    TERMINAL.writer().print('*');
                    TERMINAL.writer().flush();
                }
            }
        }, "__FALLBACK__");

        if (!"__FALLBACK__".equals(result)) return result;

        // IDE fallback: use Console if present, else plain Scanner
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword();
            if (chars == null) return null;
            String pwd = new String(chars);
            if (pwd.equalsIgnoreCase("e")) return null;
            return pwd;
        }
        String line = fallbackScanner.nextLine();
        if (line.equalsIgnoreCase("e")) return null;
        return line;
    }

    // ── Interactive list selector ──────────────────────────────────────
    public static int selectFromList(String[] labels, String title, Scanner scanner) {
        return selectFromList(labels, title, scanner, 0);
    }

    public static int selectFromList(String[] labels, String title, Scanner scanner, int initialSelection) {
        if (labels.length == 0) return -1;

        if (rawModeAvailable()) {
            final int[] selectedHolder = { Math.max(0, Math.min(initialSelection, labels.length - 1)) };
            Integer raw = withRawMode(() -> {
                NonBlockingReader reader = TERMINAL.reader();
                int selected = selectedHolder[0];
                renderList(labels, title, selected);
                while (true) {
                    int ch = reader.read();
                    if (ch == '\r' || ch == '\n') {
                        System.out.println();
                        return selected;
                    } else if (ch == 27) { // ESC / VT100 arrow prefix
                        int next = reader.read(150L);
                        if (next == -2) { // standalone ESC — cancel
                            System.out.println();
                            return -1;
                        }
                        if (next == '[') {
                            int arrow = reader.read(150L);
                            if (arrow == 'A')      selected = (selected - 1 + labels.length) % labels.length;
                            else if (arrow == 'B') selected = (selected + 1) % labels.length;
                            while (reader.read(10L) >= 0) { /* drain */ }
                        } else {
                            while (reader.read(10L) >= 0) { /* drain unknown sequence */ }
                        }
                    } else if (ch == 0xE0) { // Windows conhost arrow prefix
                        int arrow = reader.read(150L);
                        if (arrow == 0x48)      selected = (selected - 1 + labels.length) % labels.length;
                        else if (arrow == 0x50) selected = (selected + 1) % labels.length;
                    } else {
                        continue;
                    }
                    int linesToClear = labels.length + 2;
                    System.out.print("\033[" + linesToClear + "A");
                    renderList(labels, title, selected);
                }
            }, null);
            if (raw != null) return raw;
        }

        // Fallback: numbered selection (legacy cmd.exe / IDEs)
        System.out.println("  " + header(title));
        System.out.println();
        for (int i = 0; i < labels.length; i++) {
            System.out.println("  " + cyan((i + 1) + ".") + " " + labels[i]);
        }
        System.out.print(prompt("Choice (1-" + labels.length + ", or 'e' to cancel): "));
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("e")) return -1;
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= labels.length) return choice - 1;
        } catch (NumberFormatException e) { /* fall through */ }
        System.out.println(warning("Invalid selection."));
        return -1;
    }

    /** Render the selector list with the currently highlighted item in magenta. */
    private static void renderList(String[] labels, String title, int selected) {
        System.out.println();
        for (int i = 0; i < labels.length; i++) {
            String prefix = (i == selected) ? magenta("  ▸ ") : dim("    ");
            String label  = (i == selected) ? magenta(labels[i]) : "  " + labels[i];
            System.out.println(prefix + label + "\033[K");
        }
        System.out.println(dim("  ↑↓ Navigate  Enter Select  Esc Cancel") + "\033[K");
    }

    // ── Interactive room selector ───────────────────────────────────────

    /**
     * Arrow-key room selector with magenta highlight.
     * Shows room number, type, price, capacity, and status dot.
     * Returns selected Room or null on ESC.
     */
    public static Room selectRoom(List<Room> rooms, String title, Scanner scanner) {
        return selectRoom(rooms, title, scanner, 0);
    }

    public static Room selectRoom(List<Room> rooms, String title, Scanner scanner, int initialSelection) {
        if (rooms.isEmpty()) return null;

        if (rawModeAvailable()) {
            final int[] selectedHolder = { Math.max(0, Math.min(initialSelection, rooms.size() - 1)) };
            // Save cursor position, render, then restore+clear on each update
            Integer raw = withRawMode(() -> {
                NonBlockingReader reader = TERMINAL.reader();
                int selected = selectedHolder[0];
                // Save cursor position before first render
                System.out.print("\033[s");
                renderRoomList(rooms, title, selected);
                while (true) {
                    int ch = reader.read();
                    if (ch == '\r' || ch == '\n') {
                        System.out.println();
                        return selected;
                    } else if (ch == 27) { // ESC / VT100 arrow prefix
                        int next = reader.read(150L);
                        if (next == -2) {
                            System.out.println();
                            return -1;
                        }
                        if (next == '[') {
                            int arrow = reader.read(150L);
                            if (arrow == 'A')      selected = (selected - 1 + rooms.size()) % rooms.size();
                            else if (arrow == 'B') selected = (selected + 1) % rooms.size();
                            while (reader.read(10L) >= 0) { /* drain */ }
                        } else {
                            while (reader.read(10L) >= 0) { /* drain */ }
                        }
                    } else if (ch == 0xE0) { // Windows conhost arrow prefix
                        int arrow = reader.read(150L);
                        if (arrow == 0x48)      selected = (selected - 1 + rooms.size()) % rooms.size();
                        else if (arrow == 0x50) selected = (selected + 1) % rooms.size();
                    } else if (ch == 'k') {
                        selected = (selected - 1 + rooms.size()) % rooms.size();
                    } else if (ch == 'j') {
                        selected = (selected + 1) % rooms.size();
                    } else {
                        continue;
                    }
                    System.out.print("\033[u\033[J");
                    renderRoomList(rooms, title, selected);
                }
            }, null);
            if (raw != null && raw >= 0) return rooms.get(raw);
            if (raw != null) return null; // cancelled
        }

        // Fallback: numbered selection
        System.out.println("  " + header(title));
        System.out.println();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            String dot = r.getStatus().equals("AVAILABLE") ? green("●") : red("●");
            System.out.printf("  %s %s  %-6s | %-8s | %s/night | %dp | %s%n",
                    cyan((i + 1) + "."), dot,
                    bold(r.getRoomNumber()), r.getType(),
                    yellow(String.format("$%.2f", r.getPrice())),
                    r.getCapacity(),
                    UserMenu.statusColour(r.getStatus()));
        }
        System.out.print(prompt("Choice (1-" + rooms.size() + ", or 'e' to cancel): "));
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("e")) return null;
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= rooms.size()) return rooms.get(choice - 1);
        } catch (NumberFormatException e) { /* fall through */ }
        System.out.println(warning("Invalid selection."));
        return null;
    }

    /** Render the room selector list with the currently highlighted item in magenta. */
    private static void renderRoomList(List<Room> rooms, String title, int selected) {
        System.out.println();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            String dot = r.getStatus().equals("AVAILABLE") ? green("●") : red("●");
            // Keep info short to avoid terminal line wrapping
            String info = String.format("%-5s %-6s $%.0f/n %dp",
                    r.getRoomNumber(), r.getType(),
                    r.getPrice(), r.getCapacity());
            if (i == selected) {
                System.out.println(magenta("  ▸ ") + dot + " " + magenta(info) + "\033[K");
            } else {
                System.out.println(dim("    ") + dot + " " + info + "\033[K");
            }
        }
        System.out.println(dim("  ↑↓ Navigate  Enter Select  Esc Cancel") + "\033[K");
    }

    // ── Banner / divider helpers ─────────────────────────────────────────
    public static void printBanner(String title) {
        String line = "═".repeat(38);
        System.out.println(header("╔" + line + "╗"));
        int padding = (38 - title.length()) / 2;
        String padded = " ".repeat(Math.max(0, padding)) + title
                      + " ".repeat(Math.max(0, 38 - padding - title.length()));
        System.out.println(header("║") + bold(padded) + header("║"));
        System.out.println(header("╚" + line + "╝"));
    }

    public static void printDivider() {
        System.out.println(dim("─────────────────────────────────────────"));
    }

    public static void printMenuItem(String number, String label) {
        System.out.println("  " + cyan(number + ".") + " " + label);
    }

    // ── Hotkey footer ───────────────────────────────────────────────────
    public static void printFooter(String backLabel) {
        printDivider();
        System.out.println(dim("  [Esc] " + backLabel));
    }

    // ── Wait for any keypress ───────────────────────────────────────────
    public static void waitForKey(Scanner fallbackScanner) {
        System.out.print(dim("  Press any key to continue..."));
        System.out.flush();
        Boolean handled = withRawMode(() -> {
            NonBlockingReader reader = TERMINAL.reader();
            try {
                reader.read();
                while (reader.read(10L) >= 0) { /* drain trailing escape bytes */ }
            } catch (Exception ignored) {}
            TERMINAL.writer().println();
            TERMINAL.writer().flush();
            return Boolean.TRUE;
        }, Boolean.FALSE);
        if (!handled) fallbackScanner.nextLine();
    }

    // ── Raw-mode menu choice reader ─────────────────────────────────────
    /**
     * Read a menu choice as a single keypress (no Enter needed).
     * Returns "1"–"9" for digit keys, {@code "C"} for the Calendar hotkey,
     * or {@code "ESC"} when Escape is pressed.
     *
     * <p>In raw mode only Escape cancels. The dumb-terminal fallback still accepts
     * the literal input {@code e} as ESC because IDE consoles cannot transmit a
     * real Escape byte.
     */
    public static String readChoice(Scanner fallbackScanner) {
        String result = withRawMode(() -> {
            NonBlockingReader reader = TERMINAL.reader();
            while (true) {
                int ch = reader.read();
                if (ch == 27) { // ESC
                    int next = reader.read(50L);
                    if (next == -2) return "ESC"; // standalone
                    while (reader.read(10L) >= 0) { /* drain CSI */ }
                    continue; // arrow / function key — keep waiting
                }
                if (ch >= '1' && ch <= '9') return String.valueOf((char) ch);
                if (ch == 'C' || ch == 'c') return "C";
                // ignore other keys
            }
        }, null);
        if (result != null) return result;

        System.out.print(prompt("Choice: "));
        String input = fallbackScanner.nextLine().trim();
        if (input.equalsIgnoreCase("e")) return "ESC"; // dumb-terminal cancel fallback
        return input;
    }

    // ── Raw-mode line reader with ESC support ───────────────────────────
    /**
     * Read a line of text with live ESC support.
     * Characters are echoed, Backspace works, ESC cancels immediately (returns null).
     */
    public static String readLine(Scanner fallbackScanner) {
        String result = withRawMode(() -> {
            NonBlockingReader reader = TERMINAL.reader();
            StringBuilder sb = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == '\r' || ch == '\n' || ch == -1) {
                    TERMINAL.writer().println();
                    TERMINAL.writer().flush();
                    return sb.toString().trim();
                } else if (ch == 27) { // ESC
                    int next = reader.read(50L);
                    if (next == -2) { // standalone ESC — cancel
                        TERMINAL.writer().println();
                        TERMINAL.writer().flush();
                        return null;
                    }
                    while (reader.read(10L) >= 0) { /* drain CSI */ }
                } else if (ch == 127 || ch == 8) {
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        TERMINAL.writer().print("\b \b");
                        TERMINAL.writer().flush();
                    }
                } else if (ch == 3) { // Ctrl+C
                    TERMINAL.writer().println();
                    TERMINAL.writer().flush();
                    return null;
                } else if (ch >= 32) {
                    sb.append((char) ch);
                    TERMINAL.writer().print((char) ch);
                    TERMINAL.writer().flush();
                }
            }
        }, "__FALLBACK__");

        if ("__FALLBACK__".equals(result)) {
            String line = fallbackScanner.nextLine().trim();
            if (line.equalsIgnoreCase("e")) return null;
            return line;
        }
        return result; // may be null (user pressed ESC in raw mode)
    }

    // ── Validated input ─────────────────────────────────────────────────
    /**
     * Result of validating a single input attempt.
     * Either holds a parsed value ({@link #ok}) or a user-facing error message ({@link #err}).
     */
    public static final class Result<T> {
        public final T value;
        public final String error;

        private Result(T value, String error) {
            this.value = value;
            this.error = error;
        }
        public static <T> Result<T> ok(T value)     { return new Result<>(value, null); }
        public static <T> Result<T> err(String msg) { return new Result<>(null, msg); }
    }

    /** Validates raw user input and returns a {@link Result}. */
    @FunctionalInterface
    public interface Validator<T> {
        Result<T> validate(String input);
    }

    /**
     * Prompt the user, read a line, and validate. On error, show the message,
     * pause for any keypress, then re-prompt the same field. ESC always cancels
     * (returns {@code null}).
     *
     * @param promptText the prompt label (e.g. "Enter your first name (Esc to go back): ")
     * @param scanner    fallback scanner for non-raw environments
     * @param validator  returns {@link Result#ok} or {@link Result#err}
     * @return the parsed value, or {@code null} if the user pressed ESC
     */
    public static <T> T promptUntilValid(String promptText, Scanner scanner, Validator<T> validator) {
        while (true) {
            System.out.print(prompt(promptText));
            String input = readLine(scanner);
            if (input == null) return null;
            Result<T> r = validator.validate(input);
            if (r.error == null) return r.value;
            System.out.println(warning(r.error));
            waitForKey(scanner);
            eraseLastPromptCycle(promptText);
        }
    }

    /**
     * Convenience: prompt for an integer in {@code [1, max]} (inclusive).
     * Re-prompts on parse error or out-of-range. ESC returns {@code null}.
     */
    public static Integer promptChoiceInRange(String promptText, Scanner scanner, int max) {
        return promptUntilValid(promptText, scanner, s -> {
            try {
                int n = Integer.parseInt(s);
                if (n >= 1 && n <= max) return Result.ok(n);
                return Result.err("[ERR_RANGE] Enter a number between 1 and " + max + ".");
            } catch (NumberFormatException _) {
                return Result.err("[ERR_NUM] Please enter a valid number.");
            }
        });
    }

    /**
     * Same retry-until-valid contract as {@link #promptUntilValid} but reads
     * input via {@link #readPassword} (masked with '*').
     */
    public static <T> T promptPasswordUntilValid(String promptText, Scanner scanner, Validator<T> validator) {
        while (true) {
            System.out.print(prompt(promptText));
            String input = readPassword(scanner);
            if (input == null) return null;
            Result<T> r = validator.validate(input);
            if (r.error == null) return r.value;
            System.out.println(warning(r.error));
            waitForKey(scanner);
            eraseLastPromptCycle(promptText);
        }
    }

    /**
     * After a failed prompt + error + "press any key" sequence, scrub all of it
     * from the screen so the re-prompt looks like the same blank line as before.
     *
     * Lines erased: every visible line written since the prompt began, namely:
     *   - the prompt line itself (plus any leading newlines in {@code promptText})
     *   - the user's typed input (echoed on the same line as the prompt, so no extra)
     *   - the error line
     *   - the "Press any key…" line
     *
     * Falls back to {@link #clearScreen} on terminals without ANSI cursor support.
     */
    private static void eraseLastPromptCycle(String promptText) {
        if (!ANSI_SUPPORTED) {
            clearScreen();
            return;
        }
        // 1 (prompt) + leading \n in promptText + 1 (error) + 1 (press-any-key)
        int leadingNewlines = 0;
        for (int i = 0; i < promptText.length() && promptText.charAt(i) == '\n'; i++) leadingNewlines++;
        int linesUp = 3 + leadingNewlines;
        System.out.print("\033[" + linesUp + "A\r\033[J");
        System.out.flush();
    }

    // ── Arrow-key / hotkey reader ───────────────────────────────────────
    /**
     * Read a single navigation key for interactive views.
     * Returns one of: "LEFT", "RIGHT", "SHIFT_LEFT", "SHIFT_RIGHT", "T", "ESC".
     * Other keys are silently ignored (keeps waiting).
     *
     * Raw-mode path: parses xterm/VT100 CSI escape sequences.
     * Fallback path (dumb terminal / piped stdin): h=LEFT, l=RIGHT,
     *   H=SHIFT_LEFT, L=SHIFT_RIGHT, t/T=T, e/empty=ESC.
     */
    public static String readArrowOrKey(Scanner fallbackScanner) {
        String result = withRawMode(() -> {
            NonBlockingReader reader = TERMINAL.reader();
            while (true) {
                int ch = reader.read();
                if (ch == 0xE0) { // Windows conhost arrow prefix
                    int code = reader.read(150L);
                    if (code == 0x48) return "UP";
                    if (code == 0x50) return "DOWN";
                    if (code == 0x4B) return "LEFT";
                    if (code == 0x4D) return "RIGHT";
                    if (code == 0x73) return "SHIFT_LEFT";
                    if (code == 0x74) return "SHIFT_RIGHT";
                    continue;
                }
                if (ch == 27) { // ESC byte / VT100 arrow prefix
                    int next = reader.read(150L);
                    if (next == -2) return "ESC"; // standalone ESC (timeout)
                    if (next == '[') {
                        int third = reader.read(150L);
                        if (third == 'A') return "UP";
                        if (third == 'B') return "DOWN";
                        if (third == 'D') return "LEFT";
                        if (third == 'C') return "RIGHT";
                        if (third == 'Z') return "SHIFT_TAB"; // Shift+Tab → ESC[Z
                        if (third == '1') { // potential modifier sequence
                            int semi = reader.read(150L);
                            int mod  = reader.read(150L);
                            int dir  = reader.read(150L);
                            if (semi == ';' && mod == '2') {
                                if (dir == 'D') return "SHIFT_LEFT";
                                if (dir == 'C') return "SHIFT_RIGHT";
                            }
                        }
                        // drain remaining bytes of unrecognised sequence
                        while (reader.read(10L) >= 0) { }
                    } else {
                        while (reader.read(10L) >= 0) { }
                    }
                    continue; // unrecognised — keep waiting
                }
                if (ch == '\t') return "TAB";
                if (ch == 'T' || ch == 't') return "T";
                if (ch == 'M' || ch == 'm') return "M";
                if (ch == 'L' || ch == 'l') return "L";
                if (ch == '\r' || ch == '\n') return "ENTER";
                // all other keys ignored
            }
        }, null);
        if (result != null) return result;

        // Dumb terminal / piped stdin fallback
        String line = fallbackScanner.nextLine().trim();
        if (line.equalsIgnoreCase("e")) return "ESC";
        if (line.isEmpty() || line.equalsIgnoreCase("enter")) return "ENTER";
        switch (line) {
            case "h": return "LEFT";
            case "l": return "RIGHT";
            case "H": return "SHIFT_LEFT";
            case "L": return "L";
            case "k": return "UP";
            case "j": return "DOWN";
            case "t": case "T": return "T";
            case "m": case "M": return "M";
            case "tab":         return "TAB";
            case "shift_tab":   return "SHIFT_TAB";
            default:  return "ESC";
        }
    }
}
