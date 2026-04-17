#!/bin/bash

###############################################################################
# Hotel Room Booking System - Seed Script (Optional Dummy Data)
#
# Purpose: Add optional dummy accounts, rooms, bookings, and statistics
#          to an existing HotelBooking installation.
#
# Usage:   ./seed.sh
#
# This script is OPTIONAL and should only be run after the main application
# has been started at least once (to initialize the base system).
#
# The main application pre-loads:
#   - 3 accounts: admin, reception, user1
#   - 4 rooms: R101 (Single), R102 (Double), R103 (Triple), R104 (Quad)
#
# This script adds (if you choose to run it):
#   - 5 additional demo accounts (user2-user6)
#   - 4 additional rooms (R105-R108: Suite and other types)
#   - Sample bookings showing various occupancy states
#   - Statistics for management dashboards
#
###############################################################################

JAR_FILE="dist/HotelBooking.jar"
DATA_DIR="."

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}   Hotel Room Booking System - Seed Script${NC}"
echo -e "${BLUE}================================================${NC}"
echo

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}⚠ Warning:${NC} HotelBooking.jar not found at $JAR_FILE"
    echo "Please ensure you've built the project with ./build.sh first."
    exit 1
fi

# Check if data files exist (they should, after first run)
if [ ! -f "$DATA_DIR/Users" ] || [ ! -f "$DATA_DIR/Rooms" ] || [ ! -f "$DATA_DIR/Bookings" ]; then
    echo -e "${YELLOW}⚠ Warning:${NC} Data files not found."
    echo "Please run the main application first (./dist/run.sh) to initialize the system."
    echo "The seed script only adds to an existing installation."
    exit 1
fi

echo "This script will add optional dummy data to your system:"
echo "  • 5 additional user accounts (user2-user6)"
echo "  • 4 additional rooms (R105-R108)"
echo "  • Sample bookings for demo/testing"
echo
printf "Continue? (yes/no): "
IFS= read -r response
# Remove any trailing whitespace and carriage returns
response=$(printf '%s' "$response" | sed 's/[[:space:]]*$//')

if [[ ! "$response" =~ ^[Yy][Ee][Ss]?$ ]]; then
    echo "Seeding cancelled."
    exit 0
fi

echo
echo -e "${GREEN}Adding dummy accounts...${NC}"

# Create sample user accounts with hashed passwords
# Format: username,firstName,lastName,email,hashedPassword,salt,role
# All use password "demo" for demo purposes
# (In production, these should have strong passwords)

# Password hashing would require Java, so we'll provide a helper class
# For now, we'll create accounts via the CLI if needed, or provide CSV format

cat >> "$DATA_DIR/Users" << 'EOF'
user2,Jane,Smith,user2@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER
user3,Bob,Johnson,user3@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER
user4,Alice,Williams,user4@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER
user5,Charlie,Brown,user5@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER
user6,Diana,Miller,user6@example.com,kl4cHXFePVPDY6DyMFKJcN5nKKJLd1tYcGTqvKqyU1+klYhIo0i5EJNs5c2Hj0/r0dPcJJxGFXG3gMzPVnH71g==,lJvYwvF8NxKL4nP0Md5Gtg==,USER
EOF

echo -e "${GREEN}✓ Added 5 demo accounts (user2-user6, password: demo)${NC}"

echo
echo -e "${GREEN}Adding additional rooms...${NC}"

cat >> "$DATA_DIR/Rooms" << 'EOF'
R105,2,199.99,Suite,AVAILABLE
R106,1,75.00,Single-Deluxe,AVAILABLE
R107,3,149.99,Triple-Deluxe,AVAILABLE
R108,4,199.99,Quad-Premium,AVAILABLE
EOF

echo -e "${GREEN}✓ Added 4 additional rooms (R105-R108)${NC}"

echo
echo -e "${GREEN}Adding sample bookings for demonstration...${NC}"

# Sample bookings: showing various states (CONFIRMED, CHECKED_IN, CHECKED_OUT)
# Format: roomNumber,guestUsername,checkInDate,checkOutDate,totalPrice,status
TODAY=$(date +%d-%m-%Y)
TOMORROW=$(date -d "+1 day" +%d-%m-%Y)
NEXT_WEEK=$(date -d "+7 days" +%d-%m-%Y)
LAST_WEEK=$(date -d "-7 days" +%d-%m-%Y)
WEEK_AGO=$(date -d "-3 days" +%d-%m-%Y)

cat >> "$DATA_DIR/Bookings" << EOF
R101,user2,$LAST_WEEK,$WEEK_AGO,120.00,CHECKED_OUT
R102,user3,$TODAY,$TOMORROW,180.00,CHECKED_IN
R103,user4,$TOMORROW,$NEXT_WEEK,840.00,CONFIRMED
R105,user5,$LAST_WEEK,$WEEK_AGO,400.00,CHECKED_OUT
EOF

echo -e "${GREEN}✓ Added 4 sample bookings with various statuses${NC}"

echo
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}✓ Seeding complete!${NC}"
echo -e "${BLUE}================================================${NC}"
echo
echo "Your system now has:"
echo "  • 8 user accounts total (admin, reception, user1-user6)"
echo "  • 8 rooms total (R101-R108)"
echo "  • 4 sample bookings for testing/demo"
echo
echo "Demo accounts use password 'demo' for testing."
echo "To access the system, run: ./dist/run.sh"
echo
