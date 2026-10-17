@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
javac -d build VehicleRentalManagementSystem.java
if errorlevel 1 exit /b 1
java -cp build VehicleRentalManagementSystem