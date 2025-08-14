import java.time.LocalDateTime;
import java.time.LocalDate;

public class Bus extends TransportVehicle {
    private String operatorName;
    private String driverName;
    private String assistantName;
    private double totalKilometers;
    private double averageSpeed;
    private LocalDateTime lastMaintenance;
    private boolean hasAirConditioning;
    private boolean hasWiFi;
    private boolean isAccessible;
    private String licenseNumber;
    private ScheduleStatus scheduleStatus;

    public Bus(String vehicleId, int totalSeats, String status, int manufacturingYear,
               LocalDate lastMaintenanceDate, String operatorName) {
        super(vehicleId, "Bus", totalSeats, status, manufacturingYear, lastMaintenanceDate);
        this.operatorName = operatorName;
        this.driverName = "";
        this.assistantName = "";
        this.totalKilometers = 0.0;
        this.averageSpeed = 0.0;
        this.lastMaintenance = LocalDateTime.now();
        this.hasAirConditioning = false;
        this.hasWiFi = false;
        this.isAccessible = false;
        this.licenseNumber = "";
        this.scheduleStatus = ScheduleStatus.ON_TIME;
    }

    public Bus(String vehicleId, int totalSeats, String status, int manufacturingYear,
               LocalDate lastMaintenanceDate, String operatorName, String driverName,
               String assistantName, boolean hasAirConditioning, boolean hasWiFi, boolean isAccessible) {
        this(vehicleId, totalSeats, status, manufacturingYear, lastMaintenanceDate, operatorName);
        this.driverName = driverName;
        this.assistantName = assistantName;
        this.hasAirConditioning = hasAirConditioning;
        this.hasWiFi = hasWiFi;
        this.isAccessible = isAccessible;
    }

    @Override
    public double calculateOperationalCost() {
        double baseCost = 1000.0;
        double kmCost = totalKilometers * 2.5;
        double fuelCost = calculateFuelCost();
        double maintenanceCost = calculateMaintenanceCost();
        double amenityCost = (hasAirConditioning ? 500 : 0) + (hasWiFi ? 200 : 0);
        return baseCost + kmCost + fuelCost + maintenanceCost + amenityCost;
    }

    @Override
    public String getVehicleSpecifications() {
        StringBuilder specs = new StringBuilder();
        specs.append("Bus Specifications:\n");
        specs.append("Vehicle ID: ").append(getVehicleId()).append("\n");
        specs.append("Operator: ").append(operatorName).append("\n");
        specs.append("Total Seats: ").append(getTotalSeats()).append("\n");
        specs.append("Driver: ").append(driverName.isEmpty() ? "Not Assigned" : driverName).append("\n");
        specs.append("Assistant: ").append(assistantName.isEmpty() ? "Not Assigned" : assistantName).append("\n");
        specs.append("Air Conditioning: ").append(hasAirConditioning ? "Yes" : "No").append("\n");
        specs.append("WiFi: ").append(hasWiFi ? "Yes" : "No").append("\n");
        specs.append("Accessible: ").append(isAccessible ? "Yes" : "No").append("\n");
        specs.append("Total Kilometers: ").append(totalKilometers).append(" km\n");
        specs.append("Average Speed: ").append(averageSpeed).append(" km/h");
        return specs.toString();
    }

    @Override
    public boolean performSafetyCheck() {
        boolean basicSafety = getStatus() != VehicleStatus.MAINTENANCE &&
                !isDueForMaintenance() &&
                scheduleStatus.allowsBoarding();
        boolean busSpecificSafety = totalKilometers < 50000 &&
                averageSpeed > 0 &&
                averageSpeed <= 120;
        return basicSafety && busSpecificSafety;
    }

    public void performMaintenance() {
        this.lastMaintenance = LocalDateTime.now();
        this.totalKilometers = 0.0;
        setStatus(VehicleStatus.AVAILABLE);
        completeMaintenance();
        System.out.println("Bus maintenance completed for " + getVehicleId());
    }

    public void assignDriver(String driverName) {
        this.driverName = driverName;
        System.out.println("Driver " + driverName + " assigned to bus " + getVehicleId());
    }

    public void assignAssistant(String assistantName) {
        this.assistantName = assistantName;
        System.out.println("Assistant " + assistantName + " assigned to bus " + getVehicleId());
    }

    public void updateScheduleStatus(ScheduleStatus status) {
        this.scheduleStatus = status;
        System.out.println("Schedule status updated to: " + status);
    }

    public double calculateFuelCost() {
        double fuelPrice = 110.0; // BDT per liter
        double fuelConsumption = totalKilometers / getFuelEfficiency();
        return fuelConsumption * fuelPrice;
    }

    public void addKilometers(double kilometers) {
        this.totalKilometers += kilometers;
        updateAverageSpeed();
    }

    private void updateAverageSpeed() {
        if (totalKilometers > 0) {
            this.averageSpeed = totalKilometers / 10.0; // Simplified calculation
        }
    }

    public boolean needsMaintenance() {
        return totalKilometers > 40000 || isDueForMaintenance();
    }
    public BusOperator getOperator() {

        try {
            return BusOperator.valueOf(operatorName.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return BusOperator.CITY; // Default fallback
        }
    }


    // Getters and setters
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getAssistantName() { return assistantName; }
    public void setAssistantName(String assistantName) { this.assistantName = assistantName; }
    public double getTotalKilometers() { return totalKilometers; }
    public void setTotalKilometers(double totalKilometers) { this.totalKilometers = totalKilometers; }
    public double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }
    public LocalDateTime getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(LocalDateTime lastMaintenance) { this.lastMaintenance = lastMaintenance; }
    public boolean hasAirConditioning() { return hasAirConditioning; }
    public void setHasAirConditioning(boolean hasAirConditioning) { this.hasAirConditioning = hasAirConditioning; }
    public boolean hasWiFi() { return hasWiFi; }
    public void setHasWiFi(boolean hasWiFi) { this.hasWiFi = hasWiFi; }
    public boolean isAccessible() { return isAccessible; }
    public void setAccessible(boolean accessible) { isAccessible = accessible; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public ScheduleStatus getScheduleStatus() { return scheduleStatus; }
    public void setScheduleStatus(ScheduleStatus scheduleStatus) { this.scheduleStatus = scheduleStatus; }

    @Override
    public String toString() {
        return String.format("Bus{id='%s', operator='%s', seats=%d/%d, status='%s', driver='%s'}",
                getVehicleId(), operatorName, (getTotalSeats() - getAvailableSeats()),
                getTotalSeats(), getStatus(), driverName.isEmpty() ? "Unassigned" : driverName);
    }
}
