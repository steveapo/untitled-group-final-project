# Hotel Room Booking System

A cross-platform CLI application for managing hotel room reservations, built with Java. Features role-based access control, interactive occupancy calendar, and secure authentication.

**[⬇ Download Program (v1.3)](https://github.com/steveapo/untitled-group-final-project/releases/download/v1.3/HotelBooking-v1.3.zip)** — Requires Java 11+ • **[View all releases](/releases)**

## Overview

The Hotel Room Booking System is a terminal-based application with four distinct user roles, each with purpose-built menus and capabilities:

| Role | Description | Capabilities |
|------|-------------|---|
| **Guest** | Browse available rooms without authentication | View room inventory and rates |
| **User** | Registered guests who can book rooms | Search, book, view/cancel reservations |
| **Reception** | Front-desk staff | Guest management, check-in/out, booking operations, staff oversight |
| **Manager** | Hotel owner/administrator | Full system control, room management, staff administration, analytics |

## Key Features

### Authentication & Security
- **SHA-512 password hashing** with random salt per account
- Role-based access control (RBAC) with four permission levels
- Secure account management with email validation
- Password masking during input

### Interactive Booking System
- **Visual occupancy calendar** with arrow-key navigation (↑↓←→)
- **Interactive date picker** for check-in and check-out dates
- Shift+arrow keys for week-at-a-time navigation
- Vim keybindings support (hjkl)
- Real-time room availability display with status indicators
- Smart booking confirmation with input validation

### Cross-Platform Compatibility
- **macOS/Linux**: Full ANSI color support with Unicode glyphs
- **Windows 10+**: Terminal/PowerShell 7+ with VT mode auto-detection
- **Windows Legacy**: Graceful degradation to numbered menus in cmd.exe
- **IDE Consoles**: Fallback rendering in IntelliJ, VS Code, etc.
- Powered by JLine 3 for true cross-OS terminal handling

### Room Management
- Create, edit, delete, and manage room inventory
- Room types: Single, Double, Triple, Quad, Suite
- Capacity tracking and nightly pricing
- Maintenance status tracking
- Real-time occupancy visualization

### Data Management
- **File-based persistence** — no database required
- CSV-format data files (human-readable and editable)
- Automatic data validation and error logging
- Booking lifecycle management: CONFIRMED → CHECKED_IN → CHECKED_OUT

## Quick Start

```bash
# Download the latest release from GitHub
# Extract and run:
./run.sh          # macOS / Linux
run.bat           # Windows
```

See [Getting Started](/getting-started) for full installation instructions.

## Demo Accounts

Use these credentials to explore the system:

| Username | Password | Role |
|----------|----------|------|
| `user1` | `user1` | User |
| `user2` | `user1` | User |
| `reception` | `reception` | Reception |
| `admin` | `admin` | Manager |

---

**Built for ITC2205 Final Project** — A terminal-based booking system showcasing Java CLI development, cross-platform compatibility, and role-based architecture.
