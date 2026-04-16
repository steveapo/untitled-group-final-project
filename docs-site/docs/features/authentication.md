# Authentication & Security

The Hotel Room Booking System implements secure authentication with role-based access control and cryptographic password hashing.

## Password Security

### SHA-512 Hashing with Salt

Every password is securely hashed using:
- **Algorithm:** SHA-512 (NIST-approved, cryptographically strong)
- **Random Salt:** Each account gets a unique salt (SecureRandom)
- **Non-reversible:** Passwords cannot be recovered from hashes

**Security flow:**
1. User enters password
2. System generates 16-byte random salt
3. Password + salt → SHA-512 hash
4. Hash and salt stored in Users file
5. Plain password is never stored

### Password Requirements

During registration:
- At least 8 characters
- Must contain at least one uppercase letter
- Must contain at least one lowercase letter
- Must contain at least one number
- Must contain at least one special character (!@#$%^&*)

Example valid passwords:
- `MyPassword123!`
- `SecurePass@456`
- `Hotel#Booking2024`

## Role-Based Access Control (RBAC)

Four distinct roles with isolated menus and permissions:

### Guest
- **Permissions:** View room inventory and rates
- **Menu:** Browse rooms, login, register
- **Database access:** Read-only (rooms, no booking/user access)
- **Authentication:** Not required

### User
- **Permissions:** Search, book, manage personal reservations
- **Menu:** Book rooms, view/cancel own bookings, view profile
- **Database access:** Can create/modify own bookings
- **Authentication:** Username + password required
- **Default role:** Assigned to all new registered accounts

### Reception
- **Permissions:** Handle guest check-in/out, create bookings, manage all bookings
- **Menu:** All booking operations, guest/room management
- **Database access:** Full read/write on bookings, read on rooms/users
- **Authentication:** Username + password required
- **Assigned by:** Manager account

### Manager
- **Permissions:** Full system control
- **Menu:** Room and staff management, all bookings, statistics
- **Database access:** Full read/write on all tables
- **Authentication:** Username + password required
- **Special:** Auto-seeded on first run (username `admin`, password `admin`)
- **Note:** Change default admin password immediately in production

## Login Flow

```
1. User enters username
   ↓
2. System searches Users file for matching username
   ↓
3a. NOT FOUND → Error, re-prompt
3b. FOUND → Continue
   ↓
4. Prompt for password (masked input with *)
   ↓
5. Hash(password + stored salt) = ?
   ↓
6a. Matches stored hash → SUCCESS, dispatch to role menu
6b. Does not match → ERROR, re-prompt
   ↓
7. ESC at any time → Cancel login
```

## Password Masking

When entering a password:
- Each keystroke is displayed as `*`
- Backspace works normally (deletes last character)
- ESC cancels password entry
- No plain-text password visible on screen

## Account Registration

Users can create new accounts:

1. **Username** — Unique identifier (letters/numbers, no spaces)
2. **First name** — Letters only
3. **Last name** — Letters only
4. **Email** — Must contain @ symbol
5. **Password** — Meeting all security requirements (see above)

**Validation:**
- Username must not already exist
- Names cannot contain numbers
- Email must be well-formed
- Password strength checked

## Session Management

### Security model
- No persistent sessions (stateless)
- Each menu action requires the authenticated Account object in memory
- Logout = return to main menu (no persistent token)
- No cookies or tokens (terminal application)

### Session timeout
- None — User must explicitly press ESC to logout
- Ideal for desktop/terminal environments where user is always present

## Data Storage

### Users File Format
```csv
username,firstName,lastName,email,hashedPassword,salt,role
admin,Admin,User,admin@hotel.com,<SHA512-HASH>,<SALT>,MANAGER
user1,John,Doe,john@example.com,<SHA512-HASH>,<SALT>,USER
reception,Reception,Staff,reception@hotel.com,<SHA512-HASH>,<SALT>,RECEPTION
```

**Important:**
- Passwords are hashed — even system admins cannot recover plain passwords
- Salt is hexadecimal-encoded 16-byte random value
- Role is plaintext (fine — it's not sensitive)

## Cross-Platform Considerations

### Password Input (Terminal-specific)

**macOS/Linux (JLine raw mode):**
- True password masking with character-by-character echo as `*`
- Backspace properly deletes characters
- No terminal echo of input

**Windows (VT mode enabled):**
- Same as macOS/Linux with Terminal/PowerShell 7+
- Legacy cmd.exe: Falls back to Scanner (less secure display)

**IDE Consoles (IntelliJ, VS Code):**
- Falls back to System.console().readPassword() if available
- Otherwise Scanner-based fallback (plaintext in some IDEs)

## Security Best Practices

### For Users
- Change default admin password immediately
- Use strong passwords (mix of characters, numbers, symbols)
- Do not share login credentials
- Logout (ESC) when finished, especially on shared terminals

### For Administrators
- Regularly back up the Users file
- Monitor Errors file for failed login attempts
- Audit account creation and role assignments
- Consider limiting manager accounts to single owner/admin

### For Deployment
- Protect the `Users` CSV file (it contains password hashes)
- Hash ≠ encryption, but hashes prevent easy password recovery
- Do not commit Users file to version control (add to .gitignore)
- Use the application in secure environments

---

**Never compromised:** Even if the Users file is stolen, password hashes are irreversible (within practical limits of cryptography).
