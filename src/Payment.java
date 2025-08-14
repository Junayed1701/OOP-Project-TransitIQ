import java.sql.*;
import java.time.LocalDateTime;

public class Payment {
    // Database connection constants
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    private int paymentId;
    private PaymentMethod paymentMethod;
    private double amount;
    private PaymentStatus paymentStatus;
    private LocalDateTime transactionDate;
    private String currency;

    // Constructor Overloading (Compile-time Polymorphism Examples)
    public Payment(double amount, PaymentMethod method) {
        this.paymentId = generatePaymentId();
        this.amount = amount;
        this.paymentMethod = method;
        this.paymentStatus = PaymentStatus.PENDING;
        this.transactionDate = LocalDateTime.now();
        this.currency = "BDT";
    }

    public Payment(int paymentId, double amount, PaymentMethod method) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMethod = method;
        this.paymentStatus = PaymentStatus.PENDING;
        this.transactionDate = LocalDateTime.now();
        this.currency = "BDT";
    }

    public Payment(int paymentId, PaymentMethod paymentMethod, double amount,
                   PaymentStatus paymentStatus, LocalDateTime transactionDate, String currency) {
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.transactionDate = transactionDate;
        this.currency = currency;
    }

    public Payment(double amount, PaymentMethod method, PaymentStatus status) {
        this.paymentId = generatePaymentId();
        this.amount = amount;
        this.paymentMethod = method;
        this.paymentStatus = status;
        this.transactionDate = LocalDateTime.now();
        this.currency = "BDT";
    }

    // Database connection method
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // FIXED: Main payment processing method that always succeeds for valid payments
    public boolean processPayment() {
        try {
            // Basic validation
            if (this.amount <= 0) {
                this.paymentStatus = PaymentStatus.FAILED;
                return false;
            }

            if (this.paymentMethod == null) {
                this.paymentStatus = PaymentStatus.FAILED;
                return false;
            }

            this.paymentStatus = PaymentStatus.PENDING;


            Thread.sleep(500);


            this.paymentStatus = PaymentStatus.COMPLETED;
            this.transactionDate = LocalDateTime.now();


            saveToDatabase();

            return true;

        } catch (Exception e) {
            System.out.println("Payment processing error: " + e.getMessage());
            this.paymentStatus = PaymentStatus.FAILED;
            return false;
        }
    }

    // FIXED: Overloaded method with relaxed validation
    public boolean processPayment(double customAmount) {
        if (customAmount <= 0) {
            this.paymentStatus = PaymentStatus.FAILED;
            return false;
        }

        this.amount = customAmount;
        return processPayment();
    }

    public boolean processPayment(double amount, PaymentMethod method) {
        this.amount = amount;
        this.paymentMethod = method;
        return processPayment();
    }

    public boolean processPayment(double amount, PaymentMethod method, String notes) {
        this.amount = amount;
        this.paymentMethod = method;
        System.out.println("Processing payment with notes: " + notes);
        return processPayment();
    }

    // NEW: Save payment to database
    public boolean saveToDatabase() {
        String query = "INSERT INTO payments (paymentMethod, amount, paymentStatus, transactionDate, currency) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, paymentMethod.name());
            stmt.setDouble(2, amount);
            stmt.setString(3, paymentStatus.name());
            stmt.setTimestamp(4, Timestamp.valueOf(transactionDate));
            stmt.setString(5, currency);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.paymentId = generatedKeys.getInt(1);
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error saving payment: " + e.getMessage());
        }

        return false;
    }

    // NEW: Create payments table if it doesn't exist
    public static void createPaymentsTable() {
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS payments (
                paymentId INT AUTO_INCREMENT PRIMARY KEY,
                paymentMethod VARCHAR(50) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                paymentStatus VARCHAR(50) NOT NULL,
                transactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                currency VARCHAR(10) DEFAULT 'BDT',
                ticketId INT,
                userId INT,
                transactionFee DECIMAL(10,2),
                refundAmount DECIMAL(10,2) DEFAULT 0.00,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createTableQuery);
            System.out.println("✅ Payments table created/verified successfully");

        } catch (SQLException e) {
            System.out.println("Error creating payments table: " + e.getMessage());
        }
    }

    private int generatePaymentId() {
        return (int) (System.currentTimeMillis() % 1000000);
    }

    public double refund(double refundAmount) {
        if (!canBeRefunded() || refundAmount <= 0 || refundAmount > this.amount) {
            return 0;
        }

        this.amount -= refundAmount;
        paymentStatus = (this.amount == 0) ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED;
        return refundAmount;
    }

    public boolean isSuccessful() {
        return paymentStatus.isSuccessful();
    }

    public boolean canBeRefunded() {
        return paymentStatus.canBeRefunded() && paymentMethod.supportsRefunds();
    }

    public double calculateTransactionFee() {
        return amount * paymentMethod.getTransactionFee();
    }

    public double getTotalAmountWithFee() {
        return amount + calculateTransactionFee();
    }

    public boolean requiresAuthentication() {
        return paymentMethod.requiresAuth();
    }

    public int getExpectedProcessingTimeHours() {
        return paymentMethod.getProcessingTimeHours();
    }

    public boolean isInstantPayment() {
        return paymentMethod.isInstant();
    }

    public boolean isDigitalPayment() {
        return paymentMethod.isDigital();
    }

    public boolean canTransitionTo(PaymentStatus newStatus) {
        PaymentStatus[] validTransitions = paymentStatus.getNextValidStatuses();
        for (PaymentStatus validStatus : validTransitions) {
            if (validStatus == newStatus) {
                return true;
            }
        }
        return false;
    }

    public boolean canBeRefundedToMethod(PaymentMethod originalMethod) {
        return this.paymentMethod.supportsRefunds() &&
                this.paymentStatus.canBeRefunded() &&
                originalMethod.supportsRefunds();
    }

    public Refund initiateRefund(double refundAmount, String reason) {
        if (!canBeRefunded()) {
            return null;
        }

        return new Refund(0, null, refundAmount, RefundStatus.PENDING, reason, LocalDateTime.now());
    }

    public String getRefundEligibilityStatus() {
        if (paymentStatus.canBeRefunded()) {
            return "Eligible for refund - " + paymentStatus.getStatusDescription();
        }

        return "Not eligible for refund - " + paymentStatus.getStatusDescription();
    }

    public double calculateRefundAmount() {
        if (paymentStatus.canBeRefunded()) {
            return amount;
        }

        return 0.0;
    }

    public double getRefundProcessingFee() {
        return amount * paymentMethod.getTransactionFee() * 0.5;
    }

    public void displayPaymentInfo() {
        System.out.println("=== Payment Information ===");
        System.out.println("Payment ID: " + paymentId);
        System.out.println("Payment Method: " + paymentMethod.getDisplayName());
        System.out.println("Amount: ৳" + String.format("%.2f", amount));
        System.out.println("Transaction Fee: ৳" + String.format("%.2f", calculateTransactionFee()));
        System.out.println("Total with Fee: ৳" + String.format("%.2f", getTotalAmountWithFee()));
        System.out.println("Status: " + paymentStatus + " - " + paymentStatus.getStatusDescription());
        System.out.println("Processing Time: " + paymentMethod.getProcessingTimeHours() + " hours");
        System.out.println("Payment Type: " + (paymentMethod.isDigital() ? "Digital" : "Traditional"));
        System.out.println("Instant Payment: " + (paymentMethod.isInstant() ? "Yes" : "No"));
        System.out.println("Supports Refunds: " + (paymentMethod.supportsRefunds() ? "Yes" : "No"));
        System.out.println("Requires Auth: " + (paymentMethod.requiresAuth() ? "Yes" : "No"));
        System.out.println("Transaction Date: " + transactionDate);
        System.out.println("===========================");
    }

    public String getFormattedAmount() {
        return String.format("৳%.2f", amount);
    }

    public String getFormattedTotalWithFee() {
        return String.format("৳%.2f", getTotalAmountWithFee());
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", paymentMethod=" + paymentMethod +
                ", amount=" + amount +
                ", paymentStatus=" + paymentStatus +
                ", transactionDate=" + transactionDate +
                ", currency='" + currency + '\'' +
                '}';
    }
}
