@echo off
REM Build script for Hotel Room Booking System
REM Works on Windows (PowerShell / Command Prompt)

echo === Hotel Room Booking System - Build ===
echo.

REM Clean previous build
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build
mkdir dist

REM Compile all Java source files
echo [1/3] Compiling Java sources...
javac -d build src\*.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Create manifest
echo [2/3] Creating JAR...
echo Main-Class: Main> build\MANIFEST.MF

REM Package into runnable JAR
cd build
jar cfm ..\dist\HotelBooking.jar MANIFEST.MF *.class
cd ..

REM Copy data files into dist
copy Rooms dist\ >nul 2>&1
copy Bookings dist\ >nul 2>&1
copy Users dist\ >nul 2>&1
type nul > dist\Errors

REM Copy launcher
copy run.bat dist\ >nul 2>&1

echo [3/3] Done!
echo.
echo Output: dist\HotelBooking.jar
echo Run with: java -jar dist\HotelBooking.jar
echo   or use: dist\run.bat
pause
