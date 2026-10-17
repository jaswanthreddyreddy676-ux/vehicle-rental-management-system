# Drive Easy Rental System

A menu-driven vehicle rental management system written in Core Java. All data is held in memory and is cleared when the application exits.

## Requirements

- Java JDK 8 or later

## Run

Open a terminal in this folder and compile and launch the program:

```powershell
New-Item -ItemType Directory -Force build
javac -d build VehicleRentalManagementSystem.java
java -cp build VehicleRentalManagementSystem
```

On Windows, `run.bat` compiles the application and starts it in one step.

## Build a runnable JAR

```powershell
New-Item -ItemType Directory -Force build
javac -d build VehicleRentalManagementSystem.java
jar cfe drive-easy-rental.jar VehicleRentalManagementSystem -C build .
java -jar drive-easy-rental.jar
```

To deploy, copy the JAR to a machine with Java 8 or later and run `java -jar drive-easy-rental.jar`. This is a console application, not a web service; it runs in a terminal and needs no database or server.

## Features

- Add, list, and search vehicles
- Register and list customers
- Create and cancel bookings with vehicle availability checks
- Calculate charges from daily rate and rental duration
- Simulate cash, card, and UPI payments
- Report inventory, customers, bookings, successful payments, and collected revenue