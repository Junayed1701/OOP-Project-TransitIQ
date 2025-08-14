import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class Schedule {
    private int scheduleId;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private LocalDate date;
    private int availableSeats;
    private int totalSeats;
    private String status;
    private String vehicleId;
    private String vehicleType;
    private int routeId;
    private double farePerSeat;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    public Schedule(int scheduleId, LocalTime departureTime, LocalTime arrivalTime,
                    LocalDate date, int availableSeats, String status) {
        this.scheduleId = scheduleId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.date = date;
        this.availableSeats = availableSeats;
        this.totalSeats = availableSeats;
        this.status = status;
        this.vehicleId = null;
        this.vehicleType = "BUS";
        this.routeId = 0;
        this.farePerSeat = 0.0;
    }

    public Schedule(int scheduleId, LocalTime departureTime, LocalTime arrivalTime,
                    LocalDate date, int availableSeats, int totalSeats, String status,
                    String vehicleId, String vehicleType, int routeId, double farePerSeat) {
        this.scheduleId = scheduleId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.date = date;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
        this.status = status;
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.routeId = routeId;
        this.farePerSeat = farePerSeat;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public boolean isDeparted() {
        LocalDateTime scheduledDeparture = LocalDateTime.of(date, departureTime);
        return scheduledDeparture.isBefore(LocalDateTime.now());
    }

    public Duration calculateDuration() {
        if (arrivalTime.isBefore(departureTime)) {
            return Duration.between(departureTime, arrivalTime.plusHours(24));
        }
        return Duration.between(departureTime, arrivalTime);
    }

    public String getFormattedDuration() {
        Duration duration = calculateDuration();
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %dmin", hours, minutes);
    }

    public boolean updateSeats(int change) {
        if (availableSeats + change >= 0 && availableSeats + change <= totalSeats) {
            availableSeats += change;
            updateScheduleInDatabase();
            return true;
        }
        return false;
    }

    public boolean bookSeat() {
        return updateSeats(-1);
    }

    public boolean cancelSeat() {
        return updateSeats(1);
    }

    public boolean isFull() {
        return availableSeats == 0;
    }

    public boolean isAvailable() {
        return !isDeparted() && !isFull() && "ACTIVE".equals(status);
    }

    public double getOccupancyRate() {
        return ((double)(totalSeats - availableSeats) / totalSeats) * 100;
    }

    public String getFormattedOccupancyRate() {
        return String.format("%.1f%%", getOccupancyRate());
    }

    public double calculateTotalRevenue() {
        int bookedSeats = totalSeats - availableSeats;
        return bookedSeats * farePerSeat;
    }

    public String getFormattedTotalRevenue() {
        return String.format("৳%.2f", calculateTotalRevenue());
    }

    public String getFormattedFarePerSeat() {
        return String.format("৳%.2f", farePerSeat);
    }

    public boolean isToday() {
        return date.equals(LocalDate.now());
    }

    public boolean isTomorrow() {
        return date.equals(LocalDate.now().plusDays(1));
    }

    public boolean isPastSchedule() {
        LocalDateTime scheduledArrival = LocalDateTime.of(date, arrivalTime);
        return scheduledArrival.isBefore(LocalDateTime.now());
    }

    public void displaySchedule() {
        System.out.println("=== Schedule Information ===");
        System.out.println("Schedule ID: " + scheduleId);
        System.out.println("Date: " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        System.out.println("Departure: " + departureTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        System.out.println("Arrival: " + arrivalTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        System.out.println("Duration: " + getFormattedDuration());

        if (vehicleId != null) {
            System.out.println("Vehicle: " + vehicleType + " " + vehicleId);
        }

        if (routeId > 0) {
            System.out.println("Route ID: " + routeId);
        }

        System.out.println("Available Seats: " + availableSeats + "/" + totalSeats);
        System.out.println("Occupancy Rate: " + getFormattedOccupancyRate());

        if (farePerSeat > 0) {
            System.out.println("Fare per Seat: " + getFormattedFarePerSeat());
            System.out.println("Total Revenue: " + getFormattedTotalRevenue());
        }

        System.out.println("Status: " + status);

        if (isDeparted()) {
            System.out.println("🚀 Already Departed");
        } else if (isFull()) {
            System.out.println("🚫 Fully Booked");
        } else if (isAvailable()) {
            System.out.println("✅ Available for Booking");
        } else {
            System.out.println("❌ Not Available");
        }

        System.out.println("============================");
    }

    public void displayCompactSchedule() {
        String timeRange = departureTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                " - " + arrivalTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        String seatInfo = availableSeats + "/" + totalSeats + " seats";
        String fareInfo = farePerSeat > 0 ? " (৳" + String.format("%.0f", farePerSeat) + ")" : "";

        System.out.println(String.format("🚌 %s | %s | %s | %s%s",
                scheduleId, timeRange, seatInfo, status, fareInfo));
    }

    public boolean saveSchedule() {
        String query = "INSERT INTO schedules (scheduleId, departureTime, arrivalTime, date, availableSeats, totalSeats, status, vehicleId, vehicleType, routeId, farePerSeat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, scheduleId);
            stmt.setTime(2, Time.valueOf(departureTime));
            stmt.setTime(3, Time.valueOf(arrivalTime));
            stmt.setDate(4, Date.valueOf(date));
            stmt.setInt(5, availableSeats);
            stmt.setInt(6, totalSeats);
            stmt.setString(7, status);
            stmt.setString(8, vehicleId);
            stmt.setString(9, vehicleType);
            stmt.setInt(10, routeId);
            stmt.setDouble(11, farePerSeat);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Schedule " + scheduleId + " saved successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error saving schedule: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void updateScheduleInDatabase() {
        String query = "UPDATE schedules SET availableSeats = ? WHERE scheduleId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, availableSeats);
            stmt.setInt(2, scheduleId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error updating schedule: " + e.getMessage());
        }
    }

    public static Schedule getSchedule(int scheduleId) {
        String query = "SELECT * FROM schedules WHERE scheduleId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, scheduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createScheduleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving schedule: " + e.getMessage());
        }
        return null;
    }

    public static List<Schedule> getSchedulesByDate(LocalDate date) {
        List<Schedule> schedules = new ArrayList<>();
        String query = "SELECT * FROM schedules WHERE date = ? ORDER BY departureTime";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(createScheduleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving schedules by date: " + e.getMessage());
        }
        return schedules;
    }

    public static List<Schedule> getAvailableSchedules(LocalDate date) {
        List<Schedule> schedules = new ArrayList<>();
        String query = "SELECT * FROM schedules WHERE date = ? AND status = 'ACTIVE' AND availableSeats > 0 ORDER BY departureTime";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(createScheduleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving available schedules: " + e.getMessage());
        }
        return schedules;
    }

    private static Schedule createScheduleFromResultSet(ResultSet rs) throws SQLException {
        int scheduleId = rs.getInt("scheduleId");
        LocalTime departureTime = rs.getTime("departureTime").toLocalTime();
        LocalTime arrivalTime = rs.getTime("arrivalTime").toLocalTime();
        LocalDate date = rs.getDate("date").toLocalDate();
        int availableSeats = rs.getInt("availableSeats");
        int totalSeats = rs.getInt("totalSeats");
        String status = rs.getString("status");
        String vehicleId = rs.getString("vehicleId");
        String vehicleType = rs.getString("vehicleType");
        int routeId = rs.getInt("routeId");
        double farePerSeat = rs.getDouble("farePerSeat");

        return new Schedule(scheduleId, departureTime, arrivalTime, date,
                availableSeats, totalSeats, status, vehicleId,
                vehicleType, routeId, farePerSeat);
    }

    public static void displayDailySchedules(LocalDate date) {
        System.out.println("=== SCHEDULES FOR " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toUpperCase() + " ===");
        List<Schedule> schedules = getSchedulesByDate(date);

        if (schedules.isEmpty()) {
            System.out.println("No schedules found for this date.");
            return;
        }

        for (Schedule schedule : schedules) {
            schedule.displayCompactSchedule();
        }
        System.out.println("Total Schedules: " + schedules.size());
        System.out.println("===============================================");
    }

    public static void initializeDatabase() {
        String createScheduleTable = """
            CREATE TABLE IF NOT EXISTS schedules (
                scheduleId INT PRIMARY KEY,
                departureTime TIME NOT NULL,
                arrivalTime TIME NOT NULL,
                date DATE NOT NULL,
                availableSeats INT NOT NULL,
                totalSeats INT NOT NULL,
                status VARCHAR(50) DEFAULT 'ACTIVE',
                vehicleId VARCHAR(50),
                vehicleType VARCHAR(50) DEFAULT 'BUS',
                routeId INT,
                farePerSeat DECIMAL(10,2) DEFAULT 0.00,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createScheduleTable);
            System.out.println("Schedule database table initialized successfully.");

        } catch (SQLException e) {
            System.out.println("Error initializing schedule database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Schedule database connection successful!");
            return true;
        } catch (SQLException e) {
            System.out.println("Schedule database connection failed: " + e.getMessage());
            return false;
        }
    }

    // Getters and Setters
    public int getScheduleId() {
        return scheduleId;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public double getFarePerSeat() {
        return farePerSeat;
    }

    public void setFarePerSeat(double farePerSeat) {
        this.farePerSeat = farePerSeat;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleId=" + scheduleId +
                ", date=" + date +
                ", departure=" + departureTime +
                ", arrival=" + arrivalTime +
                ", seats=" + availableSeats + "/" + totalSeats +
                ", status='" + status + '\'' +
                '}';
    }
}
