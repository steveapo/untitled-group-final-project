/**
 * Cross-platform CLI utilities: ANSI colours, screen clearing, and progress spinners.
 *
 * Colour output is automatically disabled when:
 *   - stdout is not a real terminal (e.g. piped or redirected)
 *   - the TERM environment variable is "dumb"
 *   - NO_COLOR is set (https://no-color.org)
 *
 * On Windows, ANSI codes work natively in Windows Terminal and PowerShell 7+.
 * Legacy cmd.exe will see plain text (colours suppressed automatically).
 */
import java.util.Scanner;

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

    // Detect whether the current terminal supports ANSI codes
    private static final boolean ANSI_SUPPORTED = detectAnsiSupport();

    private static boolean detectAnsiSupport() {
        // Respect the NO_COLOR convention (https://no-color.org)
        if (System.getenv("NO_COLOR") != null) return false;
        // Dumb terminals don't support escape codes
        String term = System.getenv("TERM");
        if ("dumb".equals(term)) return false;
        // Check if stdout is actually connected to a terminal
        return System.console() != null;
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
    /**
     * Clear the terminal screen.
     * Uses the ANSI erase-screen + cursor-home sequence, which works on
     * macOS, Linux, Windows Terminal, and PowerShell 7+.
     * Falls back to printing blank lines on dumb terminals.
     */
    public static void clearScreen() {
        if (ANSI_SUPPORTED) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } else {
            // Fallback: scroll past old content
            for (int i = 0; i < 40; i++) System.out.println();
        }
    }

    // ── Spinner / loader ─────────────────────────────────────────────────
    private static final String[] SPINNER_FRAMES = { "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };
    // ASCII fallback for terminals that can't render Braille
    private static final String[] SPINNER_ASCII  = { "|", "/", "-", "\\" };

    /**
     * Show a spinner for the given duration while a task runs on a background thread.
     *
     * Usage:
     *   CLI.withSpinner("Loading data", 1200, () -> { ... your slow code ... });
     *
     * @param label    Text shown next to the spinner
     * @param task     Runnable to execute while the spinner animates
     */
    public static void withSpinner(String label, Runnable task) {
        // Run the real task on a background thread
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
        // Clear the spinner line, print completion tick
        System.out.print("\r" + ansi(GREEN + BOLD, "\u2714") + "  " + label + "   \n");
        System.out.flush();
    }

    /**
     * Show a fixed-duration spinner (useful for simulating load in demos).
     *
     * @param label      Text shown next to the spinner
     * @param durationMs How long to spin in milliseconds
     */
    public static void spinner(String label, int durationMs) {
        withSpinner(label, () -> {
            try { Thread.sleep(durationMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
    }

    /**
     * Show a spinner for a random duration between 500 ms and 2000 ms.
     *
     * @param label Text shown next to the spinner
     */
    private static final java.util.Random RANDOM = new java.util.Random();

    /** Returns a random delay between 500 ms and 2000 ms. */
    public static int randomDelayMs() {
        return 500 + RANDOM.nextInt(1501);
    }

    public static void randomSpinner(String label) {
        spinner(label, randomDelayMs());
    }

    // ── Password input ──────────────────────────────────────────────────
    /**
     * Read a password while masking each keystroke with '*'.
     *
     * Strategy (tries in order):
     *   1. stty — works on macOS and Linux terminals; shows live asterisks.
     *   2. System.console().readPassword() — works on Windows PowerShell /
     *      Windows Terminal; input is hidden (no asterisks, but not visible).
     *   3. Plain Scanner — last resort inside IDEs where neither is available.
     */
    public static String readPassword(Scanner fallbackScanner) {
        // Attempt 1: stty-based char-by-char masking (macOS / Linux)
        try {
            ProcessBuilder savePb = new ProcessBuilder("/bin/sh", "-c", "stty -g < /dev/tty");
            Process save = savePb.start();
            String originalSettings = new String(save.getInputStream().readAllBytes()).trim();
            save.waitFor();

            ProcessBuilder rawPb = new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
            rawPb.start().waitFor();

            StringBuilder password = new StringBuilder();
            try {
                while (true) {
                    int ch = System.in.read();
                    if (ch == '\r' || ch == '\n' || ch == -1) {
                        System.out.println();
                        break;
                    } else if (ch == 27) { // ESC
                        try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        if (System.in.available() > 0) {
                            while (System.in.available() > 0) System.in.read();
                            continue; // arrow key sequence — ignore
                        }
                        System.out.println();
                        return null; // standalone ESC — cancel
                    } else if (ch == 127 || ch == 8) { // backspace / delete
                        if (password.length() > 0) {
                            password.deleteCharAt(password.length() - 1);
                            System.out.print("\b \b");
                            System.out.flush();
                        }
                    } else if (ch == 3) { // Ctrl+C
                        System.out.println();
                        return null;
                    } else {
                        password.append((char) ch);
                        System.out.print("*");
                        System.out.flush();
                    }
                }
            } finally {
                ProcessBuilder restorePb = new ProcessBuilder("/bin/sh", "-c", "stty " + originalSettings + " < /dev/tty");
                restorePb.start().waitFor();
            }
            return password.toString();
        } catch (Exception ignored) {
            // stty not available (Windows) — fall through
        }

        // Attempt 2: System.console (Windows PowerShell / Windows Terminal — hidden, no asterisks)
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword();
            if (chars == null) return null;
            String pwd = new String(chars);
            if (pwd.equalsIgnoreCase("e")) return null;
            return pwd;
        }

        // Attempt 3: plain input (IDEs where console is null)
        String line = fallbackScanner.nextLine();
        if (line.equalsIgnoreCase("e")) return null;
        return line;
    }

    // ── Interactive list selector ──────────────────────────────────────
    /**
     * Display an interactive list the user navigates with arrow keys.
     * The currently selected item is highlighted in magenta.
     * Returns the selected index, or -1 if the user presses Escape / 'e'.
     *
     * On terminals without stty (Windows / IDEs) falls back to a
     * numbered-choice prompt using the Scanner.
     *
     * @param labels   Display strings for each option
     * @param title    Heading shown above the list
     * @param scanner  Fallback scanner for non-stty environments
     */
    public static int selectFromList(String[] labels, String title, Scanner scanner) {
        return selectFromList(labels, title, scanner, 0);
    }

    /** Overload that pre-selects a specific index (e.g. the current value in a toggle). */
    public static int selectFromList(String[] labels, String title, Scanner scanner, int initialSelection) {
        if (labels.length == 0) return -1;

        // Attempt interactive mode (macOS / Linux terminal)
        try {
            ProcessBuilder savePb = new ProcessBuilder("/bin/sh", "-c", "stty -g < /dev/tty");
            Process save = savePb.start();
            String originalSettings = new String(save.getInputStream().readAllBytes()).trim();
            save.waitFor();

            ProcessBuilder rawPb = new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
            rawPb.start().waitFor();

            int selected = Math.max(0, Math.min(initialSelection, labels.length - 1));
            try {
                renderList(labels, title, selected);
                while (true) {
                    int ch = System.in.read();
                    if (ch == '\r' || ch == '\n') {
                        // Move below the list before returning
                        System.out.println();
                        return selected;
                    } else if (ch == 27) { // ESC — could be arrow key or standalone Escape
                        int next = System.in.read();
                        if (next == '[') {
                            int arrow = System.in.read();
                            if (arrow == 'A') { // Up
                                selected = (selected - 1 + labels.length) % labels.length;
                            } else if (arrow == 'B') { // Down
                                selected = (selected + 1) % labels.length;
                            }
                        } else {
                            // Standalone Escape — cancel
                            System.out.println();
                            return -1;
                        }
                    } else if (ch == 'e' || ch == 'q') {
                        System.out.println();
                        return -1;
                    }
                    // Re-render: move cursor up to overwrite previous list
                    int linesToClear = labels.length + 2; // labels + hint + blank line
                    System.out.print("\033[" + linesToClear + "A"); // move up
                    renderList(labels, title, selected);
                }
            } finally {
                ProcessBuilder restorePb = new ProcessBuilder("/bin/sh", "-c", "stty " + originalSettings + " < /dev/tty");
                restorePb.start().waitFor();
            }
        } catch (Exception ignored) {
            // Fall through to numbered fallback
        }

        // Fallback: numbered selection (Windows / IDEs)
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
            System.out.println(prefix + label + "\033[K"); // \033[K clears rest of line
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
    /** Print a footer bar showing the ESC hotkey and its action. */
    public static void printFooter(String backLabel) {
        printDivider();
        System.out.println(dim("  [Esc] " + backLabel));
    }

    // ── Wait for any keypress ───────────────────────────────────────────
    /** Wait for any keypress (raw mode) or Enter (fallback). */
    public static void waitForKey(Scanner fallbackScanner) {
        System.out.print(dim("  Press any key to continue..."));
        try {
            ProcessBuilder savePb = new ProcessBuilder("/bin/sh", "-c", "stty -g < /dev/tty");
            Process save = savePb.start();
            String orig = new String(save.getInputStream().readAllBytes()).trim();
            save.waitFor();

            ProcessBuilder rawPb = new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
            rawPb.start().waitFor();

            try {
                System.in.read();
                try { Thread.sleep(30); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                while (System.in.available() > 0) System.in.read();
            } finally {
                ProcessBuilder restorePb = new ProcessBuilder("/bin/sh", "-c", "stty " + orig + " < /dev/tty");
                restorePb.start().waitFor();
            }
            System.out.println();
            return;
        } catch (Exception ignored) {
            // stty not available — fall through to Scanner
        }
        fallbackScanner.nextLine();
    }

    // ── Raw-mode menu choice reader ─────────────────────────────────────
    /**
     * Read a menu choice as a single keypress (no Enter needed).
     * Returns "1"–"9" for digit keys, or "ESC" when Escape is pressed.
     * Falls back to Scanner.nextLine() on Windows/IDEs where 'e' also maps to "ESC".
     */
    public static String readChoice(Scanner fallbackScanner) {
        try {
            ProcessBuilder savePb = new ProcessBuilder("/bin/sh", "-c", "stty -g < /dev/tty");
            Process save = savePb.start();
            String originalSettings = new String(save.getInputStream().readAllBytes()).trim();
            save.waitFor();

            ProcessBuilder rawPb = new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
            rawPb.start().waitFor();

            try {
                while (true) {
                    int ch = System.in.read();
                    if (ch == 27) { // ESC
                        try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        if (System.in.available() > 0) {
                            while (System.in.available() > 0) System.in.read();
                            continue; // arrow key sequence — ignore
                        }
                        return "ESC";
                    } else if (ch >= '1' && ch <= '9') {
                        return String.valueOf((char) ch);
                    } else if (ch == 'e' || ch == 'q') {
                        return "ESC";
                    }
                    // Ignore other keys
                }
            } finally {
                ProcessBuilder restorePb = new ProcessBuilder("/bin/sh", "-c", "stty " + originalSettings + " < /dev/tty");
                restorePb.start().waitFor();
            }
        } catch (Exception ignored) {
            // stty not available — fall through to Scanner
        }

        System.out.print(prompt("Choice: "));
        String input = fallbackScanner.nextLine().trim();
        if (input.equalsIgnoreCase("e")) return "ESC";
        return input;
    }

    // ── Raw-mode line reader with ESC support ───────────────────────────
    /**
     * Read a line of text with live ESC support.
     * Characters are echoed, Backspace works, ESC cancels immediately (returns null).
     * Falls back to Scanner.nextLine() in non-raw environments where 'e' also cancels.
     */
    public static String readLine(Scanner fallbackScanner) {
        try {
            ProcessBuilder savePb = new ProcessBuilder("/bin/sh", "-c", "stty -g < /dev/tty");
            Process save = savePb.start();
            String originalSettings = new String(save.getInputStream().readAllBytes()).trim();
            save.waitFor();

            ProcessBuilder rawPb = new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
            rawPb.start().waitFor();

            StringBuilder sb = new StringBuilder();
            try {
                while (true) {
                    int ch = System.in.read();
                    if (ch == '\r' || ch == '\n' || ch == -1) {
                        System.out.println();
                        return sb.toString().trim();
                    } else if (ch == 27) { // ESC
                        try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        if (System.in.available() > 0) {
                            while (System.in.available() > 0) System.in.read();
                            continue; // arrow key sequence — ignore
                        }
                        System.out.println();
                        return null; // standalone ESC — cancel
                    } else if (ch == 127 || ch == 8) { // Backspace
                        if (sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                            System.out.print("\b \b");
                            System.out.flush();
                        }
                    } else if (ch == 3) { // Ctrl+C
                        System.out.println();
                        return null;
                    } else if (ch >= 32) { // printable characters
                        sb.append((char) ch);
                        System.out.print((char) ch);
                        System.out.flush();
                    }
                }
            } finally {
                ProcessBuilder restorePb = new ProcessBuilder("/bin/sh", "-c", "stty " + originalSettings + " < /dev/tty");
                restorePb.start().waitFor();
            }
        } catch (Exception ignored) {
            // stty not available — fall through to Scanner
        }

        String line = fallbackScanner.nextLine().trim();
        if (line.equalsIgnoreCase("e")) return null;
        return line;
    }
}
