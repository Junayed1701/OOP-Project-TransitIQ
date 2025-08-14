import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class Passenger extends User {
    private String address;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    public Passenger(int userId, String name, String email, char[] password, String phone,
                     String preferredLanguage, Gender gender, String address) {
        super(userId, name, email, password, phone, preferredLanguage, gender);
        this.address = address;
    }

    public Passenger(int passengerId, String name, String email, String phone,
                     String preferredLanguage, Gender gender, String address) {
        super(passengerId, name, email, null, phone, preferredLanguage, gender);
        this.address = address;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static boolean signUp(String name, String email, String phone, String preferredLanguage,
                                 Gender gender, String address) {
        String query = "INSERT INTO passengers (name, email, phone, preferredLanguage, gender, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, preferredLanguage);
            stmt.setString(5, gender.toString());
            stmt.setString(6, address);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Passenger registration successful!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static Passenger signIn(String email, String phone) {
        String query = "SELECT * FROM passengers WHERE email = ? AND phone = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, phone);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Fixed: Parse gender from database
                    Gender gender = Gender.fromString(rs.getString("gender"));
                    if (gender == null) {
                        gender = Gender.PREFER_NOT_TO_SAY;
                    }

                    return new Passenger(
                            rs.getInt("passengerId"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("preferredLanguage"),
                            gender,
                            rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during sign in: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static Passenger getPassengerDetails(int passengerId) {
        String query = "SELECT * FROM passengers WHERE passengerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, passengerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Fixed: Parse gender from database
                    Gender gender = Gender.fromString(rs.getString("gender"));
                    if (gender == null) {
                        gender = Gender.PREFER_NOT_TO_SAY;
                    }

                    return new Passenger(
                            rs.getInt("passengerId"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("preferredLanguage"),
                            gender,
                            rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving passenger: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void displayScheduleToPassengers(List<Bus> buses) {
        System.out.println("=== CURRENT SCHEDULE STATUS ===");
        System.out.println("Last Updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        System.out.println();

        if (buses == null || buses.isEmpty()) {
            System.out.println("No buses available at this time.");
            return;
        }

        for (Bus bus : buses) {
            ScheduleStatus status = bus.getScheduleStatus();
            System.out.print("🚌 Bus " + bus.getVehicleId() + ": " + status.getDisplayText());

            switch (status) {
                case ON_TIME -> System.out.print(" ✅");
                case DELAYED -> System.out.print(" ⏰");
                case CANCELLED -> System.out.print(" ❌");
                case BOARDING -> System.out.print(" 🚌");
                case DEPARTED -> System.out.print(" 🚀");
                default -> System.out.print(" ℹ️");
            }

            if (status.allowsBoarding()) {
                System.out.println(" - 🟢 Boarding Available");
            } else {
                System.out.println(" - 🔴 Boarding Not Available");
            }

            System.out.println(" Route: " + (bus.getAssignedRoute() != null ?
                    bus.getAssignedRoute().getFullRoute() : "Route info unavailable"));
            System.out.println(" Operator: " + bus.getOperator().getDisplayName());
            System.out.println(" Accessibility: " + (bus.isAccessible() ? "♿ Wheelchair Accessible" : "Standard"));

            if (status.getDelaySeverity() > 0) {
                System.out.println(" ⚠️ Delay Level: " + status.getDelaySeverity());
            }
            System.out.println();
        }
        System.out.println("================================");
    }

    public static List<Bus> getBusesByRoute(int routeId) {
        List<Bus> routeBuses = new ArrayList<>();
        System.out.println("Getting buses for route ID: " + routeId);
        return routeBuses;
    }

    public void viewAvailableSchedules(int routeId) {
        System.out.println("Schedule information for " + getName() + ":");
        List<Bus> routeBuses = getBusesByRoute(routeId);
        displayScheduleToPassengers(routeBuses);
    }

    public void viewAccessibleSchedules(int routeId) {
        System.out.println("Accessible bus schedules for " + getName() + ":");
        List<Bus> routeBuses = getBusesByRoute(routeId);
        List<Bus> accessibleBuses = new ArrayList<>();

        for (Bus bus : routeBuses) {
            if (bus.isAccessible()) {
                accessibleBuses.add(bus);
            }
        }

        if (accessibleBuses.isEmpty()) {
            System.out.println("No wheelchair accessible buses available on this route.");
        } else {
            displayScheduleToPassengers(accessibleBuses);
        }
    }

    public static void initializeDatabase() {
        String createPassengerTable = """
                CREATE TABLE IF NOT EXISTS passengers (
                    passengerId INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    phone VARCHAR(20) NOT NULL,
                    preferredLanguage VARCHAR(50) DEFAULT 'English',
                    gender VARCHAR(20),
                    address TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createPassengerTable);
            System.out.println("Passenger database table initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Error initializing passenger database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Passenger database connection successful!");
            return true;
        } catch (SQLException e) {
            System.out.println("Passenger database connection failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateProfile(String name, String phone) {
        if (!super.updateProfile(name, phone)) {
            return false;
        }

        String query = "UPDATE passengers SET name = ?, phone = ?, address = ? WHERE passengerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, this.address);
            stmt.setInt(4, this.getUserId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error updating passenger profile: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassengerProfile(String name, String email, String phone, String address, Gender gender) {
        String query = "UPDATE passengers SET name = ?, email = ?, phone = ?, address = ?, gender = ? WHERE passengerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setString(5, gender.toString());
            stmt.setInt(6, this.getUserId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                this.address = address;
                this.gender = gender;
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error updating passenger profile: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean setPreferredLanguage(String language) {
        if (super.setLanguagePreference(language)) {
            String query = "UPDATE passengers SET preferredLanguage = ? WHERE passengerId = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, language);
                stmt.setInt(2, this.getUserId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.out.println("Error updating passenger language: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public String displayInfo() {
        return "Passenger: " + name + ", Gender: " + gender +
                ", Email: " + email + ", Address: " + address;
    }

    @Override
    public String getUserType() {
        return "Passenger";
    }

    public void displayPassengerInfo() {
        System.out.println("=== Passenger Information ===");
        System.out.println("ID: " + getUserId());
        System.out.println("Name: " + getName());
        System.out.println("Email: " + getEmail());
        System.out.println("Phone: " + getPhone());
        System.out.println("Language: " + getPreferredLanguage());
        System.out.println("Gender: " + gender);
        System.out.println("Address: " + address);
        System.out.println("=============================");
    }

    // Getters and Setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getPassengerId() { return getUserId(); }

    @Override
    public String toString() {
        return "Passenger{" +
                "passengerId=" + getUserId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
