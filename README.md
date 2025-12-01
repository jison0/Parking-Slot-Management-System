
# SMART PARKING MANAGEMENT SYSTEM

*A Java-based CLI parking system with slot mapping, reporting, CSV export, and automated fee computation.*

---

##  Overview

This project is a **Smart Parking Management System** written in Java.
It runs entirely in the **terminal/console** and provides a full set of parking-lot management features:

✔ Real-time parking and unparking
✔ Automatic fee computation
✔ Slot allocation by vehicle type
✔ Vacancy & occupancy statistics
✔ ASCII slot map view
✔ History logs & peak-hour analytics
✔ CSV export
✔ Active vehicle search
✔ Colorized user interface

The system supports two main vehicle types:

* **Motorcycle** (M-slots)
* **Four-wheel vehicles** (A-section or B-section slots)

---

##  Features

### **1. Park Vehicle**

* Choose vehicle type: `Motorcycle` or `Four-wheel`
* Automatic slot assignment based on preferred section
* Option for cars to prefer Section A or B
* Logs entry with precise timestamp
* Prevents duplicate parking for the same plate

### **2. Unpark Vehicle**

* Auto-calculates duration and billed hours
* Fee computation rules:

  * **Motorcycle**: ₱20 for first 3 hours → ₱5 per succeeding hour
  * **Four-wheel**: ₱40 for first 3 hours → ₱10 per succeeding hour
* Generates a full **ASCII receipt**

### **3. Vacancy / Occupancy Summary**

Displays:

* Available motorcycle slots
* Available A-section slots
* Available B-section slots
* Overall occupancy %

### **4. Slot Map Visualization**

Shows a graphical map using:

* `#` = Occupied
* `-` = Empty
  Organized in rows for easy visualization.

### **5. Reports**

Includes:

* Total slots
* Active parked vehicles
* Recent (last 10) history logs
* Peak hours (top 3 busiest entry hours)
* Total revenue earned

### **6. Export History to CSV**

Exports all logs to:

```
parking_history.csv
```

or a custom filename.

### **7. Active Vehicle List + Search**

Shows all vehicles currently parked with:

* Plate number
* Vehicle type
* Entry timestamp

Searchable by plate number.

---

## System Architecture

### **Core Classes**

| Class                                    | Description                                            |
| ---------------------------------------- | ------------------------------------------------------ |
| `ParkingSystem`                          | Main CLI UI and program loop                           |
| `Vehicle` (abstract)                     | Base class for supported vehicle types                 |
| `Motorcycle`, `FourWheelA`, `FourWheelB` | Vehicle implementations with slot preferences          |
| `ParkingSlot`                            | Represents a single slot with occupancy                |
| `ParkingLot`                             | Core logic: parking, unparking, logs, revenue, history |
| `LogEntry`                               | Represents single parking event                        |
| `Result`, `UnparkResult`, `Receipt`      | Data wrappers for UI responses                         |

### **Slot Count**

* **Motorcycle (M)**: 60 slots
* **4-wheel Section A (A)**: 30 slots
* **4-wheel Section B (B)**: 30 slots

---

## How to Run

### **1. Save the main file**

Save as:

```
ParkingSystem.java
```

### **2. Compile**

```sh
javac ParkingSystem.java
```

### **3. Run**

```sh
java ParkingSystem
```

---

## Program Flow Diagram (Simplified)

```
 Main Menu
    │
    ├── Park Vehicle
    ├── Unpark Vehicle (with payment)
    ├── Vacancy / Occupancy
    ├── Slot Map
    ├── Reports
    ├── Export CSV
    ├── Active Vehicle List + Search
    └── Exit
```

---

## CSV Export Format

The exported CSV includes columns:

```
plate,vehicle_type,slot_id,entry_time,exit_time,duration_minutes,amount_paid
```

Example entry:

```
ABC123,FourWheel-A,A05,2025-01-20 12:30:00,2025-01-20 15:45:00,195,70.00
```

---

## Fee Computation Examples

| Vehicle    | Duration | Hours Billed | Total               |
| ---------- | -------- | ------------ | ------------------- |
| Motorcycle | 2h       | 3h           | ₱20                 |
| Motorcycle | 5h       | 5h           | ₱20 + (2×₱5) = ₱30  |
| Four-wheel | 4h       | 4h           | ₱40 + ₱10 = ₱50     |
| Four-wheel | 8h       | 8h           | ₱40 + (5×₱10) = ₱90 |

---

## Terminal Enhancements

The UI uses ANSI color codes for:

* Blue & Cyan: Titles and headers
* Yellow: Prompts
* Green: Success messages
* Red: Errors/alerts

Clear screen and pause functions create a clean, modern CLI experience.

---
Member:

Laila, Brett Ryan
Podra, Mark Jayson
Soria, John Anielov
