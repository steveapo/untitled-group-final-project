# Getting Started

## Requirements

- **Java 11** or later ([download from Adoptium](https://adoptium.net/))

Verify your Java installation:

```bash
java -version
```

## Option 1: Download the Release (Recommended)

1. Go to the [GitHub Releases page](https://github.com/steveapo/untitled-group-final-project/releases)
2. Download `HotelBooking-v1.1.zip`
3. Extract the zip file
4. Run the application:

```bash
# macOS / Linux
./run.sh

# Windows (double-click or run in terminal)
run.bat
```

The zip contains everything you need:

| File | Purpose |
|------|---------|
| `HotelBooking.jar` | The application |
| `run.sh` | macOS / Linux launcher |
| `run.bat` | Windows launcher |
| `Rooms` | Room data |
| `Bookings` | Booking data |
| `Users` | User accounts |

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
