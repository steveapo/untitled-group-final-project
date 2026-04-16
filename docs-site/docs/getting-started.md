# Getting Started

## Requirements

- **Java 11** or later ([download from Adoptium](https://adoptium.net/))

Verify your Java installation:

```bash
java -version
```

## Option 1: Download the Release (Recommended)

1. Go to the [GitHub Releases page](https://github.com/steveapo/untitled-group-final-project/releases)
2. Download the latest release:
   - **macOS/Linux:** `HotelBooking-v1.0.tar.gz`
   - **Windows:** `HotelBooking-v1.0.zip`
3. Extract the archive
4. Run the application:

```bash
# macOS / Linux
tar -xzf HotelBooking-v1.0.tar.gz
./run.sh

# Windows (double-click or run in terminal)
# Extract HotelBooking-v1.0.zip
run.bat
```

The package contains everything you need:

| File | Purpose |
|------|---------|
| `HotelBooking.jar` | The application (executable) |
| `run.sh` | macOS / Linux launcher script |
| `run.bat` | Windows launcher script |

### What's New in v1.0

- ✨ **Interactive calendar**: Visual date picker with arrow key navigation
- ✨ **Smart confirmation**: Input validation loop that doesn't cancel on invalid input
- ✨ **Perfect alignment**: Fixed calendar header and cell highlighting
- ✨ **Cross-platform**: Full ANSI support on macOS/Linux, graceful fallback on Windows

## Option 2: Build from Source

Clone the repository and build:

```bash
git clone https://github.com/steveapo/untitled-group-final-project.git
cd untitled-group-final-project

# macOS / Linux
./build.sh

# Windows
build.bat
```

This compiles all source files, packages them into `dist/HotelBooking.jar`, and copies the data files into `dist/`.

Run with:

```bash
cd dist
java -jar HotelBooking.jar
```

## First Run

On first launch, the system automatically seeds a default admin account if no `Users` file exists. You can log in with:

- **Username:** `admin`
- **Password:** `admin`

## Data Files

The application stores data in CSV files in the same directory as the JAR:

| File | Contents |
|------|----------|
| `Users` | User accounts with hashed passwords |
| `Rooms` | Room inventory (number, type, price, status) |
| `Bookings` | All reservations with status tracking |
| `Errors` | Validation error log |

These files are human-readable CSV and can be inspected with any text editor.
