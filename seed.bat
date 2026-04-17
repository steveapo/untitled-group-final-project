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

echo This script will add optional dummy data to your system:
echo   * 5 additional user accounts (user2-user6)
echo   * 4 additional rooms (R105-R108)
echo   * Sample bookings for demo/testing
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

REM Append demo accounts to Users file
echo user2,Jane,Smith,user2@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER >> "%DATA_DIR%\Users"
echo user3,Bob,Johnson,user3@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER >> "%DATA_DIR%\Users"
echo user4,Alice,Williams,user4@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER >> "%DATA_DIR%\Users"
echo user5,Charlie,Brown,user5@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER >> "%DATA_DIR%\Users"
echo user6,Diana,Miller,user6@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER >> "%DATA_DIR%\Users"

echo [OK] Added 5 demo accounts (user2-user6, password: demo)

echo.
echo Adding additional rooms...

REM Append additional rooms to Rooms file
echo R105,2,199.99,Suite,AVAILABLE >> "%DATA_DIR%\Rooms"
echo R106,1,75.00,Single-Deluxe,AVAILABLE >> "%DATA_DIR%\Rooms"
echo R107,3,149.99,Triple-Deluxe,AVAILABLE >> "%DATA_DIR%\Rooms"
echo R108,4,199.99,Quad-Premium,AVAILABLE >> "%DATA_DIR%\Rooms"

echo [OK] Added 4 additional rooms (R105-R108)

echo.
echo Adding sample bookings for demonstration...

REM Calculate dates for bookings
REM Note: Windows batch doesn't have built-in date math, so we use fixed sample dates
REM Adjust these dates as needed for your testing

echo R101,user2,10-04-2026,13-04-2026,120.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"
echo R102,user3,17-04-2026,18-04-2026,180.00,CHECKED_IN >> "%DATA_DIR%\Bookings"
echo R103,user4,18-04-2026,25-04-2026,840.00,CONFIRMED >> "%DATA_DIR%\Bookings"
echo R105,user5,10-04-2026,13-04-2026,400.00,CHECKED_OUT >> "%DATA_DIR%\Bookings"

echo [OK] Added 4 sample bookings with various statuses

echo.
echo ================================================
echo [SUCCESS] Seeding complete!
echo ================================================
echo.
echo Your system now has:
echo   * 8 user accounts total (admin, reception, user1-user6)
echo   * 8 rooms total (R101-R108)
echo   * 4 sample bookings for testing/demo
echo.
echo Demo accounts use password 'demo' for testing.
echo To access the system, run: dist\run.bat
echo.
pause
