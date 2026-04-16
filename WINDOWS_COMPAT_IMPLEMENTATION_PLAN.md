# Windows Compatibility — Implementation Plan

**Target:** Claude Opus 4.6 development agent
**Repo:** `untitled-group-final-project` (Hotel Room Booking System, Java CLI)
**Goal:** Make the CLI work on Windows with full feature parity (arrow-key menus, ESC hotkey, masked passwords, single-keypress choices, colors, Unicode glyphs) — matching the existing macOS experience.
**Approach:** Replace the hand-rolled `stty`/`ProcessBuilder` raw-I/O layer with **JLine 3**, and migrate the build from raw `javac`/`jar` to Maven so the dependency can be bundled.

---

## 1. Context the developer needs first

The app currently "runs" on Windows but every interactive feature silently degrades because the entire CLI is built on Unix-only primitives. There is no Windows-native code path — there's only a `Scanner`-based fallback that triggers whenever the `stty` `ProcessBuilder` calls throw `IOException`.

Read these files before making any changes:

- `src/CLI.java` — the file that needs almost all of the edits
- `src/Main.java` — entry point; only minor changes needed (JAR-relative CWD)
- `src/Files.java` — file I/O; one change needed (resolve paths against JAR location)
- `run.bat` / `run.sh` / `build.bat` / `build.sh` — launcher and build scripts
- `pom.xml` — currently declarative only; must become the real build file

Everything else (`Account.java`, `Bookings.java`, `Room.java`, `DateInput.java`, `SeedManager.java`, `*Menu.java`) is already platform-neutral. Do **not** modify those files.

---

## 2. Findings summary (root-cause list)

Ordered by severity. Each item maps to a concrete fix in Section 4.

| # | Severity | Location | Problem |
|---|----------|----------|---------|
| 1 | **Critical** | `CLI.java:181, 267, 374, 406, 453` | Five methods (`readPassword`, `selectFromList`, `waitForKey`, `readChoice`, `readLine`) shell out to `/bin/sh -c "stty ... < /dev/tty"`. None of these exist on Windows. `IOException` is swallowed by `catch (Exception ignored)`, causing silent fallback to `Scanner.nextLine()`. |
| 2 | **Critical** | `CLI.java:257` — `selectFromList` | Arrow-key menu — the headline UX — is unavailable on Windows. Falls back to "type 1–N and press Enter." |
| 3 | **Critical** | `CLI.java:404` — `readChoice` | ESC hotkey advertised in footer (`CLI.java:366` prints `[Esc] Exit`) does nothing on Windows. User must type `e` + Enter, which isn't discoverable. |
| 4 | **High** | `CLI.java:33` — `detectAnsiSupport` | Claims to suppress colors in legacy `cmd.exe`, but the check `System.console() != null` returns non-null there too. Users of plain `cmd.exe` see raw `ESC[36m` garbage. |
| 5 | **High** | `CLI.java:104, 345` etc. | Spinner frames (Braille), banner (box-drawing), menu glyphs (`▸ ↑↓ ✔ ✘`) render as `?`/mojibake in `cmd.exe` without the UTF-8 code page. |
| 6 | **High** | `run.bat:5-7` | Does not set `chcp 65001` or pass `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8`. Windows defaults to CP-1252 / a regional code page. |
| 7 | **Medium** | `Files.java:12-15` | Data files (`Users`, `Bookings`, `Rooms`, `Errors`) are referenced with bare relative paths. If a Windows user double-clicks `HotelBooking.jar` from Explorer, CWD becomes `%USERPROFILE%` and the app reads/creates files in the wrong place. On macOS `./run.sh` masks this by `cd`-ing first. |
| 8 | **Medium** | `CLI.java:178` — `readPassword` fallback 2 | When stty fails, uses `System.console().readPassword()`, which hides keystrokes entirely rather than echoing `*` like the macOS path does. Fallback 3 (IDE) types passwords in plain text. |
| 9 | **Low** | `CLI.java:65` — `link` | OSC 8 hyperlinks work in Windows Terminal but render as raw escapes in `cmd.exe` / PowerShell 5.1. |
| 10 | **Low** | Build scripts | `build.bat` / `build.sh` use raw `javac`/`jar` and will not resolve a Maven dependency (needed for JLine). |

