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
# This script allows you to interactively add more accounts via the app,
# and adds sample rooms and bookings for testing/demo purposes.
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

# Check if seed data already exists
if grep -q "user2" "$DATA_DIR/Users" 2>/dev/null; then
    echo -e "${YELLOW}⚠ Seed data already exists${NC} (user2 account found)"
    echo
    printf "Reset and reseed? (yes/no): "
    IFS= read -r reseed_response
    reseed_response=$(printf '%s' "$reseed_response" | sed 's/[[:space:]]*$//')

    if [[ "$reseed_response" =~ ^[Yy][Ee][Ss]?$ ]]; then
        echo "Backing up and cleaning seed data..."
        cp "$DATA_DIR/Users" "$DATA_DIR/Users.backup.$(date +%s)" 2>/dev/null || true

        # Remove only the seeded demo accounts (user2-user6) and rooms (R105+)
        grep -v "^user[2-6]," "$DATA_DIR/Users" > "$DATA_DIR/Users.tmp" && mv "$DATA_DIR/Users.tmp" "$DATA_DIR/Users"
        grep -v "^R10[5-9]," "$DATA_DIR/Rooms" > "$DATA_DIR/Rooms.tmp" && mv "$DATA_DIR/Rooms.tmp" "$DATA_DIR/Rooms"
        # Clean bookings with demo users
        awk '!/^R10[1-9],user[2-6],/' "$DATA_DIR/Bookings" > "$DATA_DIR/Bookings.tmp" && mv "$DATA_DIR/Bookings.tmp" "$DATA_DIR/Bookings"
        echo -e "${GREEN}✓ Cleaned up existing seed data${NC}"
    else
        echo "Seeding skipped."
        exit 0
    fi
fi

echo "This script will add optional demo data:"
echo "  • 5 additional rooms (R105-R109)"
echo "  • Sample bookings for testing/demo"
echo
echo "To add new user accounts, use the application's registration feature"
echo "or run: echo 'password' | java -cp \"$JAR_FILE\" SeedManager"
echo
printf "Continue? (yes/no): "
IFS= read -r response
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
echo -e "${GREEN}Adding sample bookings for demonstration...${NC}"

# Use fixed dates to avoid platform-specific date math issues
# Bookings in different states: CHECKED_OUT, CHECKED_IN, CONFIRMED
cat >> "$DATA_DIR/Bookings" << 'EOF'
R101,user1,10-04-2026,13-04-2026,240.00,CHECKED_OUT
R102,admin,15-04-2026,17-04-2026,180.00,CHECKED_IN
R103,reception,18-04-2026,25-04-2026,840.00,CONFIRMED
R105,user1,20-04-2026,22-04-2026,400.00,CONFIRMED
EOF

echo -e "${GREEN}✓ Added 4 sample bookings with various statuses${NC}"

echo
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}✓ Seeding complete!${NC}"
echo -e "${BLUE}================================================${NC}"
echo
echo "Your system now has:"
echo "  • 3 base accounts (admin, reception, user1)"
echo "  • 9 rooms total (R101-R109)"
echo "  • 4 sample bookings for testing/demo"
echo
echo "To add more user accounts with proper password hashing:"
echo "  echo 'mypassword' | java -cp \"dist/HotelBooking.jar\" SeedManager"
echo
echo "To access the system, run: ./dist/run.sh"
echo
