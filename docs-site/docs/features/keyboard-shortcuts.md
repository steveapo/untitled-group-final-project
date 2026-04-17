# Keyboard Shortcuts & Navigation

## Universal ESC Behavior

**ESC (Escape key) always cancels and returns to the previous state**, no matter what you're doing. **ESC is the only cancel key in a real terminal** — letters are never reserved, so `e`, `q`, and every other character are free as legitimate input (e.g. a username or password containing `e` works as expected).

| Context | Action |
|---------|--------|
| **Input field** | Cancel input and return to previous menu |
| **Menu screen** | Return to parent menu or logout |
| **Calendar navigation** | Cancel selection and return to previous menu |
| **Password entry** | Cancel password input and return to previous menu |
| **Confirmation dialog** | Cancel action and return to previous menu |
| **List selection** | Cancel selection and return to previous menu |

### IDE console fallback

IDE consoles (IntelliJ, VS Code's built-in terminal) and piped stdin cannot transmit a real Escape byte. In that environment only, typing the single letter `e` on its own line acts as a cancel sentinel. Real terminals (macOS Terminal, iTerm2, Windows Terminal, PowerShell 7+, Linux tty/xterm) use the native ESC key and leave `e` fully available as input.

### Examples

**Booking a room:**
- If you press ESC while entering guest count → Return to User menu
- If you press ESC while navigating calendar → Return to User menu
- If you press ESC at confirmation → Return to User menu (booking NOT created)

**Logging in:**
- If you press ESC at username prompt → Return to main menu
- If you press ESC at password prompt → Return to main menu

**Checking in a guest:**
- If you press ESC at username prompt → Return to Reception menu
- If you press ESC while selecting booking → Return to Reception menu

---

## Menu Navigation

### Single Keypress (No Enter Required)

At main menus, press a number to select:

```
  1. Continue as Guest
  2. Login
  3. Register

Choice: █
```

- Press `1`, `2`, or `3` (no Enter needed)
- Press `Esc` to go back or logout
- Invalid keys are ignored

### Arrow Key Navigation

When navigating lists or selecting from options:

| Key | Action |
|-----|--------|
| **↑** | Move up |
| **↓** | Move down |
| **←** | Move left (calendars) |
| **→** | Move right (calendars) |
| **Enter** | Select highlighted option |
| **Esc** | Cancel and go back |

### Vim Keybindings

Power users can use Vim keys in addition to arrows:

| Key | Action |
|-----|--------|
| **h** | Left (←) |
| **j** | Down (↓) |
| **k** | Up (↑) |
| **l** | Right (→) / Week forward (non-staff calendar) |
| **H** | Shift+Left (week back) |
| **L** | **Staff calendar only:** remove maintenance from selected range |

Works in menus and calendar navigation.

### Week & Month Navigation

In the occupancy calendar:

| Key | Action |
|-----|--------|
| **Shift+←** | Jump back one week |
| **Shift+→** | Jump forward one week |
| **Tab** | Jump forward one month |
| **Shift+Tab** | Jump back one month |

Use Shift+Arrow for week-by-week scanning and Tab/Shift+Tab when you need to jump across months quickly.

---

## Text Input Fields

### Basic Editing

When typing in a text field (username, email, password, etc.):

| Key | Action |
|-------|--------|
| **Backspace** | Delete last character |
| **Enter / Return** | Submit input |
| **Esc** | Cancel and go back |

### Password Input

Passwords are masked as you type:

```
Enter your password (Esc to go back): ****
```

- Each keystroke displays as `*`
- Backspace deletes one character
- ESC cancels password entry

### Text Validation

Some fields validate your input:

```
Enter your first name (Esc to go back): John123
✘ First name must contain only letters.

Press any key to continue...
```

Invalid input shows error and **re-prompts the same field** (doesn't cancel):
- Press any key after error
- Same field re-appears
- Press ESC to cancel and go back to menu

---

## Calendar Navigation

### Selecting Dates

```
╔══════════════════════════════════════╗
║  MARCH 2026 - OCCUPANCY CALENDAR     ║
╚══════════════════════════════════════╝

       Mon Tue Wed Thu Fri Sat Sun
  1   │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │
  2   │ ░░ │ ██ │ ░░ │ ██ │ ░░ │ ██ │ ░░ │

  ↑↓ Navigate  Enter Select  Shift+→ Week  Esc Cancel
```

| Key | Action |
|-----|--------|
| **↑ ↓ ← →** | Move one day at a time |
| **Shift+→** | Jump forward one week |
| **Shift+←** | Jump backward one week |
| **Tab** | Jump forward one month |
| **Shift+Tab** | Jump backward one month |
| **T** | Jump to today |
| **Enter** | Select the current date |
| **Esc** | Cancel date selection and return |

### Color Meanings

**Idle cells** (not under the cursor):

- **Dotted green (░░)** = Available
- **Solid green (██)** = Booked (staff view)
- **Dotted red (░░)** = Unavailable (user view)
- **Dotted pink / purple (▒▒)** = Maintenance

**Cursor cells** (the currently-selected cell):

- **Solid green (██)** on an available day
- **Solid white (██)** on a booked day — contrasts against the solid-green idle booking so the cursor never disappears as you navigate across occupied rooms
- **Solid purple (██)** on a maintenance day
- **Solid red (██)** on an unavailable day (user view)

The cursor always uses a *different fill or colour* from the idle cell beneath it, so its position is visually unambiguous whatever status the underlying cell is.

---

## Confirmation Dialogs

When asked to confirm an action:

```
Confirm? (yes/no, Esc to cancel): █
```

| Input | Action |
|-------|--------|
| **yes** or **y** | Confirm and proceed |
| **no** or **n** | Cancel (same as ESC) |
| **Esc** | Cancel |
| **Any other key** | Re-prompt (doesn't cancel) |

### Invalid Input Behavior

Invalid input doesn't cancel the operation:

```
Confirm? (yes/no, Esc to cancel): maybe
✘ Please enter 'yes' or 'no'.

Press any key to continue...
[field re-appears]
Confirm? (yes/no, Esc to cancel): █
```

This is intentional — you must explicitly press ESC to cancel, not just type a wrong key.

---

## Quick Reference Card

### Always Available
- **Esc** — Go back / Cancel / Logout (the only cancel key in a real terminal)

### Menu Screens
- **1–9** — Select menu option (single keypress, no Enter)
- **C** — Open the occupancy calendar (where offered)
- **Esc** — Go back to parent menu or logout

### Occupancy Calendar (staff)
- **← → ↑ ↓** — Move by day / room
- **Shift+← →** — Jump by week
- **Tab / Shift+Tab** — Jump forward / back one month
- **T** — Jump to today
- **M** — Start / confirm a maintenance date range
- **L** — Remove maintenance: press on start of range, navigate to end, press L again
- **Esc** — Back (or cancel an in-progress M or L operation)

### List Selection
- **↑ ↓** — Navigate
- **Enter** — Select
- **Esc** — Cancel

### Calendar Navigation
- **↑ ↓ ← →** — Move day-by-day
- **Shift+← →** — Jump by week
- **Tab / Shift+Tab** — Jump forward / back one month
- **T** — Jump to today
- **h/j/k/l** — Vim keybindings
- **Enter** — Select date
- **Esc** — Cancel

### Text Input
- **Backspace** — Delete character
- **Enter** — Submit
- **Esc** — Cancel and go back

### Confirmation
- **y / yes** — Confirm
- **n / no** — Decline
- **Esc** — Cancel
- **Other** — Re-prompt

---

## Platform Differences

### macOS / Linux / Windows Terminal
- All keyboard shortcuts work as documented
- Real-time feedback (no Enter needed for menu selection)
- Full ANSI color support

### Windows Legacy (cmd.exe)
- Keyboard shortcuts still work
- May need to press Enter after some inputs
- No color support (text-only output)
- Numbered menus instead of arrow keys

### IDE Consoles
- Basic keyboard support
- May require Enter after inputs
- Fallback to numbered menus
- Limited formatting

---

**Note:** ESC is your universal "undo" button. If you make a mistake at any screen or input, just press ESC to go back!
