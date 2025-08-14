import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class Route {
    private int routeId;
    private String startLocation;
    private String endLocation;
    private double distance;
    private String distanceUnit;
    private List<String> stops;
    private double baseFare;
    private boolean isActive;
    private LocalDateTime createdAt;
    private List<Station> stations;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    public Route(int routeId, String startLocation, String endLocation, double distance, String distanceUnit, List<String> stops) {
        this.routeId = routeId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.distanceUnit = distanceUnit;
        this.stops = stops != null ? new ArrayList<>(stops) : new ArrayList<>();
        this.baseFare = calculateBaseFare();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.stations = new ArrayList<>();
    }

    // Constructor 2: Database loading constructor
    public Route(int routeId, String startLocation, String endLocation, double distance, String distanceUnit,
                 List<String> stops, double baseFare, boolean isActive, LocalDateTime createdAt) {
        this.routeId = routeId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.distanceUnit = distanceUnit;
        this.stops = stops != null ? new ArrayList<>(stops) : new ArrayList<>();
        this.baseFare = baseFare;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.stations = new ArrayList<>();
    }

    // Constructor 3: Create route with specific base fare (distance calculated from fare)
    public Route(int routeId, String startLocation, String endLocation, double baseFare, boolean useBaseFare) {
        this.routeId = routeId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.baseFare = baseFare;
        this.distance = calculateDistanceFromFare(baseFare);
        this.distanceUnit = "km";
        this.stops = new ArrayList<>();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.stations = new ArrayList<>();
    }

    // Constructor 4: Simple constructor with distance (fare calculated from distance)
    public Route(int routeId, String startLocation, String endLocation, double distance) {
        this.routeId = routeId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.distanceUnit = "km";
        this.stops = new ArrayList<>();
        this.baseFare = calculateBaseFare();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.stations = new ArrayList<>();
    }
    public static List<Route> searchRoutes(String searchTerm) {
        List<Route> searchResults = new ArrayList<>();
        String query = "SELECT * FROM routes WHERE " +
                "UPPER(startLocation) LIKE UPPER(CONCAT('%', ?, '%')) OR " +
                "UPPER(endLocation) LIKE UPPER(CONCAT('%', ?, '%')) OR " +
                "UPPER(stops) LIKE UPPER(CONCAT('%', ?, '%'))";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Route route = createRouteFromResultSet(rs);
                    searchResults.add(route);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching routes: " + e.getMessage());
        }

        return searchResults;
    }

    private static Route createRouteFromResultSet(ResultSet rs) throws SQLException {
        int routeId = rs.getInt("routeId");
        String startLocation = rs.getString("startLocation");
        String endLocation = rs.getString("endLocation");
        double distance = rs.getDouble("distance");
        String distanceUnit = rs.getString("distanceUnit");
        double baseFare = rs.getDouble("baseFare");
        boolean isActive = rs.getBoolean("isActive");
        String stopsString = rs.getString("stops");

        List<String> stops = new ArrayList<>();
        if (stopsString != null && !stopsString.trim().isEmpty()) {
            String[] stopArray = stopsString.split(",");
            for (String stop : stopArray) {
                stops.add(stop.trim());
            }
        }

        return new Route(routeId, startLocation, endLocation, distance,
                distanceUnit, stops, baseFare, isActive, LocalDateTime.now());
    }


    public static Route createRouteWithBaseFare(int routeId, String startLocation, String endLocation, double baseFare) {
        return new Route(routeId, startLocation, endLocation, baseFare, true);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private double calculateBaseFare() {
        double baseRate = 12.0;
        double distanceFare = distance * baseRate;
        double stopsSurcharge = stops.size() * 5.0;
        return distanceFare + stopsSurcharge;
    }

    private double calculateDistanceFromFare(double fare) {
        double baseRate = 12.0;
        return fare / baseRate;
    }

    public double getPrice() {
        return this.baseFare;
    }

    public double calculateETA(double speed) {
        if (speed <= 0) {
            return -1;
        }
        double baseTime = distance / speed;
        double stopTime = stops.size() * 0.083;
        return baseTime + stopTime;
    }

    public String getFormattedETA(double speed) {
        double hours = calculateETA(speed);
        if (hours < 0) return "Invalid speed";
        int totalMinutes = (int)(hours * 60);
        int displayHours = totalMinutes / 60;
        int displayMinutes = totalMinutes % 60;
        if (displayHours > 0) {
            return String.format("%dh %dmin", displayHours, displayMinutes);
        } else {
            return String.format("%dmin", displayMinutes);
        }
    }
    public static void addTrainClassColumn() {
        String addColumnQuery = "ALTER TABLE tickets ADD COLUMN IF NOT EXISTS train_class VARCHAR(20) DEFAULT 'ECONOMY'";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(addColumnQuery);
            System.out.println("Train class column added successfully to tickets table.");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate column")) {
                System.out.println("Error adding train class column: " + e.getMessage());
            }
        }
    }



    public void addStop(String location) {
        if (location != null && !location.trim().isEmpty() && !stops.contains(location)) {
            stops.add(location);
            this.baseFare = calculateBaseFare();
            updateRouteInDatabase();
        }
    }

    public boolean removeStop(String location) {
        boolean removed = stops.remove(location);
        if (removed) {
            this.baseFare = calculateBaseFare();
            updateRouteInDatabase();
        }
        return removed;
    }

    public void addStation(Station station) {
        if (station != null && !stations.contains(station)) {
            stations.add(station);
            station.addRoute(this);
        }
    }

    public boolean removeStation(Station station) {
        return stations.remove(station);
    }

    public List<Station> getStationsOnRoute() {
        return new ArrayList<>(stations);
    }

    public String getFullRoute() {
        if (stops.isEmpty()) {
            return startLocation + " to " + endLocation + " (direct route)";
        }
        return startLocation + " to " + endLocation + " with stops: " + String.join(", ", stops);
    }

    public boolean isDirectRoute() {
        return stops.isEmpty();
    }

    public int getTotalStops() {
        return stops.size();
    }

    public String getFormattedBaseFare() {
        return String.format("৳%.2f", baseFare);
    }

    public String getFormattedDistance() {
        return String.format("%.1f %s", distance, distanceUnit);
    }

    public void displayRouteInfo() {
        System.out.println("=== Route Information ===");
        System.out.println("Route ID: " + routeId);
        System.out.println("From: " + startLocation + " To: " + endLocation);
        System.out.println("Distance: " + getFormattedDistance());
        System.out.println("Base Fare: " + getFormattedBaseFare());
        System.out.println("Stops: " + (stops.isEmpty() ? "None (Direct route)" : String.join(", ", stops)));
        System.out.println("Total Stops: " + getTotalStops());
        System.out.println("Stations on Route: " + stations.size());
        System.out.println("Status: " + (isActive ? "Active" : "Inactive"));
        System.out.println("Created: " + createdAt.toLocalDate());
        System.out.println("========================");
    }

    public boolean saveRoute() {
        String query = "INSERT INTO routes (routeId, startLocation, endLocation, distance, distanceUnit, baseFare, isActive, stops) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, routeId);
            stmt.setString(2, startLocation);
            stmt.setString(3, endLocation);
            stmt.setDouble(4, distance);
            stmt.setString(5, distanceUnit);
            stmt.setDouble(6, baseFare);
            stmt.setBoolean(7, isActive);
            stmt.setString(8, String.join(",", stops));

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Route " + routeId + " saved successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error saving route: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void updateRouteInDatabase() {
        String query = "UPDATE routes SET baseFare = ?, stops = ? WHERE routeId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, baseFare);
            stmt.setString(2, String.join(",", stops));
            stmt.setInt(3, routeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating route: " + e.getMessage());
        }
    }

    public static Route getRoute(int routeId) {
        String query = "SELECT * FROM routes WHERE routeId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, routeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String startLocation = rs.getString("startLocation");
                    String endLocation = rs.getString("endLocation");
                    double distance = rs.getDouble("distance");
                    String distanceUnit = rs.getString("distanceUnit");
                    double baseFare = rs.getDouble("baseFare");
                    boolean isActive = rs.getBoolean("isActive");
                    String stopsString = rs.getString("stops");

                    List<String> stops = new ArrayList<>();
                    if (stopsString != null && !stopsString.trim().isEmpty()) {
                        String[] stopArray = stopsString.split(",");
                        for (String stop : stopArray) {
                            stops.add(stop.trim());
                        }
                    }

                    Timestamp createdTimestamp = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = createdTimestamp != null ?
                            createdTimestamp.toLocalDateTime() : LocalDateTime.now();

                    return new Route(routeId, startLocation, endLocation, distance,
                            distanceUnit, stops, baseFare, isActive, createdAt);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving route: " + e.getMessage());
        }
        return null;
    }

    public static void initializeDatabase() {
        String createRouteTable = """
                CREATE TABLE IF NOT EXISTS routes (
                    routeId INT PRIMARY KEY,
                    startLocation VARCHAR(100) NOT NULL,
                    endLocation VARCHAR(100) NOT NULL,
                    distance DOUBLE NOT NULL,
                    distanceUnit VARCHAR(10) DEFAULT 'km',
                    baseFare DECIMAL(10,2) NOT NULL,
                    isActive BOOLEAN DEFAULT TRUE,
                    stops TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createRouteTable);
            System.out.println("Route database table initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Error initializing route database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getter methods
    public int getRouteId() {
        return routeId;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public double getDistance() {
        return distance;
    }

    public String getDistanceUnit() {
        return distanceUnit;
    }

    public List<String> getStops() {
        return new ArrayList<>(stops);
    }

    public double getBaseFare() {
        return baseFare;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Route{" +
                "routeId=" + routeId +
                ", from='" + startLocation + '\'' +
                ", to='" + endLocation + '\'' +
                ", distance=" + distance +
                ", stops=" + stops.size() +
                ", stations=" + stations.size() +
                '}';
    }
}