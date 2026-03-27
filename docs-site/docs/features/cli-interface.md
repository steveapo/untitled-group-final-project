# CLI Interface

The application features a rich terminal interface built entirely with ANSI escape codes and custom Java utilities.

## ANSI Colour Support

The CLI automatically detects terminal capabilities:

- **Colour terminals** — full ANSI colour output (macOS Terminal, iTerm2, Windows Terminal, PowerShell 7+)
- **NO_COLOR environment variable** — colours disabled when set ([no-color.org](https://no-color.org))
- **Dumb terminals** — plain text fallback

### Colour Palette

| Function | Colour | Used For |
|----------|--------|----------|
| `success()` | Bold Green | Confirmation messages |
| `error()` | Bold Red | Error messages |
| `warning()` | Bold Yellow | Warnings and validation errors |
| `header()` | Bold Cyan | Section headings and banners |
| `prompt()` | Magenta | Input prompts |
| `dim()` | Dim | Secondary information |

## Interactive Selectors

For status toggles and guest selection, the CLI provides an **arrow-key navigator**:

```
  ▸ AVAILABLE        ← highlighted in magenta
    MAINTENANCE
  ↑↓ Navigate  Enter Select  Esc Cancel
```

Features:
- **Arrow keys** to move up and down
- **Enter** to confirm selection
- **Escape** or **e** to cancel
- Current value is **pre-highlighted** when editing
- **Wraps around** — pressing down on the last item goes to the first

Falls back to a numbered list on Windows or in IDEs where `stty` is unavailable.

## Loading Spinners

Operations that take time show an animated spinner:

- **Braille spinner** (`⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏`) on capable terminals
- **ASCII fallback** (`|/-\`) on basic terminals
- Completion shown with a green checkmark: `✔`

Used for: loading data, saving bookings, login transitions, room updates.

## Screen Clearing

The terminal is cleared between menu transitions using:
- `\033[H\033[2J` (ANSI clear) on supported terminals
- 40 blank lines as fallback on dumb terminals

## Box-Drawing Banners

Section headers are displayed in Unicode box-drawing frames:

```
╔══════════════════════════════════════╗
║         HOTEL ROOM BOOKING SYSTEM    ║
╚══════════════════════════════════════╝
```

## Cross-Platform Compatibility

| Feature | macOS / Linux | Windows Terminal | IDE |
|---------|--------------|-----------------|-----|
| ANSI colours | Full | Full | Depends on IDE |
| Spinners | Braille | ASCII fallback | ASCII fallback |
| Password masking | Live asterisks | Hidden input | Plain text |
| Arrow-key selector | Full | Numbered fallback | Numbered fallback |
| Screen clearing | ANSI escape | ANSI escape | Blank lines |
