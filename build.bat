@echo off
REM Build script for Hotel Room Booking System (Maven-based)
REM Works on Windows (PowerShell / Command Prompt)

echo === Hotel Room Booking System - Build ===
echo.

call mvn clean package
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

if not exist dist mkdir dist
copy target\HotelBooking.jar dist\ >nul

REM Copy data files into dist
copy Rooms dist\ >nul 2>&1
copy Bookings dist\ >nul 2>&1
copy Users dist\ >nul 2>&1
type nul > dist\Errors

REM Copy launcher
copy run.bat dist\ >nul 2>&1

echo Done!
echo.
echo Output: dist\HotelBooking.jar
echo Run with: java -jar dist\HotelBooking.jar
echo   or use: dist\run.bat
pause
