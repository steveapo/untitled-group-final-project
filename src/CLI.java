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
    public static String success(String text) { return ansi(GREEN  + BOLD,  text); }
    public static String error(String text)   { return ansi(RED    + BOLD,  text); }
    public static String warning(String text) { return ansi(YELLOW + BOLD,  text); }
    public static String header(String text)  { return ansi(CYAN   + BOLD,  text); }
    public static String prompt(String text)  { return ansi(MAGENTA,        text); }

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
        System.out.print("\r" + success("✔") + "  " + label + "   \n");
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
}
