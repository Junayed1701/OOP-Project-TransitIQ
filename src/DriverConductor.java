import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DriverConductor {
    // Database connection constants
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    // Instance attributes
    private int employeeId;
    private String name;
    private String licenseNumber;
    private String contactInfo;
    private LocalDate hireDate;
    private double salary;
    private String shift;
    private String assignedVehicle;
    private String role;
    private boolean isAvailable;

    // Constructor
    public DriverConductor(int employeeId, String name, String licenseNumber, String shift,
                           String assignedVehicle, String role) {
        this.employeeId = employeeId;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.shift = shift;
        this.assignedVehicle = assignedVehicle;
        this.role = role;
        this.isAvailable = true;
        this.hireDate = LocalDate.now();
        this.salary = 0.0;
        this.contactInfo = "";
    }

    // Complete constructor with all fields
    public DriverConductor(int employeeId, String name, String licenseNumber, String contactInfo,
                           LocalDate hireDate, double salary, String shift, String assignedVehicle,
                           String role, boolean isAvailable) {
        this.employeeId = employeeId;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.contactInfo = contactInfo;
        this.hireDate = hireDate;
        this.salary = salary;
        this.shift = shift;
        this.assignedVehicle = assignedVehicle;
        this.role = role;
        this.isAvailable = isAvailable;
    }

    // Database connection method
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Create driver_conductors table
    public static void createDriverConductorTable() {
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS driver_conductors (
                employeeId INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                licenseNumber VARCHAR(50),
                contactInfo VARCHAR(100),
                hireDate DATE,
                salary DECIMAL(10,2) DEFAULT 0.00,
                shift VARCHAR(20),
                assignedVehicle VARCHAR(50),
                role VARCHAR(20) NOT NULL,
                isAvailable BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                
                FOREIGN KEY (assignedVehicle) REFERENCES vehicles(vehicleId)
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createTableQuery);
            System.out.println("✅ DriverConductor table created/verified successfully");

        } catch (SQLException e) {
            System.out.println("❌ Error creating driver_conductors table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Save to database
    public boolean saveToDatabase() {
        String query = "INSERT INTO driver_conductors (name, licenseNumber, contactInfo, hireDate, salary, shift, assignedVehicle, role, isAvailable) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, licenseNumber);
            stmt.setString(3, contactInfo);
            stmt.setDate(4, Date.valueOf(hireDate));
            stmt.setDouble(5, salary);
            stmt.setString(6, shift);
            stmt.setString(7, assignedVehicle);
            stmt.setString(8, role);
            stmt.setBoolean(9, isAvailable);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.employeeId = generatedKeys.getInt(1);
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error saving driver/conductor: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Update in database
    public boolean updateInDatabase() {
        String query = "UPDATE driver_conductors SET name = ?, licenseNumber = ?, contactInfo = ?, salary = ?, shift = ?, assignedVehicle = ?, role = ?, isAvailable = ?, updated_at = CURRENT_TIMESTAMP WHERE employeeId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, licenseNumber);
            stmt.setString(3, contactInfo);
            stmt.setDouble(4, salary);
            stmt.setString(5, shift);
            stmt.setString(6, assignedVehicle);
            stmt.setString(7, role);
            stmt.setBoolean(8, isAvailable);
            stmt.setInt(9, employeeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating driver/conductor: " + e.getMessage());
            return false;
        }
    }

    // Delete from database
    public boolean deleteFromDatabase() {
        String query = "DELETE FROM driver_conductors WHERE employeeId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting driver/conductor: " + e.getMessage());
            return false;
        }
    }

    // Get driver/conductor by ID
    public static DriverConductor getDriverConductorById(int employeeId) {
        String query = "SELECT * FROM driver_conductors WHERE employeeId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving driver/conductor: " + e.getMessage());
        }

        return null;
    }

    // Get all driver/conductors
    public static List<DriverConductor> getAllDriverConductors() {
        List<DriverConductor> employees = new ArrayList<>();
        String query = "SELECT * FROM driver_conductors ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                employees.add(createFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving all driver/conductors: " + e.getMessage());
        }

        return employees;
    }

    // Get available drivers
    public static List<DriverConductor> getAvailableDrivers() {
        List<DriverConductor> availableDrivers = new ArrayList<>();
        String query = "SELECT * FROM driver_conductors WHERE role = 'DRIVER' AND isAvailable = TRUE ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                availableDrivers.add(createFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving available drivers: " + e.getMessage());
        }

        return availableDrivers;
    }

    // Get available conductors
    public static List<DriverConductor> getAvailableConductors() {
        List<DriverConductor> availableConductors = new ArrayList<>();
        String query = "SELECT * FROM driver_conductors WHERE role = 'CONDUCTOR' AND isAvailable = TRUE ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                availableConductors.add(createFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving available conductors: " + e.getMessage());
        }

        return availableConductors;
    }

    // Get employees by shift
    public static List<DriverConductor> getEmployeesByShift(String shift) {
        List<DriverConductor> shiftEmployees = new ArrayList<>();
        String query = "SELECT * FROM driver_conductors WHERE shift = ? ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, shift);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shiftEmployees.add(createFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving employees by shift: " + e.getMessage());
        }

        return shiftEmployees;
    }

    // Assign vehicle in database
    public static boolean assignVehicleInDatabase(int employeeId, String vehicleId) {
        String query = "UPDATE driver_conductors SET assignedVehicle = ?, isAvailable = FALSE, updated_at = CURRENT_TIMESTAMP WHERE employeeId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, vehicleId);
            stmt.setInt(2, employeeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error assigning vehicle: " + e.getMessage());
            return false;
        }
    }

    // Unassign vehicle in database
    public static boolean unassignVehicleInDatabase(int employeeId) {
        String query = "UPDATE driver_conductors SET assignedVehicle = NULL, isAvailable = TRUE, updated_at = CURRENT_TIMESTAMP WHERE employeeId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error unassigning vehicle: " + e.getMessage());
            return false;
        }
    }

    // Helper method to create object from ResultSet
    private static DriverConductor createFromResultSet(ResultSet rs) throws SQLException {
        return new DriverConductor(
                rs.getInt("employeeId"),
                rs.getString("name"),
                rs.getString("licenseNumber"),
                rs.getString("contactInfo"),
                rs.getDate("hireDate").toLocalDate(),
                rs.getDouble("salary"),
                rs.getString("shift"),
                rs.getString("assignedVehicle"),
                rs.getString("role"),
                rs.getBoolean("isAvailable")
        );
    }

    // Original business logic methods
    public boolean canDrive() {
        return "DRIVER".equals(role) &&
                licenseNumber != null &&
                !licenseNumber.trim().isEmpty() &&
                isAvailable;
    }

    public boolean isAssigned() {
        return assignedVehicle != null && !assignedVehicle.trim().isEmpty();
    }

    public void assignVehicle(String vehicleId) {
        if (vehicleId != null && !vehicleId.trim().isEmpty()) {
            this.assignedVehicle = vehicleId;
            this.isAvailable = false;
            updateInDatabase(); // Update in database
        }
    }

    public void unassignVehicle() {
        this.assignedVehicle = null;
        this.isAvailable = true;
        updateInDatabase(); // Update in database
    }

    public boolean isOnShift(String currentShift) {
        return shift != null && shift.equals(currentShift);
    }

    public void updateAvailability(boolean available) {
        this.isAvailable = available;
        updateInDatabase(); // Update in database
    }

    public void displayEmployeeInfo() {
        System.out.println("=== Employee Information ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Name: " + name);
        System.out.println("Role: " + role);
        System.out.println("License: " + (licenseNumber != null ? licenseNumber : "N/A"));
        System.out.println("Contact: " + (contactInfo != null ? contactInfo : "N/A"));
        System.out.println("Hire Date: " + hireDate);
        System.out.println("Salary: ৳" + String.format("%.2f", salary));
        System.out.println("Shift: " + (shift != null ? shift : "Not assigned"));
        System.out.println("Assigned Vehicle: " + (assignedVehicle != null ? assignedVehicle : "None"));
        System.out.println("Available: " + (isAvailable ? "Yes" : "No"));
        System.out.println("===========================");
    }

    // Getters and Setters
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAssignedVehicle() {
        return assignedVehicle;
    }

    public void setAssignedVehicle(String assignedVehicle) {
        this.assignedVehicle = assignedVehicle;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "DriverConductor{" +
                "employeeId=" + employeeId +
                ", name='" + name + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", hireDate=" + hireDate +
                ", salary=" + salary +
                ", shift='" + shift + '\'' +
                ", assignedVehicle='" + assignedVehicle + '\'' +
                ", role='" + role + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
