#!/bin/bash
# Build script for Hotel Room Booking System
# Works on macOS and Linux

set -e

echo "=== Hotel Room Booking System - Build ==="
echo ""

# Clean previous build
rm -rf build dist
mkdir -p build dist

# Compile all Java source files
echo "[1/3] Compiling Java sources..."
javac -d build src/*.java

# Create manifest
echo "[2/3] Creating JAR..."
echo "Main-Class: Main" > build/MANIFEST.MF

# Package into runnable JAR
cd build
jar cfm ../dist/HotelBooking.jar MANIFEST.MF *.class
cd ..

# Copy data files into dist
cp Rooms Bookings Users dist/ 2>/dev/null || true
touch dist/Errors

# Copy launcher scripts
cp run.sh run.bat dist/ 2>/dev/null || true

echo "[3/3] Done!"
echo ""
echo "Output: dist/HotelBooking.jar"
echo "Run with: java -jar dist/HotelBooking.jar"
echo "  or use: ./dist/run.sh (macOS/Linux)"
echo "  or use:  dist\\run.bat (Windows)"
