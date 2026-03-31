@echo off
REM Hotel Room Booking System Launcher (Windows)
REM Ensures the JAR runs from the correct directory so data files are found.

cd /d "%~dp0dist"
java -jar HotelBooking.jar
pause
