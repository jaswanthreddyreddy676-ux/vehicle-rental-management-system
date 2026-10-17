import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class VehicleRentalManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
    private final ArrayList<Customer> customers = new ArrayList<Customer>();
    private final ArrayList<Booking> bookings = new ArrayList<Booking>();
    private int nextVehicleId = 1;
    private int nextCustomerId = 1;
    private int nextBookingId = 1;

    public static void main(String[] args) {
        new VehicleRentalManagementSystem().run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ", 1, 10);
            switch (choice) {
                case 1: addVehicle(); break;
                case 2: viewVehicles(); break;
                case 3: searchVehicle(); break;
                case 4: registerCustomer(); break;
                case 5: viewCustomers(); break;
                case 6: createBooking(); break;
                case 7: cancelBooking(); break;
                case 8: makePayment(); break;
                case 9: viewReports(); break;
                case 10:
                    running = false;
                    System.out.println("Thank you for using Drive Easy Rental System.");
                    break;
                default: break;
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("\n====================================");
        System.out.println("     DRIVE EASY RENTAL SYSTEM");
        System.out.println("====================================");
        System.out.println("1. Add Vehicle");
        System.out.println("2. View Vehicles");
        System.out.println("3. Search Vehicle");
        System.out.println("4. Register Customer");
        System.out.println("5. View Customers");
        System.out.println("6. Create Rental Booking");
        System.out.println("7. Cancel Booking");
        System.out.println("8. Make Payment");
        System.out.println("9. View Reports");
        System.out.println("10. Exit");
    }

    private void addVehicle() {
        System.out.println("\n-- Add Vehicle --");
        String name = readRequired("Vehicle name: ");
        String type = readRequired("Vehicle type (e.g. Sedan, SUV, Bike): ");
        double rate = readPositiveDouble("Daily rental rate: ");
        String id = String.format(Locale.ROOT, "V%03d", nextVehicleId++);
        vehicles.add(new Vehicle(id, name, type, rate));
        System.out.println("Vehicle added. ID: " + id);
    }

    private void viewVehicles() {
        System.out.println("\n-- Vehicles --");
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles registered.");
            return;
        }
        for (Vehicle vehicle : vehicles) printVehicle(vehicle);
    }

    private void searchVehicle() {
        String id = readRequired("Vehicle ID: ").toUpperCase(Locale.ROOT);
        Vehicle vehicle = findVehicle(id);
        if (vehicle == null) System.out.println("No vehicle found with ID " + id + ".");
        else printVehicle(vehicle);
    }

    private void registerCustomer() {
        System.out.println("\n-- Register Customer --");
        String name = readRequired("Customer name: ");
        String phone = readRequired("Phone number: ");
        String email = readRequired("Email address: ");
        String id = String.format(Locale.ROOT, "C%03d", nextCustomerId++);
        customers.add(new Customer(id, name, phone, email));
        System.out.println("Customer registered. ID: " + id);
    }

    private void viewCustomers() {
        System.out.println("\n-- Customers --");
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        for (Customer customer : customers) {
            System.out.printf("%s | %s | Phone: %s | Email: %s%n",
                    customer.id, customer.name, customer.phone, customer.email);
        }
    }

    private void createBooking() {
        System.out.println("\n-- Create Rental Booking --");
        if (customers.isEmpty()) {
            System.out.println("Register a customer before creating a booking.");
            return;
        }
        if (vehicles.isEmpty()) {
            System.out.println("Add a vehicle before creating a booking.");
            return;
        }

        String customerId = readRequired("Customer ID: ").toUpperCase(Locale.ROOT);
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            System.out.println("No registered customer found with ID " + customerId + ".");
            return;
        }
        String vehicleId = readRequired("Vehicle ID: ").toUpperCase(Locale.ROOT);
        Vehicle vehicle = findVehicle(vehicleId);
        if (vehicle == null) {
            System.out.println("No vehicle found with ID " + vehicleId + ".");
            return;
        }
        if (!vehicle.available) {
            System.out.println("That vehicle is currently unavailable.");
            return;
        }

        int days = readInt("Rental duration in days: ", 1, Integer.MAX_VALUE);
        double total = vehicle.dailyRate * days;
        String id = String.format(Locale.ROOT, "B%03d", nextBookingId++);
        bookings.add(new Booking(id, customer, vehicle, days, total));
        vehicle.available = false;
        System.out.printf(Locale.ROOT, "Booking %s created. Total: $%.2f%n", id, total);
    }

    private void cancelBooking() {
        String id = readRequired("Booking ID: ").toUpperCase(Locale.ROOT);
        Booking booking = findBooking(id);
        if (booking == null) {
            System.out.println("No booking found with ID " + id + ".");
            return;
        }
        if (booking.status == BookingStatus.CANCELLED) {
            System.out.println("That booking has already been cancelled.");
            return;
        }
        booking.status = BookingStatus.CANCELLED;
        booking.vehicle.available = !hasActiveBooking(booking.vehicle, booking);
        System.out.println("Booking " + id + " cancelled. Vehicle availability updated.");
    }

    private void makePayment() {
        String id = readRequired("Booking ID: ").toUpperCase(Locale.ROOT);
        Booking booking = findBooking(id);
        if (booking == null) {
            System.out.println("No booking found with ID " + id + ".");
            return;
        }
        if (booking.status == BookingStatus.CANCELLED) {
            System.out.println("Payment cannot be made for a cancelled booking.");
            return;
        }
        if (booking.payment != null && booking.payment.status == PaymentStatus.SUCCESSFUL) {
            System.out.println("This booking has already been paid.");
            return;
        }

        System.out.println("Payment method: 1. Cash  2. Card  3. UPI");
        int choice = readInt("Choose payment method: ", 1, 3);
        PaymentMethod method;
        switch (choice) {
            case 1: method = new CashPayment(); break;
            case 2: method = new CardPayment(); break;
            case 3: method = new UpiPayment(); break;
            default: throw new IllegalStateException("Unexpected payment method");
        }
        booking.payment = method.process(booking.totalCost);
        System.out.printf(Locale.ROOT, "%s payment successful for booking %s. Amount: $%.2f%n",
                booking.payment.method, id, booking.payment.amount);
    }

    private void viewReports() {
        int paidBookings = 0;
        double revenue = 0.0;
        for (Booking booking : bookings) {
            if (booking.payment != null && booking.payment.status == PaymentStatus.SUCCESSFUL) {
                paidBookings++;
                revenue += booking.payment.amount;
            }
        }
        System.out.println("\n-- Reports --");
        System.out.println("Total vehicles: " + vehicles.size());
        System.out.println("Available vehicles: " + countAvailableVehicles());
        System.out.println("Registered customers: " + customers.size());
        System.out.println("Total bookings: " + bookings.size());
        System.out.println("Active bookings: " + countActiveBookings());
        System.out.println("Successful payments: " + paidBookings);
        System.out.printf(Locale.ROOT, "Revenue collected: $%.2f%n", revenue);
    }

    private int countAvailableVehicles() {
        int count = 0;
        for (Vehicle vehicle : vehicles) if (vehicle.available) count++;
        return count;
    }

    private int countActiveBookings() {
        int count = 0;
        for (Booking booking : bookings) if (booking.status == BookingStatus.ACTIVE) count++;
        return count;
    }

    private boolean hasActiveBooking(Vehicle vehicle, Booking excluded) {
        for (Booking booking : bookings) {
            if (booking != excluded && booking.vehicle == vehicle && booking.status == BookingStatus.ACTIVE) return true;
        }
        return false;
    }

    private Vehicle findVehicle(String id) {
        for (Vehicle vehicle : vehicles) if (vehicle.id.equalsIgnoreCase(id)) return vehicle;
        return null;
    }

    private Customer findCustomer(String id) {
        for (Customer customer : customers) if (customer.id.equalsIgnoreCase(id)) return customer;
        return null;
    }

    private Booking findBooking(String id) {
        for (Booking booking : bookings) if (booking.id.equalsIgnoreCase(id)) return booking;
        return null;
    }

    private void printVehicle(Vehicle vehicle) {
        System.out.printf(Locale.ROOT, "%s | %s | %s | $%.2f/day | %s%n",
                vehicle.id, vehicle.name, vehicle.type, vehicle.dailyRate,
                vehicle.available ? "Available" : "Rented");
    }

    private String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private int readInt(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(value);
                if (number >= minimum && number <= maximum) return number;
            } catch (NumberFormatException ignored) {
                // Keep the console input loop active for invalid input.
            }
            System.out.println("Enter a whole number from " + minimum + " to " + maximum + ".");
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                double amount = Double.parseDouble(value);
                if (Double.isFinite(amount) && amount > 0) return amount;
            } catch (NumberFormatException ignored) {
                // Keep the console input loop active for invalid input.
            }
            System.out.println("Enter a valid amount greater than zero.");
        }
    }

    private enum BookingStatus { ACTIVE, CANCELLED }
    private enum PaymentStatus { SUCCESSFUL }

    private static class Vehicle {
        private final String id;
        private final String name;
        private final String type;
        private final double dailyRate;
        private boolean available = true;

        private Vehicle(String id, String name, String type, double dailyRate) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.dailyRate = dailyRate;
        }
    }

    private static class Customer {
        private final String id;
        private final String name;
        private final String phone;
        private final String email;

        private Customer(String id, String name, String phone, String email) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
        }
    }

    private static class Booking {
        private final String id;
        private final Customer customer;
        private final Vehicle vehicle;
        private final int days;
        private final double totalCost;
        private BookingStatus status = BookingStatus.ACTIVE;
        private Payment payment;

        private Booking(String id, Customer customer, Vehicle vehicle, int days, double totalCost) {
            this.id = id;
            this.customer = customer;
            this.vehicle = vehicle;
            this.days = days;
            this.totalCost = totalCost;
        }
    }

    private static class Payment {
        private final String method;
        private final double amount;
        private final PaymentStatus status;

        private Payment(String method, double amount) {
            this.method = method;
            this.amount = amount;
            this.status = PaymentStatus.SUCCESSFUL;
        }
    }

    private interface PaymentMethod {
        Payment process(double amount);
    }

    private static class CashPayment implements PaymentMethod {
        public Payment process(double amount) { return new Payment("Cash", amount); }
    }

    private static class CardPayment implements PaymentMethod {
        public Payment process(double amount) { return new Payment("Card", amount); }
    }

    private static class UpiPayment implements PaymentMethod {
        public Payment process(double amount) { return new Payment("UPI", amount); }
    }
}