import java.time.LocalDate;
import java.time.Period;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public abstract class TransportVehicle {

    protected String vehicleId;
    protected String type;
    protected int totalSeats;
    protected int availableSeats;
    protected VehicleStatus status;
    protected int manufacturingYear;
    protected FuelType fuelType;
    protected Route assignedRoute;
    private LocalDate lastMaintenanceDate;
    private double fuelEfficiency;
    private double currentMileage;
    private List<String> maintenanceHistory;
    private boolean isEcoFriendly;

    // Database connection constants
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    public TransportVehicle(String vehicleId, String type, int totalSeats, String status,
                            int manufacturingYear, LocalDate lastMaintenanceDate) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.status = VehicleStatus.valueOf(status.toUpperCase());
        this.manufacturingYear = manufacturingYear;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.fuelType = FuelType.DIESEL;
        this.assignedRoute = null;
        this.fuelEfficiency = 10.0;
        this.currentMileage = 0.0;
        this.maintenanceHistory = new ArrayList<>();
        this.isEcoFriendly = false;
    }

    public TransportVehicle(String vehicleId, String type, int totalSeats, String status,
                            int manufacturingYear, LocalDate lastMaintenanceDate,
                            FuelType fuelType, Route assignedRoute, double fuelEfficiency) {
        this(vehicleId, type, totalSeats, status, manufacturingYear, lastMaintenanceDate);
        this.fuelType = fuelType;
        this.assignedRoute = assignedRoute;
        this.fuelEfficiency = fuelEfficiency;
        this.isEcoFriendly = (fuelType == FuelType.ELECTRIC || fuelType == FuelType.HYBRID);
    }

    public void displayDetails() {
        System.out.println("=== Vehicle Details ===");
        System.out.println("Vehicle ID: " + vehicleId);
        System.out.println("Type: " + type);
        System.out.println("Total Seats: " + totalSeats);
        System.out.println("Available Seats: " + availableSeats);
        System.out.println("Status: " + status);
        System.out.println("Manufacturing Year: " + manufacturingYear);
        System.out.println("Fuel Type: " + fuelType);
        System.out.println("Eco-Friendly: " + (isEcoFriendly ? "Yes" : "No"));
        if (assignedRoute != null) {
            System.out.println("Assigned Route: " + assignedRoute.getStartLocation()
                    + " → " + assignedRoute.getEndLocation());
        }
        System.out.println("Fuel Efficiency: " + fuelEfficiency + " km/L");
        System.out.println("Current Mileage: " + currentMileage + " km");
        System.out.println("Due for Maintenance: " + (isDueForMaintenance() ? "Yes" : "No"));
        System.out.println("=======================");
    }

    public double calculateMaintenanceCost() {
        if (lastMaintenanceDate == null) {
            return 1000;
        }
        int yearsSinceLastMaintenance = Period.between(lastMaintenanceDate, LocalDate.now()).getYears();
        double baseCost = yearsSinceLastMaintenance * 500;
        double typeFactor = getMaintenanceTypeFactor();
        double fuelFactor = getFuelMaintenanceFactor();
        return baseCost * typeFactor * fuelFactor;
    }

    public boolean isDueForMaintenance() {
        if (lastMaintenanceDate == null) {
            return true;
        }
        int yearsSinceLastMaintenance = Period.between(lastMaintenanceDate, LocalDate.now()).getYears();
        double mileageThreshold = 50000.0;
        return yearsSinceLastMaintenance > 1 || currentMileage > mileageThreshold;
    }

    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            updateSeatsInDatabase();
            return true;
        }
        return false;
    }

    public boolean cancelSeat() {
        if (availableSeats < totalSeats) {
            availableSeats++;
            updateSeatsInDatabase();
            return true;
        }
        return false;
    }

    public boolean canAccommodatePassengers(int passengerCount) {
        return availableSeats >= passengerCount && status == VehicleStatus.AVAILABLE;
    }

    public double calculateFareMultiplier() {
        double multiplier = 1.0;
        if (isEcoFriendly) {
            multiplier += 0.1;
        }
        if (fuelType == FuelType.ELECTRIC) {
            multiplier += 0.05;
        }
        if (getVehicleAge() < 2) {
            multiplier += 0.15;
        }
        return multiplier;
    }

    public double calculateCarbonFootprint(double distance) {
        switch (fuelType) {
            case ELECTRIC: return 0.0;
            case HYBRID: return distance * 0.05;
            case CNG: return distance * 0.08;
            case DIESEL: return distance * 0.12;
            case PETROL: return distance * 0.15;
            default: return distance * 0.12;
        }
    }

    public void assignToRoute(Route route) {
        this.assignedRoute = route;
        if (route != null) {
            List<String> facilities = new ArrayList<>();
            facilities.add("Vehicle Parking");
            facilities.add("Passenger Boarding");

            route.addStation(new Station(
                    Integer.parseInt(vehicleId.replaceAll("[^0-9]", "")),
                    "Vehicle Station " + vehicleId,
                    route.getStartLocation(),
                    facilities
            ));
        }
        updateVehicleInDatabase();
    }

    public double getUtilizationRate() {
        if (totalSeats == 0) return 0.0;
        return ((double) (totalSeats - availableSeats) / totalSeats) * 100;
    }

    public String getPerformanceStatus() {
        double utilization = getUtilizationRate();
        if (utilization >= 80) return "High Performance";
        if (utilization >= 50) return "Good Performance";
        if (utilization >= 20) return "Average Performance";
        return "Low Performance";
    }

    public void scheduleMaintenance() {
        this.status = VehicleStatus.MAINTENANCE;
        this.availableSeats = 0;
        maintenanceHistory.add("Scheduled: " + LocalDate.now());
        updateVehicleInDatabase();
    }

    public void completeMaintenance() {
        this.status = VehicleStatus.AVAILABLE;
        this.availableSeats = totalSeats;
        this.lastMaintenanceDate = LocalDate.now();
        maintenanceHistory.add("Completed: " + LocalDate.now());
        updateVehicleInDatabase();
    }

    private void updateSeatsInDatabase() {
        String query = "UPDATE vehicles SET available_seats = ? WHERE vehicleId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, availableSeats);
            stmt.setString(2, vehicleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating seats: " + e.getMessage());
        }
    }

    private void updateVehicleInDatabase() {

        String query = "UPDATE vehicles SET status = ?, routeId = ?, lastMaintenance = ? WHERE vehicleId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, assignedRoute != null ? assignedRoute.getRouteId() : 0);
            stmt.setDate(3, lastMaintenanceDate != null ? Date.valueOf(lastMaintenanceDate) : null);
            stmt.setString(4, vehicleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
        }
    }

    protected static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private double getMaintenanceTypeFactor() {
        switch (type.toLowerCase()) {
            case "bus": return 1.2;
            case "train": return 1.5;
            case "metro": return 1.8;
            default: return 1.0;
        }
    }

    private double getFuelMaintenanceFactor() {
        switch (fuelType) {
            case ELECTRIC: return 0.7;
            case HYBRID: return 0.9;
            case CNG: return 1.0;
            case DIESEL: return 1.1;
            case PETROL: return 1.2;
            default: return 1.0;
        }
    }

    private int getVehicleAge() {
        return LocalDate.now().getYear() - manufacturingYear;
    }

    public double calculateClassAdjustedFare(double baseFare, String trainClass) {
        if (!"train".equalsIgnoreCase(type)) {
            return baseFare;
        }
        try {
            TrainClass tClass = TrainClass.valueOf(trainClass.toUpperCase());
            return baseFare * tClass.getPriceMultiplier() * calculateFareMultiplier();
        } catch (IllegalArgumentException e) {
            return baseFare * calculateFareMultiplier();
        }
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
        if (status == VehicleStatus.MAINTENANCE) {
            this.availableSeats = 0;
        } else if (status == VehicleStatus.AVAILABLE) {
            this.availableSeats = totalSeats;
        }
    }

    public void setStatus(String statusStr) {
        try {
            setStatus(VehicleStatus.valueOf(statusStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid VehicleStatus: " + statusStr);
        }
    }


    public abstract double calculateOperationalCost();
    public abstract String getVehicleSpecifications();
    public abstract boolean performSafetyCheck();

    public String getVehicleId() {return vehicleId; }
    public String getType() { return type; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public VehicleStatus getStatus() { return status; }
    public int getManufacturingYear() { return manufacturingYear; }
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
        this.isEcoFriendly = (fuelType == FuelType.ELECTRIC || fuelType == FuelType.HYBRID);
    }
    public Route getAssignedRoute() { return assignedRoute; }
    public double getFuelEfficiency() { return fuelEfficiency; }
    public void setFuelEfficiency(double fuelEfficiency) { this.fuelEfficiency = fuelEfficiency; }
    public double getCurrentMileage() { return currentMileage; }
    public void setCurrentMileage(double currentMileage) { this.currentMileage = currentMileage; }
    public List<String> getMaintenanceHistory() { return new ArrayList<>(maintenanceHistory); }
    public boolean isEcoFriendly() { return isEcoFriendly; }

    @Override
    public String toString() {
        return String.format("TransportVehicle{id='%s', type='%s', seats=%d/%d, status='%s', fuel=%s}",
                vehicleId, type, (totalSeats - availableSeats), totalSeats, status, fuelType);
    }
}
