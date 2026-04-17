#!/bin/bash

###############################################################################
# Hotel Room Booking System - Seed Script (Optional Demo Data)
#
# Purpose: Add optional demo rooms and bookings to simulate a working
#          hotel environment with occupancy and reservation history.
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
# This script adds:
#   - 5 additional rooms (R105-R109)
#   - 12 sample bookings with various statuses (CHECKED_OUT, CHECKED_IN, CONFIRMED)
#     showing realistic occupancy patterns for the pre-loaded users
#
###############################################################################

JAR_FILE="dist/HotelBooking.jar"
DATA_DIR="dist"

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
    echo -e "${YELLOW}⚠ Warning:${NC} Data files not found in $DATA_DIR/"
    echo "Please run the main application first (./dist/run.sh) to initialize the system."
    exit 1
fi

# Check if seed data already exists
if grep -q "^R105," "$DATA_DIR/Rooms" 2>/dev/null; then
    echo -e "${YELLOW}⚠ Seed data already exists${NC} (R105 room found)"
    echo
    read -p "Delete and reseed? (yes/no): " -r reseed_response
    reseed_response=$(printf '%s' "$reseed_response" | sed 's/[[:space:]]*$//')

    if [[ "$reseed_response" =~ ^[Yy][Ee][Ss]?$ ]]; then
        echo "Backing up and cleaning seed data..."
        cp "$DATA_DIR/Rooms" "$DATA_DIR/Rooms.backup.$(date +%s)" 2>/dev/null || true
        cp "$DATA_DIR/Bookings" "$DATA_DIR/Bookings.backup.$(date +%s)" 2>/dev/null || true

        # Remove only the seeded demo rooms (R105+) and their bookings
        grep -v "^R10[5-9]," "$DATA_DIR/Rooms" > "$DATA_DIR/Rooms.tmp" && mv "$DATA_DIR/Rooms.tmp" "$DATA_DIR/Rooms"
        awk '!/^R10[5-9],/' "$DATA_DIR/Bookings" > "$DATA_DIR/Bookings.tmp" && mv "$DATA_DIR/Bookings.tmp" "$DATA_DIR/Bookings"
        echo -e "${GREEN}✓ Cleaned up existing seed data${NC}"
    else
        echo "Seeding cancelled."
        exit 0
    fi
fi

echo "This script will add demo data to simulate a working hotel environment:"
echo "  • 5 additional rooms (R105-R109)"
echo "  • 12 sample bookings showing occupancy and reservation history"
echo

read -p "Continue? (yes/no): " -r response
response=$(printf '%s' "$response" | sed 's/[[:space:]]*$//')

if [[ ! "$response" =~ ^[Yy][Ee][Ss]?$ ]]; then
    echo "Seeding cancelled."
    exit 0
fi

echo
echo -e "${GREEN}Adding additional rooms...${NC}"

cat >> "$DATA_DIR/Rooms" << 'EOF'
R105,2,199.99,Suite,AVAILABLE
R106,1,75.00,Single-Deluxe,AVAILABLE
R107,3,149.99,Triple-Deluxe,AVAILABLE
R108,4,199.99,Quad-Premium,AVAILABLE
R109,6,299.99,Penthouse,AVAILABLE
EOF

echo -e "${GREEN}✓ Added 5 additional rooms (R105-R109)${NC}"

echo
echo -e "${GREEN}Adding sample bookings to simulate occupancy...${NC}"

# Sample bookings showing realistic occupancy patterns
# Using pre-loaded users: admin, reception, user1
# Format: roomNumber,guestUsername,checkInDate,checkOutDate,totalPrice,status
cat >> "$DATA_DIR/Bookings" << 'EOF'
R101,user1,10-04-2026,13-04-2026,180.00,CHECKED_OUT
R101,admin,15-04-2026,17-04-2026,120.00,CHECKED_IN
R102,user1,12-04-2026,15-04-2026,270.00,CHECKED_OUT
R102,reception,17-04-2026,19-04-2026,180.00,CONFIRMED
R103,admin,08-04-2026,12-04-2026,480.00,CHECKED_OUT
R103,user1,20-04-2026,27-04-2026,840.00,CONFIRMED
R104,reception,11-04-2026,14-04-2026,450.00,CHECKED_OUT
R105,admin,16-04-2026,18-04-2026,400.00,CHECKED_IN
R106,user1,14-04-2026,16-04-2026,150.00,CHECKED_OUT
R107,reception,19-04-2026,22-04-2026,450.00,CONFIRMED
R108,admin,09-04-2026,13-04-2026,800.00,CHECKED_OUT
R109,user1,21-04-2026,24-04-2026,900.00,CONFIRMED
EOF

echo -e "${GREEN}✓ Added 12 sample bookings with realistic occupancy patterns${NC}"

echo
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}✓ Seeding complete!${NC}"
echo -e "${BLUE}================================================${NC}"
echo
echo "Your system now has:"
echo "  • 3 user accounts (admin, reception, user1)"
echo "  • 9 rooms total (R101-R109)"
echo "  • 12 sample bookings showing:"
echo "    - Past reservations (CHECKED_OUT)"
echo "    - Current reservations (CHECKED_IN)"
echo "    - Upcoming reservations (CONFIRMED)"
echo
echo "To access the system, run: ./dist/run.sh"
echo