---

## 3. Chosen approach: JLine 3

JLine 3 is the library JShell, Groovy's shell, and the Spring Boot CLI use. It provides:

- Raw-mode terminal access with one API on **Unix termios, Windows `conhost` (via JNA), Windows Terminal, MSYS/Cygwin, and "dumb" IDE consoles**
- Automatic enabling of `ENABLE_VIRTUAL_TERMINAL_PROCESSING` on Windows 10+ so our existing ANSI color codes keep working
- Non-blocking single-keypress reading (what we need for arrow keys, ESC, digit hotkeys)
- Masked password input (with echoed `*`) that works identically everywhere

### Dependency coordinates

Use **JLine 3.26.3** (Java 8+ compatible, stable, widely deployed). If you prefer the latest 3.x minor, 3.30.4 is also fine — the API used below is unchanged. Do **not** use JLine 4.x unless you also audit for API differences.

```xml
<dependency>
    <groupId>org.jline</groupId>
    <artifactId>jline</artifactId>
    <version>3.26.3</version>
</dependency>
```

This "uber" artifact includes all the terminal providers. No separate JNA dep is required — JLine ships it shaded.

---

## 4. Implementation steps

Execute in this order. Each step is independently verifiable.

### Step 1 — Migrate build to Maven (required before any code changes compile)

**File:** `pom.xml` — replace the current content with a working build definition.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.hotelbooking</groupId>
    <artifactId>HotelBooking</artifactId>
    <version>1.2.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <sourceDirectory>src</sourceDirectory>
        <testSourceDirectory>test</testSourceDirectory>
        <plugins>
            <plugin>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals><goal>shade</goal></goals>
                        <configuration>
                            <finalName>HotelBooking</finalName>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>Main</mainClass>
                                </transformer>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.jline</groupId>
            <artifactId>jline</artifactId>
            <version>3.26.3</version>
        </dependency>
    </dependencies>
</project>
```

> `ServicesResourceTransformer` is **required** — JLine uses `META-INF/services` to discover terminal providers. Without it, only the "dumb" provider will load and the Windows experience will still be broken.

Update `build.sh` and `build.bat` to invoke Maven:

```bash
# build.sh
#!/bin/bash
set -e
mvn clean package
mkdir -p dist
cp target/HotelBooking.jar dist/
cp Rooms Bookings Users dist/ 2>/dev/null || true
touch dist/Errors
cp run.sh run.bat dist/ 2>/dev/null || true
```

```bat
REM build.bat
@echo off
call mvn clean package
if errorlevel 1 ( echo Build failed & pause & exit /b 1 )
if not exist dist mkdir dist
copy target\HotelBooking.jar dist\ >nul
copy Rooms dist\ >nul 2>&1
copy Bookings dist\ >nul 2>&1
copy Users dist\ >nul 2>&1
type nul > dist\Errors
copy run.bat dist\ >nul 2>&1
pause
```

### Step 2 — Update `run.bat` to force UTF-8

**File:** `run.bat`

```bat
@echo off
chcp 65001 >nul
cd /d "%~dp0dist"
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar HotelBooking.jar
pause
```

`chcp 65001` switches the console to UTF-8. The two `-D` flags ensure `System.out` writes UTF-8 regardless of the platform default. Together these fix issues **#5 and #6**. No change needed to `run.sh`.

### Step 3 — Introduce a shared `Terminal` singleton in `CLI.java`

At the top of `CLI.java`, replace the import block and add a terminal holder. Close it on shutdown.

```java
import java.util.Scanner;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

public class CLI {

    private static final Terminal TERMINAL = buildTerminal();

