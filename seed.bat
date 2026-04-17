@echo off
REM ###############################################################################
REM # Hotel Room Booking System - Seed Script (Optional Demo Data)
REM #
REM # Purpose: Add optional demo rooms and bookings to simulate a working
REM #          hotel environment with occupancy and reservation history.
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
REM # This script adds:
REM #   - 5 additional rooms (R105-R109)
REM #   - 12 sample bookings with various statuses (CHECKED_OUT, CHECKED_IN, CONFIRMED)
REM #     showing realistic occupancy patterns for the pre-loaded users
REM #
REM ###############################################################################

setlocal enabledelayedexpansion

set JAR_FILE=dist\HotelBooking.jar
set DATA_DIR=dist

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

REM Check if seed data already exists
findstr /m "^R105," "%DATA_DIR%\Rooms" >nul 2>&1
if not errorlevel 1 (
    echo [WARNING] Seed data already exists (R105 room found)
    echo.
    set /p reseed_response="Reseed (delete and repopulate demo data)? (yes/no): "

    if /i "!reseed_response!"=="yes" (
        echo Backing up and cleaning seed data...
        REM Backup existing files
        for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
        copy "%DATA_DIR%\Rooms" "%DATA_DIR%\Rooms.backup.!mydate!" >nul 2>&1
        copy "%DATA_DIR%\Bookings" "%DATA_DIR%\Bookings.backup.!mydate!" >nul 2>&1

        REM Remove seeded rooms (R105+)
        for /f "tokens=*" %%%%i in ('findstr /v "^R10[5-9]," "%DATA_DIR%\Rooms"') do echo %%%%i >> "%DATA_DIR%\Rooms.tmp"
        move /y "%DATA_DIR%\Rooms.tmp" "%DATA_DIR%\Rooms" >nul 2>&1

        REM Remove seeded bookings
        for /f "tokens=*" %%%%i in ('findstr /v "^R10[5-9]," "%DATA_DIR%\Bookings"') do echo %%%%i >> "%DATA_DIR%\Bookings.tmp"
        move /y "%DATA_DIR%\Bookings.tmp" "%DATA_DIR%\Bookings" >nul 2>&1

        echo [OK] Cleaned up existing seed data
    ) else (
        echo Seeding skipped.
        exit /b 0
    )
)

echo This script will add demo data to simulate a working hotel environment:
echo   * 5 additional rooms (R105-R109)
echo   * 12 sample bookings showing occupancy and reservation history
echo.
set /p response="Continue? (yes/no): "

if /i not "!response!"=="yes" (
    if /i not "!response!"=="y" (
        echo Seeding cancelled.
        exit /b 0
    )
)

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
echo Adding sample bookings to simulate occupancy...

REM Sample bookings showing realistic occupancy patterns
REM Using pre-loaded users: admin, reception, user1
REM Format: roomNumber,guestUsername,checkInDate,checkOutDate,totalPrice,status

echo R101,user1,10-04-2026,13-04-2026,180.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R101,admin,15-04-2026,17-04-2026,120.00,CHECKED_IN >> "%DATA_DIR%\Bookings"
echo R102,user1,12-04-2026,15-04-2026,270.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R102,reception,17-04-2026,19-04-2026,180.00,CONFIRMED >> "%DATA_DIR%\Bookings"
echo R103,admin,08-04-2026,12-04-2026,480.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R103,user1,20-04-2026,27-04-2026,840.00,CONFIRMED >> "%DATA_DIR%\Bookings"
echo R104,reception,11-04-2026,14-04-2026,450.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R105,admin,16-04-2026,18-04-2026,400.00,CHECKED_IN >> "%DATA_DIR%\Bookings"
echo R106,user1,14-04-2026,16-04-2026,150.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R107,reception,19-04-2026,22-04-2026,450.00,CONFIRMED >> "%DATA_DIR%\Bookings"
echo R108,admin,09-04-2026,13-04-2026,800.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R109,user1,21-04-2026,24-04-2026,900.00,CONFIRMED >> "%DATA_DIR%\Bookings"

echo [OK] Added 12 sample bookings with realistic occupancy patterns

echo.
echo ================================================
echo [SUCCESS] Seeding complete!
echo ================================================
echo.
echo Your system now has:
echo   * 3 user accounts (admin, reception, user1)
echo   * 9 rooms total (R101-R109)
echo   * 12 sample bookings showing:
echo     - Past reservations (CHECKED_OUT)
echo     - Current reservations (CHECKED_IN)
echo     - Upcoming reservations (CONFIRMED)
echo.
echo To access the system, run: dist\run.bat
echo.
pause
