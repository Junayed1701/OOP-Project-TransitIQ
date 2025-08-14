import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Refund {
    private int refundId;
    private Ticket ticket;
    private double amount;
    private RefundStatus status; // Changed to RefundStatus
    private String refundReason;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    private int processedByAdminId;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    public Refund(int refundId, Ticket ticket, double amount, RefundStatus status, String refundReason, LocalDateTime requestDate) {
        this.refundId = refundId;
        this.ticket = ticket;
        this.amount = amount;
        this.status = status;
        this.refundReason = refundReason;
        this.requestDate = requestDate;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    public boolean validateTicketForRefund() {
        if (ticket == null) {
            System.out.println("No ticket associated with refund");
            return false;
        }

        TicketStatus ticketStatus = ticket.getStatus();

        if (!ticketStatus.allowsRefund()) {
            System.out.println("Ticket status " + ticketStatus + " does not allow refunds");
            return false;
        }

        // Calculate refund amount based on ticket status
        double calculatedAmount = ticket.getPrice() * ticketStatus.getRefundPercentage();
        if (this.amount > calculatedAmount) {
            System.out.println("Refund amount exceeds maximum allowed for ticket status");
            return false;
        }

        return true;
    }

    // Enhanced AdminRole integration with RefundStatus
    public boolean approveRefund(AdminRole adminRole) {
        if (!adminRole.canApproveRefunds()) {
            System.out.println("Admin role " + adminRole.getDisplayName() + " cannot approve refunds");
            return false;
        }

        if (status != RefundStatus.PENDING) {
            System.out.println("Refund is not pending: " + status.getStatusDescription());
            return false;
        }

        // First approve, then process
        this.status = RefundStatus.APPROVED;
        return processRefund();
    }

    public boolean processRefund() {
        // Validate ticket status first
        if (!validateTicketForRefund()) {
            this.status = RefundStatus.REJECTED;
            return false;
        }

        if (status != RefundStatus.APPROVED && status != RefundStatus.PENDING) {
            System.out.println("Refund cannot be processed: " + status.getStatusDescription());
            return false;
        }

        String query = "UPDATE refunds SET status = ?, processedDate = NOW() WHERE refundId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, RefundStatus.COMPLETED.name());
            stmt.setInt(2, this.refundId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                this.status = RefundStatus.COMPLETED;
                this.processedDate = LocalDateTime.now();

                ticket.setStatus(TicketStatus.CANCELLED);

                System.out.println("Refund processed successfully: " + getFormattedAmount());
                return true;
            }

        } catch (SQLException e) {
            this.status = RefundStatus.FAILED;
            System.out.println("Error processing refund: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    public boolean rejectRefund(AdminRole adminRole, String rejectionReason) {
        if (!adminRole.canApproveRefunds()) {
            System.out.println("Admin role cannot reject refunds");
            return false;
        }

        if (status != RefundStatus.PENDING) {
            System.out.println("Refund is not pending: " + status.getStatusDescription());
            return false;
        }

        String query = "UPDATE refunds SET status = ?, processedDate = NOW() WHERE refundId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, RefundStatus.REJECTED.name());
            stmt.setInt(2, this.refundId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                this.status = RefundStatus.REJECTED;
                this.processedDate = LocalDateTime.now();

                System.out.println("Refund rejected: " + rejectionReason);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error rejecting refund: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelRefund() {
        if (status == RefundStatus.COMPLETED || status == RefundStatus.REJECTED) {
            System.out.println("Cannot cancel refund: " + status.getStatusDescription());
            return false;
        }

        this.status = RefundStatus.CANCELED;
        System.out.println("Refund cancelled successfully");
        return true;
    }

    // Enhanced status checking methods using RefundStatus
    public boolean isPending() {
        return status == RefundStatus.PENDING;
    }

    public boolean isApproved() {
        return status == RefundStatus.APPROVED;
    }

    public boolean isCompleted() {
        return status == RefundStatus.COMPLETED;
    }

    public boolean isRejected() {
        return status == RefundStatus.REJECTED;
    }

    public boolean isCanceled() {
        return status == RefundStatus.CANCELED;
    }

    public boolean isFailed() {
        return status == RefundStatus.FAILED;
    }

    // BDT currency formatting
    public String getFormattedAmount() {
        return String.format("৳%.2f", amount);
    }

    public void displayRefundDetails() {
        System.out.println("=== Refund Details ===");
        System.out.println("Refund ID: " + refundId);
        System.out.println("Ticket ID: " + ticket.getTicketId());
        System.out.println("Amount: " + getFormattedAmount());
        System.out.println("Status: " + status + " - " + status.getStatusDescription());
        System.out.println("Reason: " + refundReason);
        System.out.println("Request Date: " + requestDate);
        if (processedDate != null) {
            System.out.println("Processed Date: " + processedDate);
        }
        System.out.println("=====================");
    }

    public static void initializeDatabase() {
        String createRefundTable = """
            CREATE TABLE IF NOT EXISTS refunds (
                refundId INT AUTO_INCREMENT PRIMARY KEY,
                ticketId INT NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                status VARCHAR(50) DEFAULT 'PENDING',
                refundReason TEXT NOT NULL,
                requestDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                processedDate TIMESTAMP NULL,
                processedByAdminId INT NULL,
                FOREIGN KEY (ticketId) REFERENCES tickets(ticketId) ON DELETE CASCADE
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createRefundTable);
            System.out.println("Refund database table initialized successfully.");

        } catch (SQLException e) {
            System.out.println("Error initializing refund database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public int getRefundId() {
        return refundId;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public double getAmount() {
        return amount;
    }

    public RefundStatus getStatus() { // Changed return type
        return status;
    }

    public void setStatus(RefundStatus status) { // Changed parameter type
        this.status = status;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }
}
