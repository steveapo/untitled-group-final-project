# Authentication & Security

## Password Hashing

All passwords are hashed using **SHA-512** with a random 16-byte salt before storage. The process:

1. Generate a cryptographically secure random salt using `SecureRandom`
2. Prepend the salt to the password bytes
3. Hash with `SHA-512` via `MessageDigest`
4. Store both the hash and salt as **Base64-encoded** strings in the Users file

During login, the same process is applied to the entered password using the stored salt, then compared using `MessageDigest.isEqual()` for **constant-time comparison** (prevents timing attacks).

## Password Input Masking

Password input is masked for security:

| Platform | Behaviour |
|----------|-----------|
| macOS / Linux terminal | Live `****` asterisks as you type |
| Windows PowerShell / Terminal | Input hidden (no visible characters) |
| IDE (IntelliJ, VS Code) | Plain text fallback |

The implementation uses `stty` raw terminal mode on Unix systems for character-by-character masking, with `System.console().readPassword()` as the Windows fallback.

## Role-Based Access Control

Four access levels control what each user can do:

| Role | Access Level |
|------|-------------|
| Guest (no account) | View rooms only |
| USER | Book rooms, view/cancel own bookings |
| RECEPTION | Full booking management, check-in/out, room status |
| MANAGER | Room CRUD, staff management, statistics, all bookings |

## Input Validation

The system validates all user input:

- **Username** — must be unique (case-insensitive check)
- **Names** — letters only, no numbers or special characters
- **Email** — validated against RFC-compliant regex pattern
- **Dates** — strict `dd-MM-yyyy` format with invalid date rejection (e.g. Feb 30th)

All validation errors are logged to the `Errors` file with class name and line number.

## Auto-Seeding

On first run, if no admin account exists, the system automatically creates one:
- Username: `admin`
- Password: `admin`
- Role: `MANAGER`

This ensures the application is immediately usable without manual setup.
