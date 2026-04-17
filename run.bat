@echo off
REM Hotel Room Booking System Launcher (Windows)
REM
REM Works from either location:
REM   - repo root:  run.bat            -> launches dist\HotelBooking.jar
REM   - dist copy:  dist\run.bat       -> launches .\HotelBooking.jar (same dir)
REM
REM chcp 65001 forces UTF-8 output so Unicode glyphs (Braille spinner,
REM box-drawing characters) render correctly in cmd.exe / Windows Terminal.

chcp 65001 >nul
cd /d "%~dp0"

if exist "HotelBooking.jar" goto :launch
if exist "dist\HotelBooking.jar" (
    cd /d "%~dp0dist"
    goto :launch
)

echo HotelBooking.jar not found. Run build.bat first.
pause
exit /b 1

:launch
java --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar HotelBooking.jar
pause
