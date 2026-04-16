@echo off
REM Hotel Room Booking System Launcher (Windows)
REM Forces UTF-8 output so Unicode glyphs (Braille spinner, box-drawing) render correctly.
chcp 65001 >nul
cd /d "%~dp0dist"
java --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar HotelBooking.jar
pause
