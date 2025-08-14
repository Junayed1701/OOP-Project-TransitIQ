import java.time.LocalDate;
import java.time.LocalDateTime;

public class Train extends TransportVehicle {
    private TrainClass trainClass;
    private String trainName;
    private int coachCount;
    private double totalKilometers;
    private double averageSpeed;
    private LocalDate lastServiceDate;
    private boolean isOperational;
    private boolean hasAccessibilityRamp;
    private boolean hasDiningCar;
    private boolean hasSleeperCoach;
    private String engineType;
    private ScheduleStatus scheduleStatus;

    public Train(String vehicleId, int totalSeats, String status, int manufacturingYear,
                 LocalDate lastMaintenanceDate, TrainClass trainClass, String trainName) {
        super(vehicleId, "Train", totalSeats, status, manufacturingYear, lastMaintenanceDate);
        this.trainClass = trainClass;
        this.trainName = trainName;
        this.coachCount = 8;
        this.totalKilometers = 0.0;
        this.averageSpeed = 0.0;
        this.lastServiceDate = LocalDate.now();
        this.isOperational = true;
        this.hasAccessibilityRamp = false;
        this.hasDiningCar = false;
        this.hasSleeperCoach = false;
        this.engineType = "Diesel";
        this.scheduleStatus = ScheduleStatus.ON_TIME;
    }

    public Train(String vehicleId, int totalSeats, String status, int manufacturingYear,
                 LocalDate lastMaintenanceDate, TrainClass trainClass, String trainName,
                 int coachCount, boolean hasAccessibilityRamp, boolean hasDiningCar, boolean hasSleeperCoach) {
        this(vehicleId, totalSeats, status, manufacturingYear, lastMaintenanceDate, trainClass, trainName);
        this.coachCount = coachCount;
        this.hasAccessibilityRamp = hasAccessibilityRamp;
        this.hasDiningCar = hasDiningCar;
        this.hasSleeperCoach = hasSleeperCoach;
    }

    @Override
    public double calculateOperationalCost() {
        double baseCost = 2000.0;
        double kmCost = totalKilometers * 5.0;
        double fuelCost = calculateFuelCost();
        double maintenanceCost = calculateMaintenanceCost();
        double coachCost = coachCount * 300.0;
        double facilityCost = (hasDiningCar ? 1000 : 0) + (hasSleeperCoach ? 1500 : 0);
        return baseCost + kmCost + fuelCost + maintenanceCost + coachCost + facilityCost;
    }

    @Override
    public String getVehicleSpecifications() {
        StringBuilder specs = new StringBuilder();
        specs.append("Train Specifications:\n");
        specs.append("Vehicle ID: ").append(getVehicleId()).append("\n");
        specs.append("Train Name: ").append(trainName).append("\n");
        specs.append("Class: ").append(trainClass).append("\n");
        specs.append("Total Seats: ").append(getTotalSeats()).append("\n");
        specs.append("Coach Count: ").append(coachCount).append("\n");
        specs.append("Engine Type: ").append(engineType).append("\n");
        specs.append("Accessibility Ramp: ").append(hasAccessibilityRamp ? "Yes" : "No").append("\n");
        specs.append("Dining Car: ").append(hasDiningCar ? "Yes" : "No").append("\n");
        specs.append("Sleeper Coach: ").append(hasSleeperCoach ? "Yes" : "No").append("\n");
        specs.append("Total Kilometers: ").append(totalKilometers).append(" km\n");
        specs.append("Average Speed: ").append(averageSpeed).append(" km/h");
        return specs.toString();
    }

    @Override
    public boolean performSafetyCheck() {
        boolean basicSafety = getStatus() != VehicleStatus.MAINTENANCE &&
                !isDueForMaintenance() &&
                scheduleStatus.allowsBoarding() &&
                isOperational;
        boolean trainSpecificSafety = totalKilometers < 75000 &&
                averageSpeed > 0 &&
                averageSpeed <= 300 &&
                !needsService();
        return basicSafety && trainSpecificSafety;
    }

    public void performService() {
        this.lastServiceDate = LocalDate.now();
        this.totalKilometers = 0.0;
        this.isOperational = true;
        setStatus(VehicleStatus.AVAILABLE);
        completeMaintenance();
        System.out.println("Train service completed for " + getVehicleId());
    }

    public double calculateClassBasedFare(double baseFare) {
        return baseFare * trainClass.getPriceMultiplier();
    }

    public void updateScheduleStatus(ScheduleStatus status) {
        this.scheduleStatus = status;
        System.out.println("Train schedule status updated to: " + status);
    }

    public double calculateFuelCost() {
        double fuelPrice = 120.0; // BDT per liter for diesel/electric
        double fuelConsumption = totalKilometers / getFuelEfficiency();
        return fuelConsumption * fuelPrice;
    }

    public void addKilometers(double kilometers) {
        this.totalKilometers += kilometers;
        updateAverageSpeed();
    }

    private void updateAverageSpeed() {
        if (totalKilometers > 0) {
            this.averageSpeed = totalKilometers / 15.0; // Simplified calculation
        }
    }

    public boolean needsService() {
        return totalKilometers > 60000 || isDueForMaintenance();
    }

    public void addCoach() {
        this.coachCount++;
        System.out.println("Coach added. Total coaches: " + coachCount);
    }

    public void removeCoach() {
        if (coachCount > 1) {
            this.coachCount--;
            System.out.println("Coach removed. Total coaches: " + coachCount);
        } else {
            System.out.println("Cannot remove coach. Minimum 1 coach required.");
        }
    }

    public TrainClass getTrainClass() { return trainClass; }
    public void setTrainClass(TrainClass trainClass) { this.trainClass = trainClass; }
    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }
    public int getCoachCount() { return coachCount; }
    public void setCoachCount(int coachCount) { this.coachCount = coachCount; }
    public double getTotalKilometers() { return totalKilometers; }
    public void setTotalKilometers(double totalKilometers) { this.totalKilometers = totalKilometers; }
    public double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }
    public LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDate lastServiceDate) { this.lastServiceDate = lastServiceDate; }
    public boolean isOperational() { return isOperational; }
    public void setOperational(boolean operational) { isOperational = operational; }
    public boolean hasAccessibilityRamp() { return hasAccessibilityRamp; }
    public void setHasAccessibilityRamp(boolean hasAccessibilityRamp) { this.hasAccessibilityRamp = hasAccessibilityRamp; }
    public boolean hasDiningCar() { return hasDiningCar; }
    public void setHasDiningCar(boolean hasDiningCar) { this.hasDiningCar = hasDiningCar; }
    public boolean hasSleeperCoach() { return hasSleeperCoach; }
    public void setHasSleeperCoach(boolean hasSleeperCoach) { this.hasSleeperCoach = hasSleeperCoach; }
    public String getEngineType() { return engineType; }
    public void setEngineType(String engineType) { this.engineType = engineType; }
    public ScheduleStatus getScheduleStatus() { return scheduleStatus; }
    public void setScheduleStatus(ScheduleStatus scheduleStatus) { this.scheduleStatus = scheduleStatus; }

    @Override
    public String toString() {
        return String.format("Train{id='%s', name='%s', class='%s', seats=%d/%d, status='%s', coaches=%d}",
                getVehicleId(), trainName, trainClass, (getTotalSeats() - getAvailableSeats()),
                getTotalSeats(), getStatus(), coachCount);
    }
}
