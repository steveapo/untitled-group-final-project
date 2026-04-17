# Getting Started

## Requirements

- **Java 11** or later ([download from Adoptium](https://adoptium.net/))

Verify your Java installation:

```bash
java -version
```

## Option 1: Download the Release (Recommended)

The easiest way to get started — everything is pre-packaged and ready to run.

- **[📦 Download for macOS/Linux](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.4/HotelBooking-v1.4.tar.gz)**
- **[📦 Download for Windows](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.4/HotelBooking-v1.4.zip)**

### macOS / Linux Setup

```bash
# Extract the archive
tar -xzf HotelBooking-v1.4.tar.gz

# Run the application
./run.sh
```

### Windows Setup

```cmd
# Extract HotelBooking-v1.4.zip (right-click → Extract All)
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

On first launch, the system automatically initializes with:

- **3 default accounts:** 
  - `admin` / `admin` (Manager role)
  - `reception` / `reception` (Reception role)
  - `user1` / `user1` (User role)
- **4 default rooms:** R101 (Single), R102 (Double), R103 (Triple), R104 (Quad)
- **Empty bookings file** ready for reservations

⚠️ **Important:** Change default passwords immediately if deploying to a shared environment.

### Adding Demo Data (Optional)

The main application starts clean with minimal data. If you want to add optional demo accounts, rooms, and sample bookings for testing:

**macOS / Linux:**
```bash
./seed.sh
```

**Windows:**
```cmd
seed.bat
```

This adds 5 extra user accounts (user2-user6), 4 additional rooms (R105-R108), and sample bookings showing various occupancy states.

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

These accounts are pre-configured on first run:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin` | Manager |
| `reception` | `reception` | Reception |
| `user1` | `user1` | User |

To add more demo accounts for testing, run the seed script:

| Username | Password | Role |
|----------|----------|------|
| `user2` | `demo` | User |
| `user3` | `demo` | User |
| `user4` | `demo` | User |
| `user5` | `demo` | User |
| `user6` | `demo` | User |

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
