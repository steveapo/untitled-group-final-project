# CLI Interface

The Hotel Room Booking System uses a rich, interactive command-line interface powered by **JLine 3** for cross-platform terminal handling.

## Terminal Features

### ANSI Color Support

Colors adapt automatically to terminal capabilities:

| Element | Color | Example |
|---------|-------|---------|
| Success messages | Green | ✔ Booking confirmed! |
| Error messages | Red | ✘ Invalid date |
| Warnings | Red | ✘ Please enter a valid option |
| Headers | Cyan | HOTEL ROOM BOOKING SYSTEM |
| Prompts | Magenta | Enter your password: |
| Menu items | Cyan | 1. Search and book a room |
| Links | Cyan + Underline | https://docs.example.com |
| Emphasis | Bold | Room R401 |
| Disabled text | Dim | [Esc] Back |

### Graceful Degradation

**Terminal detection:**
- Modern terminals (macOS, Linux, Windows 10+ Terminal) → Full ANSI color and effects
- Legacy cmd.exe → Numbered menus (no colors, no fancy formatting)
- IDE consoles → Fallback to numbered selection, no raw input
- Piped output → Gracefully skips ANSI codes

**Color disable environment variables:**
- Set `NO_COLOR=1` to disable all colors
- Set `TERM=dumb` to disable colors and interactive features

## Interactive Elements

### Arrow-Key Navigation

Navigate menus and selectors using arrow keys:
- **↑** Move up
- **↓** Move down
- **←** Move left (in calendars)
- **→** Move right (in calendars)
- **Shift+←** / **Shift+→** Jump by week (calendar only)
- **Vim keybindings** h/j/k/l also work

### List Selection

When presented with multiple options:
```
  ▸ Option 1
    Option 2
    Option 3

  ↑↓ Navigate  Enter Select  Esc Cancel
```

- **Arrow keys** to move between options
- **Enter** to select highlighted option
- **Esc** to cancel and go back

### Password Masking

Passwords are entered securely:
```
Enter your password: ****
```

- Input echoed as `*` characters
- Backspace works normally
- ESC cancels password entry
- No plaintext visible on screen

### Menu Choice (Single Keypress)

Top-level menu selections don't require Enter:
```
1. Continue as Guest
2. Login
3. Register
4. User Guide
[Esc] Exit
```

- Press `1`–`9` to select (no Enter needed)
- Press `Esc` or `e` or `q` to go back
- Instant feedback

### Spinner / Loader

Long-running operations show a spinner:
```
⠋ Loading rooms...
⠙ Loading rooms...
⠹ Loading rooms...
✔ Loading rooms
```

**ANSI terminals:** Braille spinner frames (smooth animation)
**Non-ANSI:** ASCII spinner (|, /, -, \)

Both are smooth and responsive.

### Input Validation with Re-prompting

When validation fails, error messages are shown and the same field is re-prompted:

```
Enter your first name (Esc to go back): _
✘ First name must contain only letters.

Press any key to continue...
```

After keypress, prompt repeats cleanly without UI creep.

## Dialog Types

### Prompt for Text Input

```
Enter your username (Esc to go back): john_doe
```

- ESC to cancel
- Enter to submit
- Backspace to delete

### Prompt for Validated Input

```
Enter your email (Esc to go back): john@example
✘ Email must contain @ symbol.

Press any key to continue...
[re-prompts same field]
```

### Prompt for Integer Choice

```
Number of guests (1-9, Esc to go back): _
```

- Input must be 1-9
- Invalid input shows error and re-prompts
- ESC to cancel

### Yes/No Confirmation

```
Confirm booking? (yes/no, Esc to cancel): _
```

- Accepts: yes, y, no, n (case-insensitive)
- Invalid input re-prompts (doesn't cancel)
- ESC to cancel entire operation

## Occupancy Calendar

Interactive visual calendar for date selection:

```
        MAR 2026        
    Mon Tue Wed Thu Fri Sat Sun
    ─ ──────────────────────────
 1  ██  ░░  ██  ░░  ██  ░░  ██
 2  ░░  ██  ░░  ██  ░░  ██  ░░
 3  ██  ░░  ██  ░░  ██  ░░  ██
 4  ░░  ██  ░░  ██  ░░  ██  ░░
```

**Features:**
- Real-time availability display
- Smooth cursor movement with arrow keys
- Week jumps with Shift+Arrows
- Color-coded cells (green/red/pink)
- Dotted glyphs (░░) for normal state
- Solid glyphs (██) for selected/cursor position

## Hyperlinks

Clickable links work in modern terminals (macOS Terminal, Linux terminals, Windows Terminal):

```
Opening User Guide in browser...
📎 https://untitled-group-self.vercel.app
```

Click the link to open in your default browser. Falls back to displaying URL if terminal doesn't support links.

## Platform-Specific Behavior

### macOS / Linux
- **Terminal:** Full ANSI support with native JLine handling
- **Colors:** All ANSI colors available
- **Input:** Raw mode for instant keypresses (no Enter needed for single selections)
- **Special keys:** All arrow keys, Shift combinations supported

### Windows 10+ (Terminal / PowerShell 7+)
- **Terminal:** Windows Terminal 1.0+, PowerShell 7+
- **VT mode:** Auto-enabled by JLine (conhost VT emulation)
- **Colors:** Full ANSI color support
- **Input:** Same as macOS/Linux

### Windows Legacy (cmd.exe)
- **Terminal:** Old Command Prompt
- **Colors:** None (ANSI not supported)
- **Input:** Falls back to Scanner (requires Enter after every input)
- **Menus:** Numbered selection instead of arrow keys
- **Spinners:** ASCII frames instead of Braille

### IDE Consoles (IntelliJ, VS Code, etc.)
- **Dumb terminal fallback:** No raw input or ANSI
- **Colors:** Stripped (many IDEs don't support ANSI in console)
- **Input:** Scanner-based (requires Enter)
- **Menus:** Numbered selection
- **Spinners:** ASCII frames

## Screen Clearing

- **ANSI terminals:** Instant clear using escape sequences
- **Non-ANSI:** 40 blank lines printed (crude but effective)

## Accessibility

### Keyboard-Only Navigation
Everything can be done via keyboard:
- No mouse required
- ESC always cancels/goes back
- All navigation via arrow keys or keybindings

### Vim Keybindings
Power users can use Vim keybindings:
- **h** = Left
- **j** = Down
- **k** = Up
- **l** = Right
- **H** = Shift+Left (week back)
- **L** = Shift+Right (week forward)

### Large Terminal Support
Works on terminals of any size:
- Minimum: 80×24 (standard VT100)
- Works on massive displays too
- Dynamic layout (no hardcoded column counts for main items)

## Error Handling

### Error Logs
Errors are logged to an `Errors` file:
- Validation errors
- Invalid credentials
- System errors
- File I/O issues

Each log entry includes:
- Error message
- Class/method name
- Line number
- Timestamp (implicit from file write order)

### User-Facing Errors
Users see:
- Clear, actionable error messages
- Error codes like `[ERR_NAME]`, `[ERR_RANGE]`
- Option to re-try or ESC to cancel

---

**Questions?** Explore the [Getting Started](/getting-started) guide or check out the [User Guides](/guides/guest).
