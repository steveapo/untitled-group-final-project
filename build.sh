#!/bin/bash
# Build script for Hotel Room Booking System (Maven-based)
# Works on macOS and Linux. Produces a fat JAR with JLine shaded in.

set -e

echo "=== Hotel Room Booking System - Build ==="
echo ""

echo "[1/2] Running mvn clean package..."
mvn -q clean package -DskipTests

mkdir -p dist
cp target/HotelBooking.jar dist/

# Copy data files into dist (preserve existing if not present at repo root)
cp Rooms Bookings Users dist/ 2>/dev/null || true
touch dist/Errors

# Copy launcher scripts
cp run.sh run.bat dist/ 2>/dev/null || true

echo "[2/2] Done!"
echo ""
echo "Output: dist/HotelBooking.jar"
echo "Run with: java -jar dist/HotelBooking.jar"
echo "  or use: ./dist/run.sh (macOS/Linux)"
echo "  or use:  dist\\run.bat (Windows)"
