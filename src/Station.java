import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Station {
    private int stationId;
    private String stationName;
    private String location;
    private List<String> facilities;
    private List<Route> connectedRoutes;
    private List<Bus> currentBuses;
    private List<Train> currentTrains;
    private int capacity;
    private boolean isOperational;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    public Station(int stationId, String stationName, String location, List<String> facilities) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.location = location;
        this.facilities = facilities != null ? new ArrayList<>(facilities) : new ArrayList<>();
        this.connectedRoutes = new ArrayList<>();
        this.currentBuses = new ArrayList<>();
        this.currentTrains = new ArrayList<>();
        this.capacity = 50;
        this.isOperational = true;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void addRoute(Route route) {
        if (route != null && !connectedRoutes.contains(route)) {
            connectedRoutes.add(route);
        }
    }

    public boolean isConnectedToRoute(int routeId) {
        return connectedRoutes.stream().anyMatch(route -> route.getRouteId() == routeId);
    }

    public void busArrival(Bus bus) {
        if (bus != null && !currentBuses.contains(bus)) {
            currentBuses.add(bus);
            System.out.println("🚌 Bus " + bus.getVehicleId() + " arrived at " + stationName);
        }
    }

    public void busDeparture(Bus bus) {
        if (currentBuses.remove(bus)) {
            System.out.println("🚌 Bus " + bus.getVehicleId() + " departed from " + stationName);
        }
    }

    public void trainArrival(Train train) {
        if (train != null && !currentTrains.contains(train)) {
            currentTrains.add(train);
            System.out.println("🚄 Train " + train.getVehicleId() + " arrived at " + stationName);
        }
    }

    public void trainDeparture(Train train) {
        if (currentTrains.remove(train)) {
            System.out.println("🚄 Train " + train.getVehicleId() + " departed from " + stationName);
        }
    }

    public void displayStationSchedules() {
        System.out.println("=== " + stationName.toUpperCase() + " STATION ===");
        System.out.println("Location: " + location);
        System.out.println("Status: " + (isOperational ? "Operational" : "Closed"));
        System.out.println();

        if (!currentBuses.isEmpty()) {
            System.out.println("🚌 BUSES AT STATION:");
            Passenger.displayScheduleToPassengers(currentBuses);
        }

        if (!currentTrains.isEmpty()) {
            System.out.println("🚄 TRAINS AT STATION:");
            for (Train train : currentTrains) {
                System.out.println(train.getVehicleSpecifications());
                System.out.println();
            }
        }

        if (currentBuses.isEmpty() && currentTrains.isEmpty()) {
            System.out.println("No vehicles currently at station");
        }
    }

    public boolean hasAccessibilityFacilities() {
        return facilities.contains("Wheelchair Access") ||
                facilities.contains("Elevator") ||
                facilities.contains("Ramp");
    }

    public void displayAccessibilityInfo() {
        System.out.println("♿ Accessibility at " + stationName + ":");
        if (hasAccessibilityFacilities()) {
            facilities.stream()
                    .filter(f -> f.contains("Wheelchair") || f.contains("Elevator") || f.contains("Ramp"))
                    .forEach(f -> System.out.println(" ✅ " + f));
        } else {
            System.out.println(" ❌ No accessibility facilities available");
        }
    }

    public void displayStationInfo() {
        System.out.println("=== Station Information ===");
        System.out.println("Station ID: " + stationId);
        System.out.println("Name: " + stationName);
        System.out.println("Location: " + location);
        System.out.println("Facilities: " + String.join(", ", facilities));
        System.out.println("Connected Routes: " + connectedRoutes.size());
        System.out.println("Current Buses: " + currentBuses.size());
        System.out.println("Current Trains: " + currentTrains.size());
        System.out.println("Capacity: " + capacity + " passengers");
        System.out.println("Status: " + (isOperational ? "Operational" : "Closed"));
        System.out.println("===========================");
    }

    public boolean hasFacility(String facility) {
        return facilities.contains(facility);
    }

    public void addFacility(String facility) {
        if (facility != null && !facility.trim().isEmpty() && !facilities.contains(facility)) {
            facilities.add(facility);
        }
    }

    public static void initializeDatabase() {
        String createStationTable = """
            CREATE TABLE IF NOT EXISTS stations (
                stationId INT PRIMARY KEY,
                stationName VARCHAR(100) NOT NULL,
                location VARCHAR(200) NOT NULL,
                facilities TEXT,
                capacity INT DEFAULT 50,
                isOperational BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createStationTable);
            System.out.println("Station database table initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Error initializing station database: " + e.getMessage());
        }
    }

    // Getters and Setters
    public int getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getFacilities() {
        return new ArrayList<>(facilities);
    }

    public List<Route> getConnectedRoutes() {
        return new ArrayList<>(connectedRoutes);
    }

    public List<Bus> getCurrentBuses() {
        return new ArrayList<>(currentBuses);
    }

    public List<Train> getCurrentTrains() {
        return new ArrayList<>(currentTrains);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isOperational() {
        return isOperational;
    }

    public void setOperational(boolean operational) {
        isOperational = operational;
    }

    @Override
    public String toString() {
        return "Station{" +
                "stationId=" + stationId +
                ", stationName='" + stationName + '\'' +
                ", location='" + location + '\'' +
                ", vehicles=" + (currentBuses.size() + currentTrains.size()) +
                '}';
    }
}
