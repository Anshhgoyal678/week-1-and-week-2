import java.util.*;
import java.time.*;

public class ParkingLot {

    private static class Vehicle {
        String licensePlate;
        LocalDateTime entryTime;

        Vehicle(String licensePlate, LocalDateTime entryTime) {
            this.licensePlate = licensePlate;
            this.entryTime = entryTime;
        }
    }

    private enum SpotStatus { EMPTY, OCCUPIED, DELETED }

    private static class Spot {
        SpotStatus status;
        Vehicle vehicle;

        Spot() {
            status = SpotStatus.EMPTY;
            vehicle = null;
        }
    }

    private final Spot[] spots;
    private final int capacity;
    private int totalProbes = 0;
    private int occupiedSpots = 0;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        spots = new Spot[capacity];
        for (int i = 0; i < capacity; i++) spots[i] = new Spot();
    }

    // Hash function for license plate
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle
    public String parkVehicle(String licensePlate) {
        int preferred = hash(licensePlate);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int idx = (preferred + i) % capacity;
            probes++;

            if (spots[idx].status == SpotStatus.EMPTY || spots[idx].status == SpotStatus.DELETED) {
                spots[idx].status = SpotStatus.OCCUPIED;
                spots[idx].vehicle = new Vehicle(licensePlate, LocalDateTime.now());
                occupiedSpots++;
                totalProbes += (probes - 1); // count only collisions
                return "Assigned spot #" + idx + " (" + (probes - 1) + " probes)";
            }
        }

        return "Parking lot full";
    }

    // Exit vehicle
    public String exitVehicle(String licensePlate) {
        int preferred = hash(licensePlate);

        for (int i = 0; i < capacity; i++) {
            int idx = (preferred + i) % capacity;

            if (spots[idx].status == SpotStatus.OCCUPIED &&
                    spots[idx].vehicle.licensePlate.equals(licensePlate)) {
                Vehicle v = spots[idx].vehicle;
                spots[idx].status = SpotStatus.DELETED;
                spots[idx].vehicle = null;
                occupiedSpots--;

                Duration duration = Duration.between(v.entryTime, LocalDateTime.now());
                double fee = calculateFee(duration);

                return "Spot #" + idx + " freed, Duration: " +
                        duration.toHours() + "h " + (duration.toMinutes() % 60) +
                        "m, Fee: $" + String.format("%.2f", fee);
            }
        }

        return "Vehicle not found";
    }

    // Fee calculation: $5 per hour
    private double calculateFee(Duration duration) {
        double hours = duration.toMinutes() / 60.0;
        return Math.ceil(hours) * 5.0;
    }

    // Get statistics
    public String getStatistics() {
        double occupancy = (occupiedSpots * 100.0) / capacity;
        double avgProbes = capacity == 0 ? 0 : (double) totalProbes / occupiedSpots;
        return "Occupancy: " + String.format("%.1f", occupancy) + "%, Avg Probes: " +
                String.format("%.2f", avgProbes);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        System.out.println(lot.parkVehicle("ABC-1234")); // e.g., spot #127
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000); // simulate parking duration

        System.out.println(lot.exitVehicle("ABC-1234"));
        System.out.println(lot.getStatistics());
    }
}