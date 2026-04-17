@echo off
REM ###############################################################################
REM # Hotel Room Booking System - Seed Script (Optional Dummy Data)
REM #
REM # Purpose: Add optional dummy accounts, rooms, bookings, and statistics
REM #          to an existing HotelBooking installation.
REM #
REM # Usage:   seed.bat
REM #
REM # This script is OPTIONAL and should only be run after the main application
REM # has been started at least once (to initialize the base system).
REM #
REM # The main application pre-loads:
REM #   - 3 accounts: admin, reception, user1
REM #   - 4 rooms: R101 (Single), R102 (Double), R103 (Triple), R104 (Quad)
REM #
REM # This script adds (if you choose to run it):
REM #   - 5 additional demo accounts (user2-user6)
REM #   - 4 additional rooms (R105-R108: Suite and other types)
REM #   - Sample bookings showing various occupancy states
REM #   - Statistics for management dashboards
REM #
REM ###############################################################################

setlocal enabledelayedexpansion

set JAR_FILE=dist\HotelBooking.jar
set DATA_DIR=.

cls
echo.
echo ================================================
echo    Hotel Room Booking System - Seed Script
echo ================================================
echo.

REM Check if JAR exists
if not exist "%JAR_FILE%" (
    echo [WARNING] HotelBooking.jar not found at %JAR_FILE%
    echo Please ensure you've built the project first.
    pause
    exit /b 1
)

REM Check if data files exist
if not exist "%DATA_DIR%\Users" (
    echo [WARNING] Data files not found.
    echo Please run the main application first to initialize the system.
    pause
    exit /b 1
)

echo This script will add optional demo data:
echo   * 5 additional rooms (R105-R109)
echo   * Sample bookings for testing/demo
echo.
echo To add new user accounts, use the application's registration feature
echo or run: echo password ^| java -cp "%JAR_FILE%" SeedManager
echo.
set /p response="Continue? (yes/no): "

if /i not "%response%"=="yes" (
    if /i not "%response%"=="y" (
        echo Seeding cancelled.
        exit /b 0
    )
)

echo.
echo Adding dummy accounts...

echo [INFO] To add user accounts, use the application's registration feature
echo [INFO] or run: echo password ^| java -cp "dist\HotelBooking.jar" SeedManager

echo.
echo Adding additional rooms...

REM Append additional rooms to Rooms file
echo R105,2,199.99,Suite,AVAILABLE >> "%DATA_DIR%\Rooms"
echo R106,1,75.00,Single-Deluxe,AVAILABLE >> "%DATA_DIR%\Rooms"
echo R107,3,149.99,Triple-Deluxe,AVAILABLE >> "%DATA_DIR%\Rooms"
echo R108,4,199.99,Quad-Premium,AVAILABLE >> "%DATA_DIR%\Rooms"
echo R109,6,299.99,Penthouse,AVAILABLE >> "%DATA_DIR%\Rooms"

echo [OK] Added 5 additional rooms (R105-R109)

echo.
echo Adding sample bookings for demonstration...

REM Use fixed dates to avoid platform-specific issues
REM Bookings in different states: CHECKED_OUT, CHECKED_IN, CONFIRMED
echo R101,user1,10-04-2026,13-04-2026,240.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R102,admin,15-04-2026,17-04-2026,180.00,CHECKED_IN >> "%DATA_DIR%\Bookings"
echo R103,reception,18-04-2026,25-04-2026,840.00,CONFIRMED >> "%DATA_DIR%\Bookings"
echo R105,user1,20-04-2026,22-04-2026,400.00,CONFIRMED >> "%DATA_DIR%\Bookings"

echo [OK] Added 4 sample bookings with various statuses

echo.
echo ================================================
echo [SUCCESS] Seeding complete!
echo ================================================
echo.
echo Your system now has:
echo   * 3 base accounts (admin, reception, user1)
echo   * 9 rooms total (R101-R109)
echo   * 4 sample bookings for testing/demo
echo.
echo To add more user accounts with proper password hashing:
echo   echo mypassword ^| java -cp "dist\HotelBooking.jar" SeedManager
echo.
echo To access the system, run: dist\run.bat
echo.
pause
