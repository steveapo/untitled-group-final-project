# Hotel Room Booking System

A cross-platform CLI application for managing hotel room reservations, built with Java.

<div style="margin: 1.5rem 0;">
  <a href="https://github.com/steveapo/untitled-group-final-project/releases/download/v1.0/HotelBooking-v1.0.zip" style="display: inline-block; padding: 0.75rem 1.5rem; background: var(--vp-c-brand-1); color: var(--vp-c-white); border-radius: 8px; font-weight: 600; text-decoration: none; font-size: 1.05rem;">
    ⬇ Download Program (v1.0)
  </a>
  <span style="margin-left: 0.75rem; opacity: 0.6; font-size: 0.9rem;">Requires Java 11+</span>
</div>

## Overview

The Hotel Room Booking System is a terminal-based application that supports three distinct user roles, each with purpose-built menus and capabilities:

| Role | Description |
|------|-------------|
| **Guest** | Browse available rooms without an account |
| **User** | Registered guests who can search, book, and manage reservations |
| **Reception** | Front-desk staff who handle check-ins, check-outs, and booking management |
| **Manager** | Hotel owner with full control over rooms, staff, bookings, and statistics |

## Key Highlights

- **Cross-platform** — runs on macOS, Linux, and Windows (PowerShell / Terminal)
- **Secure authentication** — SHA-512 password hashing with random salts
- **Interactive CLI** — ANSI colors, spinners, arrow-key selectors, and masked password input
- **File-based persistence** — CSV data files, no database required
- **Role-based access control** — four distinct permission levels

## Quick Start

```bash
# Download the latest release from GitHub
# Extract and run:
./run.sh          # macOS / Linux
run.bat           # Windows
```

See [Getting Started](/getting-started) for full installation instructions.

## Demo Accounts

| Username | Password | Role |
|----------|----------|------|
| `user1` | `user1` | Guest (User) |
| `user2` | `user1` | Guest (User) |
| `reception` | `reception` | Receptionist |
| `admin` | `admin` | Manager / Owner |
