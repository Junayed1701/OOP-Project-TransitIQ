import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Notification {
    // Database connection constants
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    // Notification attributes
    private int notificationId;
    private int userId;           // Foreign key to users table
    private Integer adminId;      // Foreign key to admins table (optional)
    private Integer ticketId;     // Foreign key to tickets table (optional)
    private Integer routeId;      // Foreign key to routes table (optional)
    private String message;
    private String type;
    private LocalDateTime date;
    private boolean isRead;
    private boolean isSent;
    private String priority;

    // Constructor Overloading
    public Notification(int notificationId, String message, String type, LocalDateTime date) {
        this.notificationId = notificationId;
        this.message = message;
        this.type = type;
        this.date = date;
        this.isRead = false;
        this.isSent = false;
        this.priority = "NORMAL";
    }

    public Notification(int userId, String message, String type) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.date = LocalDateTime.now();
        this.isRead = false;
        this.isSent = false;
        this.priority = "NORMAL";
    }

    public Notification(int userId, String message, String type, String priority) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.date = LocalDateTime.now();
        this.isRead = false;
        this.isSent = false;
        this.priority = priority;
    }

    public Notification(int userId, Integer ticketId, String message, String type) {
        this.userId = userId;
        this.ticketId = ticketId;
        this.message = message;
        this.type = type;
        this.date = LocalDateTime.now();
        this.isRead = false;
        this.isSent = false;
        this.priority = "NORMAL";
    }

    // Complete constructor with all fields
    public Notification(int notificationId, int userId, Integer adminId, Integer ticketId,
                        Integer routeId, String message, String type, LocalDateTime date,
                        boolean isRead, boolean isSent, String priority) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.adminId = adminId;
        this.ticketId = ticketId;
        this.routeId = routeId;
        this.message = message;
        this.type = type;
        this.date = date;
        this.isRead = isRead;
        this.isSent = isSent;
        this.priority = priority;
    }

    // Database connection method
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Create notifications table
    public static void createNotificationsTable() {
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS notifications (
                notificationId INT AUTO_INCREMENT PRIMARY KEY,
                userId INT NOT NULL,
                adminId INT,
                ticketId INT,
                routeId INT,
                message TEXT NOT NULL,
                type VARCHAR(50) NOT NULL,
                date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                isRead BOOLEAN DEFAULT FALSE,
                isSent BOOLEAN DEFAULT FALSE,
                priority VARCHAR(20) DEFAULT 'NORMAL',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                
                FOREIGN KEY (userId) REFERENCES users(userId),
                FOREIGN KEY (adminId) REFERENCES admins(adminId),
                FOREIGN KEY (ticketId) REFERENCES tickets(ticketId),
                FOREIGN KEY (routeId) REFERENCES routes(routeId)
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createTableQuery);
            System.out.println("✅ Notifications table created/verified successfully");

        } catch (SQLException e) {
            System.out.println("❌ Error creating notifications table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Save notification to database
    public boolean saveToDatabase() {
        String query = "INSERT INTO notifications (userId, adminId, ticketId, routeId, message, type, date, isRead, isSent, priority) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setObject(2, adminId);
            stmt.setObject(3, ticketId);
            stmt.setObject(4, routeId);
            stmt.setString(5, message);
            stmt.setString(6, type);
            stmt.setTimestamp(7, Timestamp.valueOf(date));
            stmt.setBoolean(8, isRead);
            stmt.setBoolean(9, isSent);
            stmt.setString(10, priority);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.notificationId = generatedKeys.getInt(1);
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error saving notification: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Get user notifications from database
    public static List<Notification> getUserNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE userId = ? ORDER BY date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification notification = new Notification(
                            rs.getInt("notificationId"),
                            rs.getInt("userId"),
                            (Integer) rs.getObject("adminId"),
                            (Integer) rs.getObject("ticketId"),
                            (Integer) rs.getObject("routeId"),
                            rs.getString("message"),
                            rs.getString("type"),
                            rs.getTimestamp("date").toLocalDateTime(),
                            rs.getBoolean("isRead"),
                            rs.getBoolean("isSent"),
                            rs.getString("priority")
                    );
                    notifications.add(notification);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving notifications: " + e.getMessage());
            e.printStackTrace();
        }

        return notifications;
    }

    // Get all notifications for admin dashboard
    public static List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications ORDER BY date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification notification = new Notification(
                            rs.getInt("notificationId"),
                            rs.getInt("userId"),
                            (Integer) rs.getObject("adminId"),
                            (Integer) rs.getObject("ticketId"),
                            (Integer) rs.getObject("routeId"),
                            rs.getString("message"),
                            rs.getString("type"),
                            rs.getTimestamp("date").toLocalDateTime(),
                            rs.getBoolean("isRead"),
                            rs.getBoolean("isSent"),
                            rs.getString("priority")
                    );
                    notifications.add(notification);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving all notifications: " + e.getMessage());
            e.printStackTrace();
        }

        return notifications;
    }

    // Update notification status in database
    public boolean updateInDatabase() {
        String query = "UPDATE notifications SET isRead = ?, isSent = ?, updated_at = CURRENT_TIMESTAMP WHERE notificationId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isRead);
            stmt.setBoolean(2, isSent);
            stmt.setInt(3, notificationId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating notification: " + e.getMessage());
            return false;
        }
    }

    // Delete notification from database
    public boolean deleteFromDatabase() {
        String query = "DELETE FROM notifications WHERE notificationId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting notification: " + e.getMessage());
            return false;
        }
    }

    // Get unread notifications count
    public static int getUnreadNotificationsCount(int userId) {
        String query = "SELECT COUNT(*) FROM notifications WHERE userId = ? AND isRead = FALSE";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting unread count: " + e.getMessage());
        }

        return 0;
    }

    // Mark all notifications as read for a user
    public static boolean markAllAsRead(int userId) {
        String query = "UPDATE notifications SET isRead = TRUE WHERE userId = ? AND isRead = FALSE";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error marking all as read: " + e.getMessage());
            return false;
        }
    }

    // Static factory methods for common notification types
    public static Notification createBookingConfirmation(int userId, int ticketId, String route) {
        Notification notification = new Notification(
                userId,
                ticketId,
                "Your ticket for " + route + " has been confirmed! Have a safe journey.",
                "BOOKING_CONFIRMATION"
        );
        notification.priority = "HIGH";
        return notification;
    }

    public static Notification createPaymentSuccess(int userId, double amount) {
        return new Notification(
                userId,
                "Payment of ৳" + String.format("%.2f", amount) + " processed successfully.",
                "PAYMENT_SUCCESS",
                "HIGH"
        );
    }

    public static Notification createPaymentFailed(int userId, double amount) {
        return new Notification(
                userId,
                "Payment of ৳" + String.format("%.2f", amount) + " failed. Please try again.",
                "PAYMENT_FAILED",
                "HIGH"
        );
    }

    public static Notification createRouteDelay(int userId, int routeId, String route, String delay) {
        Notification notification = new Notification(
                userId,
                "Route " + route + " is delayed by " + delay + ". Please plan accordingly.",
                "ROUTE_DELAY"
        );
        notification.routeId = routeId;
        notification.priority = "HIGH";
        return notification;
    }

    public static Notification createSystemUpdate(int userId, String updateInfo) {
        return new Notification(
                userId,
                "System Update: " + updateInfo,
                "SYSTEM_UPDATE",
                "NORMAL"
        );
    }

    // Enhanced send notification with database integration
    public boolean sendNotification() {
        if (message == null || message.trim().isEmpty()) {
            System.out.println("Cannot send empty notification");
            return false;
        }

        // Save to database first
        boolean savedToDb = saveToDatabase();

        if (savedToDb) {
            System.out.println("Sending " + type + " notification: " + message);
            isSent = true;
            updateInDatabase(); // Update the sent status in database
            return true;
        } else {
            System.out.println("Failed to save notification to database");
            return false;
        }
    }

    public void scheduleNotification() {
        if (date.isAfter(LocalDateTime.now())) {
            System.out.println("Scheduling " + type + " notification for: " +
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            // Save to database as scheduled
            saveToDatabase();
        } else {
            System.out.println("Cannot schedule notification for past date");
        }
    }

    public void markAsRead() {
        isRead = true;
        updateInDatabase(); // Update in database
    }

    public boolean isPending() {
        return !isSent && date.isAfter(LocalDateTime.now());
    }

    public boolean isOverdue() {
        return !isSent && date.isBefore(LocalDateTime.now());
    }

    public void displayNotification() {
        System.out.println("=== NOTIFICATION ===");
        System.out.println("ID: " + notificationId);
        System.out.println("Type: " + type);
        System.out.println("Priority: " + priority);
        System.out.println("Message: " + message);
        System.out.println("Date: " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("Status: " + (isSent ? "Sent" : "Pending") +
                (isRead ? " (Read)" : " (Unread)"));
        if (ticketId != null) {
            System.out.println("Related Ticket: " + ticketId);
        }
        if (routeId != null) {
            System.out.println("Related Route: " + routeId);
        }
        System.out.println("===================");
    }

    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", adminId=" + adminId +
                ", ticketId=" + ticketId +
                ", routeId=" + routeId +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                ", isRead=" + isRead +
                ", isSent=" + isSent +
                ", priority='" + priority + '\'' +
                '}';
    }
}