    private static Terminal buildTerminal() {
        try {
            Terminal t = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)           // fall back gracefully in IDEs instead of throwing
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
    // ... rest of class
}
```

### Step 4 — Fix ANSI detection (issue #4)

Replace `detectAnsiSupport` with one that trusts JLine's capability detection:

```java
private static final boolean ANSI_SUPPORTED = detectAnsiSupport();

private static boolean detectAnsiSupport() {
    if (System.getenv("NO_COLOR") != null) return false;
    if ("dumb".equals(System.getenv("TERM"))) return false;
    if (TERMINAL == null) return false;
    // JLine's type is "dumb" for IDE consoles and unsupported cmd.exe,
    // and a proper name (xterm-256color, windows, etc.) when VT is available.
    return !"dumb".equals(TERMINAL.getType());
}
```

### Step 5 — Replace `readChoice` (issue #3)

Delete the entire current `readChoice` body (lines 404–443) and replace with:

```java
public static String readChoice(Scanner fallbackScanner) {
    if (rawModeAvailable()) {
        try {
            TERMINAL.enterRawMode();
            NonBlockingReader reader = TERMINAL.reader();
            while (true) {
                int ch = reader.read();
                if (ch == 27) { // ESC
                    int next = reader.read(50L); // wait up to 50ms for sequence
                    if (next == -2) return "ESC"; // timeout = standalone ESC
                    // consume rest of CSI sequence
                    while (reader.read(10L) >= 0) { /* drain */ }
                    continue;
                }
                if (ch >= '1' && ch <= '9') return String.valueOf((char) ch);
                if (ch == 'e' || ch == 'q') return "ESC";
                // ignore other keys
            }
        } catch (Exception ignored) {
            // fall through
        }
    }
    System.out.print(prompt("Choice: "));
    String input = fallbackScanner.nextLine().trim();
    if (input.equalsIgnoreCase("e")) return "ESC";
    return input;
}
```

Notes for the dev: `NonBlockingReader.read(long)` returns `-2` on timeout (sentinel for "no character within timeout"), which is the idiomatic JLine way to distinguish a standalone ESC from the start of a CSI escape sequence. Do not use `System.in.available()` — JLine buffers input and that API will lie.

### Step 6 — Replace `readLine` (raw-mode line reader)

Replace `readLine` (lines 451–502) with a JLine-backed version. You can keep the manual char-at-a-time loop for full parity with the current behavior (live ESC cancel), or use `LineReader`:

```java
public static String readLine(Scanner fallbackScanner) {
    if (rawModeAvailable()) {
        try {
            TERMINAL.enterRawMode();
            NonBlockingReader reader = TERMINAL.reader();
            StringBuilder sb = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == '\r' || ch == '\n' || ch == -1) {
                    TERMINAL.writer().println();
                    TERMINAL.writer().flush();
                    return sb.toString().trim();
                } else if (ch == 27) {
                    int next = reader.read(50L);
                    if (next == -2) { // standalone ESC
                        TERMINAL.writer().println();
                        TERMINAL.writer().flush();
                        return null;
                    }
                    while (reader.read(10L) >= 0) { /* drain */ }
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
        } catch (Exception ignored) {
            // fall through
        }
    }
    String line = fallbackScanner.nextLine().trim();
    if (line.equalsIgnoreCase("e")) return null;
    return line;
}
```

### Step 7 — Replace `readPassword` (issue #8)

Replace `readPassword` (lines 178–242). Same shape as `readLine` but echo `*`:

```java
public static String readPassword(Scanner fallbackScanner) {
    if (rawModeAvailable()) {
        try {
            TERMINAL.enterRawMode();
            NonBlockingReader reader = TERMINAL.reader();
            StringBuilder pw = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == '\r' || ch == '\n' || ch == -1) {
                    TERMINAL.writer().println();
                    TERMINAL.writer().flush();
                    return pw.toString();
                } else if (ch == 27) {
                    int next = reader.read(50L);
                    if (next == -2) {
                        TERMINAL.writer().println();
                        TERMINAL.writer().flush();
                        return null;
                    }
                    while (reader.read(10L) >= 0) { /* drain */ }
                } else if (ch == 127 || ch == 8) {
                    if (pw.length() > 0) {
                        pw.deleteCharAt(pw.length() - 1);
                        TERMINAL.writer().print("\b \b");
                        TERMINAL.writer().flush();
                    }
                } else if (ch == 3) {
                    TERMINAL.writer().println();
                    TERMINAL.writer().flush();
                    return null;
                } else {
                    pw.append((char) ch);
                    TERMINAL.writer().print('*');
                    TERMINAL.writer().flush();
                }
            }
        } catch (Exception ignored) { /* fall through */ }
    }
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
```

### Step 8 — Replace `waitForKey`

Replace `waitForKey` (lines 371–396):

```java
public static void waitForKey(Scanner fallbackScanner) {
    System.out.print(dim("  Press any key to continue..."));
    System.out.flush();
    if (rawModeAvailable()) {
        try {
            TERMINAL.enterRawMode();
            NonBlockingReader reader = TERMINAL.reader();
            reader.read();
            // drain any trailing bytes from escape sequences
            while (reader.read(10L) >= 0) { /* ignore */ }
            TERMINAL.writer().println();
            TERMINAL.writer().flush();
            return;
        } catch (Exception ignored) { /* fall through */ }
    }
    fallbackScanner.nextLine();
}
```

### Step 9 — Replace `selectFromList` (issue #2 — the headline feature)

Replace the two overloads (lines 257–330). Keep the existing `renderList` helper as-is. Only the input-handling block changes:

```java
public static int selectFromList(String[] labels, String title, Scanner scanner, int initialSelection) {
    if (labels.length == 0) return -1;

    if (rawModeAvailable()) {
        try {
            TERMINAL.enterRawMode();
            NonBlockingReader reader = TERMINAL.reader();
            int selected = Math.max(0, Math.min(initialSelection, labels.length - 1));
            renderList(labels, title, selected);
            while (true) {
                int ch = reader.read();
                if (ch == '\r' || ch == '\n') {
                    System.out.println();
                    return selected;
                } else if (ch == 27) {
                    int next = reader.read(50L);
                    if (next == -2) { // standalone ESC
                        System.out.println();
                        return -1;
                    }
                    if (next == '[') {
                        int arrow = reader.read(50L);
                        if (arrow == 'A') selected = (selected - 1 + labels.length) % labels.length;
                        else if (arrow == 'B') selected = (selected + 1) % labels.length;
                        // ignore C/D (left/right)
                    }
                } else if (ch == 'e' || ch == 'q') {
                    System.out.println();
                    return -1;
                } else {
                    continue;
                }
                int linesToClear = labels.length + 2;
                System.out.print("\033[" + linesToClear + "A");
                renderList(labels, title, selected);
            }
        } catch (Exception ignored) { /* fall through */ }
    }
    // Numbered fallback unchanged
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
```

### Step 10 — Remove the dead Unix-specific code

After Steps 5–9, the file should no longer import or reference any of: `/bin/sh`, `stty`, `/dev/tty`, `ProcessBuilder` used for raw-mode, or `System.in.read()` inside CLI methods. Search the file and confirm none remain. The only surviving `ProcessBuilder` call should be in `openUrl` (lines 71–84), which is already correctly OS-branched — leave it alone.

### Step 11 — Make data files resolve against the JAR location (issue #7)

**File:** `src/Files.java`

Replace the string constants at the top (lines 12–15) with JAR-relative `File` constants built in a static initializer:

```java
private static final File USERS_FILE    = resolveDataFile("Users");
private static final File BOOKINGS_FILE = resolveDataFile("Bookings");
private static final File ROOMS_FILE    = resolveDataFile("Rooms");
private static final File ERRORS_FILE   = resolveDataFile("Errors");

private static File resolveDataFile(String name) {
    try {
        File jarDir = new File(
            Files.class.getProtectionDomain().getCodeSource().getLocation().toURI()
        ).getParentFile();
        File candidate = new File(jarDir, name);
        if (candidate.exists() || jarDir.canWrite()) return candidate;
    } catch (Exception ignored) { /* fall through */ }
    return new File(name); // CWD fallback for IDE runs
}
```

Then update every `new File(USERS_FILE)` / `new Scanner(new File(BOOKINGS_FILE))` call site in the file to pass the `File` directly (not wrap it again). `FileWriter(String, boolean)` calls need to become `FileWriter(File, boolean)`. There are roughly a dozen call sites — a mechanical search-and-replace in `Files.java`. **Do not change the call sites' semantics**, only the path resolution.

### Step 12 — Clean up `Main.java` messaging

Main already uses `CLI.withSpinner`, `CLI.clearScreen`, etc., and does not need edits. However, the line `System.out.println("Data files found.");` in `Files.checkFile()` (line 71) is noisy — consider silencing or routing through `CLI.dim()`. Non-blocking; do only if time permits.

---

## 5. Verification checklist

The dev agent must perform every check below and report results. Do not mark complete until every box is checked on both OSes.

### Compile & smoke-test
- [ ] `mvn clean package` succeeds and produces `target/HotelBooking.jar` (~1.5–2 MB with JLine shaded in)
- [ ] `java -jar target/HotelBooking.jar` launches on macOS and Windows without exceptions
- [ ] `jar tf target/HotelBooking.jar | grep META-INF/services` lists JLine terminal providers (proves `ServicesResourceTransformer` ran)

### macOS regression (must still work identically to before)
- [ ] Arrow keys navigate `selectFromList` and the selection highlights in magenta
- [ ] ESC cancels from any menu and from password/line inputs
- [ ] Password input echoes `*` per keystroke
- [ ] Spinner Braille frames animate
- [ ] Banner box-drawing renders correctly
- [ ] Menu digits 1–4 trigger without pressing Enter
- [ ] ANSI colors render

### Windows — Windows Terminal (primary target)
- [ ] Arrow keys navigate `selectFromList` — **this is the headline regression to fix**
- [ ] ESC cancels at all prompts
- [ ] Password input echoes `*` per keystroke
- [ ] Spinner Braille frames animate (fall back to ASCII acceptable if terminal can't render Braille)
- [ ] Banner and menu glyphs render as boxes/arrows, not `?`
- [ ] Digit hotkeys work without Enter
- [ ] Colors render
- [ ] Double-clicking `dist\HotelBooking.jar` from Explorer finds the data files (proves Step 11)

### Windows — PowerShell 7+
- [ ] Same as Windows Terminal above

### Windows — legacy `cmd.exe` (graceful degradation target)
- [ ] App launches and is usable
- [ ] Colors either render correctly (VT enabled) or are fully suppressed — **no raw `ESC[36m` text**
- [ ] Unicode glyphs render after `chcp 65001` runs; no `?` spam
- [ ] Numbered-choice fallback appears if raw mode can't initialize

### IDE (IntelliJ, VS Code terminal)
- [ ] App launches. Interactive menus use the numbered fallback (IDE consoles are "dumb")
- [ ] Password input works via `Scanner` fallback (plain text acceptable in IDEs — document this in README)

---

## 6. Out of scope / explicitly do not touch

- Business logic (`Account`, `Bookings`, `Room`, `*Menu`, `DateInput`, `SeedManager`) — already portable
- CSV file format — already portable
- SHA-512 hashing / Base64 encoding — already uses `StandardCharsets.UTF_8`
- `openUrl` (`CLI.java:71`) — already correctly OS-branched
- OSC 8 terminal hyperlinks (`CLI.java:65`) — low-value polish; leave as-is

---

## 7. Rollback plan

All changes are on a single feature branch. If Windows regression testing reveals JLine issues:

1. Revert `pom.xml`, `build.sh`, `build.bat`, `CLI.java`, `Files.java`, `run.bat` to `main`
2. The `git revert` is clean because no business logic was touched
3. `dist/HotelBooking.jar` built from `main` still runs on macOS; Windows degradation returns to current state

---

## 8. Estimated effort

- Step 1 (Maven migration): 30 min
- Step 2 (run.bat UTF-8): 5 min
- Steps 3–10 (CLI.java rewrite): 2–3 hours (most of the work; mostly mechanical once the JLine `Terminal` + `NonBlockingReader` pattern is established)
- Step 11 (Files.java JAR-relative): 30 min
- Verification across 4 environments: 1–2 hours

**Total: ~half a day for an experienced Java developer.**

---

## 9. References

- [JLine 3 GitHub](https://github.com/jline/jline3)
- [JLine 3.26.3 on Maven Central](https://search.maven.org/artifact/org.jline/jline/3.26.3/jar)
- [JLine releases (check for newer 3.x patches)](https://github.com/jline/jline3/releases)
- [no-color.org convention (already respected by existing code)](https://no-color.org)
