#!/bin/bash
# Hotel Room Booking System Launcher (macOS / Linux)
# Ensures the JAR runs from the correct directory so data files are found.

cd "$(dirname "$0")"
java -jar HotelBooking.jar
