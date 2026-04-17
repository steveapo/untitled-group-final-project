#!/bin/bash
# Hotel Room Booking System Launcher (macOS / Linux)
#
# Works from either location:
#   - repo root:  ./run.sh            → launches dist/HotelBooking.jar
#   - dist copy:  ./dist/run.sh       → launches ./HotelBooking.jar (same dir)
# Data files (Users / Rooms / Bookings / Errors) are resolved next to the JAR.

set -e
cd "$(dirname "$0")"

if [ -f "HotelBooking.jar" ]; then
    : # script already sits beside the JAR (e.g. inside dist/)
elif [ -f "dist/HotelBooking.jar" ]; then
    cd dist
else
    echo "HotelBooking.jar not found. Run ./build.sh first." >&2
    exit 1
fi

exec java --enable-native-access=ALL-UNNAMED -jar HotelBooking.jar
