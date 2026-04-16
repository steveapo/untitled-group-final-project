#!/bin/bash
# Hotel Room Booking System Launcher (macOS / Linux)
# Ensures the JAR runs from the correct directory so data files are found.

cd "$(dirname "$0")/dist"
java --enable-native-access=ALL-UNNAMED -jar HotelBooking.jar
