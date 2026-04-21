# 🏨 Grand Elite Hotel Management System

A JavaFX-based desktop application for managing hotel room inventory, guest check-ins, and billing — built as a practical demonstration of core Java OOP and systems programming concepts.

---

## 📌 Overview

This application provides a clean, tab-based UI for hotel staff to manage room availability, record guest check-ins with date tracking, and generate invoices upon checkout. All data is persisted to disk automatically and transactions are logged for auditing.

---

## ✨ Features

- **Room Inventory** — Add and delete rooms with type classification and pricing
- **Guest Check-In** — Assign guests to available rooms with a date picker (past dates are disabled)
- **Billing & Checkout** — Generate a formatted invoice based on nightly rate × number of nights stayed
- **Auto-Save** — Data is saved to disk every 60 seconds via a background thread, and on app close
- **Transaction Logging** — Every checkout is appended to a persistent log file using random access I/O
- **Visual Feedback** — Clicking a room row highlights it and dims all others for clear selection

---

## 🧠 Java Concepts Demonstrated

| Concept | Where Used |
|---|---|
| **Enums** | `RoomType` (SINGLE, DOUBLE, DELUXE, SUITE) |
| **Serialization** | `Room` implements `Serializable`; saved/loaded via `ObjectOutputStream` |
| **Generics & Collections** | `ObservableList<Room>`, `ArrayList<VBox>` |
| **Multithreading** | Daemon thread for auto-saving every 60 seconds |
| **Synchronization** | `saveDataToFile()` is `synchronized` |
| **Random Access Files** | `RandomAccessFile` used to append transaction logs |
| **Wrapper Classes & Autoboxing** | `Double total = nights * price` in billing |
| **Exception Handling** | Prevents deletion of occupied rooms; validates number inputs |

---

## 🗂️ Project Structure

```
HotelManagementApp.java   # Main application file
hotel_data.dat            # Serialized room data (auto-generated at runtime)
transactions.txt          # Checkout log file (auto-generated at runtime)
```

---

## 🖥️ UI Layout

The app is organized into three tabs:

**⚙ Inventory**
- View all rooms as scrollable rows (green border = available, red = occupied)
- Add a new room by specifying room number, type, and price
- Delete a selected room (occupied rooms cannot be deleted)

**🔑 Check-In**
- Select a room row, enter a guest name (letters only), pick a check-in date
- Past dates are disabled in the date picker

**💰 Billing**
- Select an occupied room row, pick a check-out date
- Generates an invoice showing guest details, nights stayed, and total amount
- Resets the room to "Available" after checkout

---

## ⚙️ Requirements

- **Java 11+**
- **JavaFX SDK** (must be on the module path)

---

## 🚀 Running the App

1. Clone or download the project
2. Add JavaFX libraries to your IDE or build tool
3. Run `HotelManagementApp.java` as a JavaFX application

**With CLI (example using JavaFX SDK at `/path/to/javafx-sdk`):**
```bash
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls HotelManagementApp.java
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls application.HotelManagementApp
```

---

## 📋 Key Constraints & Validations

- Guest names must contain only letters and spaces
- Check-out date must be strictly after check-in date
- Duplicate room numbers are rejected
- Room price must be a positive number
- Occupied rooms cannot be deleted — checkout must be processed first

---

## 📄 License

Developed as an academic Java programming project.
