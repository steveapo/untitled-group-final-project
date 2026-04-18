# Hotel Room Booking System

A cross-platform CLI application for managing hotel room reservations, built with Java. Features role-based access control, an interactive occupancy calendar, secure authentication, and a manager analytics dashboard.

**[⬇ Download Program (v1.6.4)](/getting-started#option-1-download-the-release-recommended)** — Requires Java 22+ • **[View all releases](/releases)**

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
- **Scheduled maintenance windows** — pick start + end dates from the Edit Room flow (or press `M` in the calendar) and the room is automatically blocked for that range
- Real-time occupancy visualisation

### Manager Analytics
- **Booking Statistics dashboard** — overview, booking-status distribution with inline bar charts, revenue breakdown (Realised / Booked / Lost / Total), 7-day occupancy heat map, and top-earning rooms
- **Room-deletion impact preview** — see every active, upcoming, past, and cancelled booking attached to a room before confirming deletion

### Data Management
- **File-based persistence** — no database required
- CSV-format data files, strict 5-field schema, UTF-8 pinned
- **Atomic writes** (`Files.move(... ATOMIC_MOVE)`) — a crash mid-write never leaves a half-written file
- Automatic data validation with malformed-line logging to `Errors`
- Booking lifecycle management: CONFIRMED → CHECKED_IN → CHECKED_OUT

## Quick Start

1. [Download the release](/getting-started#option-1-download-the-release-recommended) — choose `.tar.gz` (macOS/Linux) or `.zip` (Windows)
2. Extract the archive and run `./run.sh` (macOS/Linux) or `run.bat` (Windows)

See [Getting Started](/getting-started) for full setup instructions including Java requirements and troubleshooting.

## Demo Accounts

These accounts are pre-loaded on first run:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin` | Manager |
| `reception` | `reception` | Reception |
| `user1` | `user1` | User |

On first launch the system also seeds **9 rooms (R101–R109)** and a realistic mix of past, current, and upcoming bookings for `user1` — dates are computed relative to today so the demo is always current.

See [Getting Started](/getting-started) for details.

---

**Built for ITC2205 Final Project** — A terminal-based booking system showcasing Java CLI development, cross-platform compatibility, and role-based architecture.
