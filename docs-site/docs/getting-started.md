# Getting Started

## Requirements

- **Java 11** or later ([download from Adoptium](https://adoptium.net/))

Verify your Java installation:

```bash
java -version
```

## Option 1: Download the Release (Recommended)

The easiest way to get started — everything is pre-packaged and ready to run.

<div style="display: flex; gap: 1rem; margin: 1.5rem 0;">
  <a href="https://github.com/steveapo/untitled-group-final-project/releases/download/v1.3/HotelBooking-v1.3.tar.gz" style="display: inline-block; padding: 0.75rem 1.5rem; background: var(--vp-c-brand-1); color: var(--vp-c-white); border-radius: 8px; font-weight: 600; text-decoration: none;">
    📦 Download for macOS/Linux
  </a>
  <a href="https://github.com/steveapo/untitled-group-final-project/releases/download/v1.3/HotelBooking-v1.3.zip" style="display: inline-block; padding: 0.75rem 1.5rem; background: var(--vp-c-brand-1); color: var(--vp-c-white); border-radius: 8px; font-weight: 600; text-decoration: none;">
    📦 Download for Windows
  </a>
</div>

### macOS / Linux Setup

```bash
# Extract the archive
tar -xzf HotelBooking-v1.3.tar.gz

# Run the application
./run.sh
```

### Windows Setup

```cmd
# Extract HotelBooking-v1.3.zip (right-click → Extract All)
# Open Command Prompt or PowerShell in the extracted folder
run.bat
```

### Package Contents

| File | Purpose |
|------|---------|
| `HotelBooking.jar` | The application executable (requires Java 11+) |
| `run.sh` | Launch script for macOS / Linux |
| `run.bat` | Launch script for Windows |

## Option 2: Build from Source

If you prefer to compile from source code:

```bash
# Clone the repository
git clone https://github.com/steveapo/untitled-group-final-project.git
cd untitled-group-final-project

# Build the project
./build.sh              # macOS / Linux
build.bat              # Windows
```

This compiles all source files and packages them into `dist/HotelBooking.jar`.

Run with:

```bash
cd dist
./run.sh               # macOS / Linux
run.bat                # Windows
```

## First Run

On first launch, the system automatically creates a default admin account if no `Users` file exists:

- **Username:** `admin`
- **Password:** `admin`

⚠️ **Important:** Change the admin password immediately if deploying to a shared environment.

## Data Files

The application stores data in CSV files in the same directory as the JAR:

| File | Contents |
|------|----------|
| `Users` | User accounts with hashed passwords and roles |
| `Rooms` | Room inventory (number, type, price, capacity, status) |
| `Bookings` | All reservations with status tracking |
| `Errors` | Validation error log |

These files are human-readable CSV format and can be edited with any text editor if needed.

## Demo Accounts

Try these pre-configured accounts (if you re-copy the provided data files):

| Username | Password | Role |
|----------|----------|------|
| `user1` | `user1` | User (Guest) |
| `user2` | `user1` | User (Guest) |
| `reception` | `reception` | Reception (Staff) |
| `admin` | `admin` | Manager (Admin) |

## Troubleshooting

### "Java not found"
Ensure Java 11+ is installed and in your PATH:
```bash
java -version
```

### Application won't start on Windows
- Try **Windows Terminal** or **PowerShell 7+** instead of cmd.exe
- Legacy cmd.exe has limited color support but will still work

### Missing data files
The application creates `Users`, `Rooms`, and `Bookings` files automatically on first run. If they're missing:
1. Delete all data files
2. Restart the application
3. Files will be recreated

---

**Ready?** Start with [Guest Mode](/guides/guest) or [create a User account](/guides/user).
