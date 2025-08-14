import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class Ticket {
    private int ticketId;
    private int passengerId;
    private String route;
    private double price;
    private TicketStatus status;
    private String transportationType;
    private LocalDateTime bookingDate;
    private LocalDateTime travelDate;
    private String paymentMethod;
    private double transactionFee;
    private Route routeObject;
    private String seatNumber;
    private boolean isRefundable;
    private Payment paymentDetails;
    private String trainClass = "ECONOMY";

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    // Constructor 1
    public Ticket(int ticketId, int passengerId, String route, double price,
                  TicketStatus status, String transportationType) {
        this.ticketId = ticketId;
        this.passengerId = passengerId;
        this.route = route;
        this.price = price;
        this.status = status;
        this.transportationType = transportationType;
        this.bookingDate = LocalDateTime.now();
        this.travelDate = null;
        this.paymentMethod = null;
        this.transactionFee = 0.0;
        this.seatNumber = null;
        this.isRefundable = true;
    }

    // Constructor 2
    public Ticket(int ticketId, int passengerId, String route, double price,
                  TicketStatus status, String transportationType, LocalDateTime bookingDate,
                  LocalDateTime travelDate, String paymentMethod, double transactionFee) {
        this.ticketId = ticketId;
        this.passengerId = passengerId;
        this.route = route;
        this.price = price;
        this.status = status;
        this.transportationType = transportationType;
        this.bookingDate = bookingDate;
        this.travelDate = travelDate;
        this.paymentMethod = paymentMethod;
        this.transactionFee = transactionFee;
        this.seatNumber = null;
        this.isRefundable = true;
    }

    // Database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ====== MISSING METHODS ADDED (FIXES MAIN.JAVA ERRORS) ======

    /**
     * Process ticket payment - MISSING METHOD FIXED
     */
    public boolean processTicketPayment(PaymentMethod paymentMethod, double amount) {
        if (amount < this.price) {
            System.out.println("❌ Payment amount (৳" + String.format("%.2f", amount) +
                    ") is less than ticket price (৳" + String.format("%.2f", this.price) + ")");
            return false;
        }

        if (this.status != TicketStatus.PENDING) {
            System.out.println("❌ Ticket is not in PENDING status. Current status: " + this.status);
            return false;
        }

        try {
            Payment payment = new Payment(
                    this.ticketId,
                    paymentMethod,
                    amount,
                    PaymentStatus.COMPLETED,
                    LocalDateTime.now(),
                    "BDT"
            );

            // Update ticket details
            this.paymentDetails = payment;
            this.paymentMethod = paymentMethod.name();
            this.transactionFee = paymentMethod.getTransactionFee();
            this.status = TicketStatus.CONFIRMED;

            // Update database
            updateTicketInDatabase();

            System.out.println("✅ Payment processed successfully!");
            System.out.println("   Amount: ৳" + String.format("%.2f", amount));
            System.out.println("   Method: " + paymentMethod.getDisplayName());
            System.out.println("   Transaction Fee: ৳" + String.format("%.2f", this.transactionFee));

            return true;

        } catch (Exception e) {
            System.out.println("❌ Payment processing failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Board passenger - MISSING METHOD FIXED
     */
    public boolean boardPassenger(Schedule schedule) {
        if (this.status != TicketStatus.CONFIRMED) {
            System.out.println("❌ Cannot board: Ticket is not confirmed. Current status: " + this.status);
            return false;
        }

        if (schedule == null) {
            System.out.println("❌ Cannot board: No valid schedule provided");
            return false;
        }

        if (!schedule.getStatus().equals("ACTIVE")) {
            System.out.println("❌ Cannot board: Schedule is not active. Status: " + schedule.getStatus());
            return false;
        }

        // Check if boarding time is appropriate
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime boardingStart = LocalDateTime.of(LocalDate.now(), schedule.getDepartureTime().minusMinutes(30));
        LocalDateTime boardingEnd = LocalDateTime.of(LocalDate.now(), schedule.getDepartureTime());

        if (now.isBefore(boardingStart)) {
            System.out.println("❌ Too early to board. Boarding starts at: " + boardingStart.toLocalTime());
            return false;
        }

        if (now.isAfter(boardingEnd)) {
            System.out.println("❌ Boarding has ended. Departure was at: " + schedule.getDepartureTime());
            return false;
        }

        try {
            this.status = TicketStatus.BOARDED;
            this.travelDate = now;

            // Update database
            updateTicketInDatabase();

            System.out.println("✅ Passenger boarded successfully!");
            System.out.println("   Ticket ID: " + this.ticketId);
            System.out.println("   Boarding Time: " + now.toLocalTime());
            System.out.println("   Transportation: " + this.transportationType);

            return true;

        } catch (Exception e) {
            System.out.println("❌ Boarding failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update ticket in database - HELPER METHOD ADDED
     */
    private void updateTicketInDatabase() throws SQLException {
        String query = "UPDATE tickets SET status = ?, paymentMethod = ?, transactionFee = ?, travelDate = ? WHERE ticketId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, this.status.name());
            stmt.setString(2, this.paymentMethod);
            stmt.setDouble(3, this.transactionFee);

            if (this.travelDate != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(this.travelDate));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }

            stmt.setInt(5, this.ticketId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No rows updated for ticket ID: " + this.ticketId);
            }

        } catch (SQLException e) {
            System.out.println("Database update failed: " + e.getMessage());
            throw e;
        }
    }

    // ====== EXISTING STATIC METHODS (FIXED) ======

    public static boolean addBooking(int passengerId, String route, double price, String transportationType) {
        return addBooking(passengerId, route, price, transportationType, null, 0.0);
    }

    public static boolean addBooking(int passengerId, String route, double price, String transportationType,
                                     String paymentMethod, double transactionFee) {
        if (route == null || route.trim().isEmpty() || price < 0 || transportationType == null) {
            System.out.println("Route, valid price, and transportation type are required");
            return false;
        }

        String query = "INSERT INTO tickets (passengerId, route, price, status, transportationType, paymentMethod, transactionFee, bookingDate) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, passengerId);
            stmt.setString(2, route);
            stmt.setDouble(3, price);
            stmt.setString(4, TicketStatus.CONFIRMED.name());
            stmt.setString(5, transportationType);
            stmt.setString(6, paymentMethod);
            stmt.setDouble(7, transactionFee);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Booking added successfully for ৳" + String.format("%.2f", price));
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error adding booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addBooking(int passengerId, String route, double price, String transportationType, String trainClass) {
        return addBooking(passengerId, route, price, transportationType, null, 0.0, trainClass);
    }

    public static boolean addBooking(int passengerId, String route, double price, String transportationType,
                                     String paymentMethod, double transactionFee, String trainClass) {
        if (route == null || route.trim().isEmpty() || price < 0 || transportationType == null) {
            System.out.println("Route, valid price, and transportation type are required");
            return false;
        }

        String query = "INSERT INTO tickets (passengerId, route, price, status, transportationType, paymentMethod, transactionFee, trainclass, bookingDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, passengerId);
            stmt.setString(2, route);
            stmt.setDouble(3, price);
            stmt.setString(4, TicketStatus.CONFIRMED.name());
            stmt.setString(5, transportationType);
            stmt.setString(6, paymentMethod);
            stmt.setDouble(7, transactionFee);
            stmt.setString(8, trainClass != null ? trainClass : "ECONOMY");

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Booking added successfully for ৳" + String.format("%.2f", price));
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error adding booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createTicket(int passengerId, String route, double price, String transportationType) {
        return addBooking(passengerId, route, price, transportationType);
    }

    public static Ticket getTicket(int ticketId) {
        String query = "SELECT * FROM tickets WHERE ticketId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createTicketFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving ticket: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static List<Ticket> getBookings(int passengerId) {
        List<Ticket> bookings = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE passengerId = ? ORDER BY bookingDate DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, passengerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ticket ticket = createTicketFromResultSet(rs);
                    bookings.add(ticket);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    public static List<Ticket> getAllTickets() {
        List<Ticket> allTickets = new ArrayList<>();
        String query = "SELECT * FROM tickets ORDER BY bookingDate DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Ticket ticket = createTicketFromResultSet(rs);
                allTickets.add(ticket);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving all tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return allTickets;
    }

    private static Ticket createTicketFromResultSet(ResultSet rs) throws SQLException {
        int ticketId = rs.getInt("ticketId");
        int passengerId = rs.getInt("passengerId");
        String route = rs.getString("route");
        double price = rs.getDouble("price");
        String statusString = rs.getString("status");

        TicketStatus status;
        try {
            status = TicketStatus.valueOf(statusString);
        } catch (IllegalArgumentException e) {
            status = TicketStatus.PENDING;
            System.out.println("Warning: Invalid status '" + statusString + "' found, defaulting to PENDING");
        }

        String transportationType = rs.getString("transportationType");
        Timestamp bookingTimestamp = rs.getTimestamp("bookingDate");
        LocalDateTime bookingDate = bookingTimestamp != null ? bookingTimestamp.toLocalDateTime() : LocalDateTime.now();

        Timestamp travelTimestamp = rs.getTimestamp("travelDate");
        LocalDateTime travelDate = travelTimestamp != null ? travelTimestamp.toLocalDateTime() : null;

        String paymentMethod = rs.getString("paymentMethod");
        double transactionFee = rs.getDouble("transactionFee");

        Ticket ticket = new Ticket(ticketId, passengerId, route, price, status, transportationType,
                bookingDate, travelDate, paymentMethod, transactionFee);

        try {
            String trainClass = rs.getString("train_class");
            ticket.setTrainClass(trainClass != null ? trainClass : "ECONOMY");
        } catch (SQLException e) {
            ticket.setTrainClass("ECONOMY");
        }

        return ticket;
    }

    // ====== ADDITIONAL UTILITY METHODS ======

    public boolean cancelTicket() {
        if (!canBeCancelled()) {
            System.out.println("❌ Ticket cannot be cancelled. Current status: " + this.status);
            return false;
        }

        try {
            double refundAmount = calculateRefundAmount();
            this.status = TicketStatus.CANCELLED;
            updateTicketInDatabase();

            System.out.println("✅ Ticket cancelled successfully!");
            System.out.println("   Refund Amount: ৳" + String.format("%.2f", refundAmount));
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Error cancelling ticket: " + e.getMessage());
            return false;
        }
    }

    public boolean validateForTravel() {
        if (this.status != TicketStatus.CONFIRMED) {
            System.out.println("❌ Invalid ticket status for travel: " + this.status);
            return false;
        }

        if (this.travelDate != null && this.travelDate.isBefore(LocalDateTime.now().minusHours(1))) {
            System.out.println("❌ Ticket has expired");
            return false;
        }

        System.out.println("✅ Ticket is valid for travel");
        return true;
    }

    public String getTicketSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Ticket #").append(ticketId)
                .append(" | ").append(transportationType)
                .append(" | ").append(route)
                .append(" | ").append(getFormattedPrice())
                .append(" | Status: ").append(status);

        if (trainClass != null && !trainClass.equals("ECONOMY")) {
            summary.append(" | Class: ").append(trainClass);
        }

        return summary.toString();
    }

    // ====== DISPLAY AND UTILITY METHODS ======

    public void displayTicketInfo() {
        System.out.println("=== Ticket Information ===");
        System.out.println("Ticket ID: " + ticketId);
        System.out.println("Passenger ID: " + passengerId);
        System.out.println("Route: " + route);
        System.out.println("Transportation: " + transportationType);
        System.out.println("Price: " + getFormattedPrice());

        if (transactionFee > 0) {
            System.out.println("Transaction Fee: " + getFormattedTransactionFee());
            System.out.println("Total Amount: " + getFormattedTotalAmount());
        }

        System.out.println("Status: " + status);

        if (paymentMethod != null) {
            System.out.println("Payment Method: " + paymentMethod);
        }

        if (seatNumber != null) {
            System.out.println("Seat Number: " + seatNumber);
        }

        if (trainClass != null && !trainClass.equals("ECONOMY")) {
            System.out.println("Train Class: " + trainClass);
        }

        System.out.println("Booking Date: " + bookingDate);

        if (travelDate != null) {
            System.out.println("Travel Date: " + travelDate);
        }

        System.out.println("========================");
    }

    public String getFormattedPrice() {
        return String.format("৳%.2f", price);
    }

    public String getFormattedTotalAmount() {
        return String.format("৳%.2f", getTotalAmountPaid());
    }

    public String getFormattedTransactionFee() {
        return String.format("৳%.2f", transactionFee);
    }

    public double getTotalAmountPaid() {
        return price + transactionFee;
    }

    public boolean canBeCancelled() {
        return status.allowsCancellation();
    }

    public boolean canBeRefunded() {
        return isRefundable && !isCancelled() &&
                (travelDate == null || travelDate.isAfter(LocalDateTime.now().plusHours(24)));
    }

    public boolean isCancelled() {
        return status == TicketStatus.CANCELLED;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public String getFormattedRefundAmount() {
        return String.format("৳%.2f", calculateRefundAmount());
    }

    public double calculateRefundAmount() {
        if (!canBeRefunded()) {
            return 0.0;
        }

        if (travelDate == null || travelDate.isAfter(LocalDateTime.now().plusDays(7))) {
            return getTotalAmountPaid() * 0.9; // 90% refund
        } else if (travelDate.isAfter(LocalDateTime.now().plusHours(24))) {
            return getTotalAmountPaid() * 0.7; // 70% refund
        } else {
            return getTotalAmountPaid() * 0.5; // 50% refund
        }
    }



    public int getTicketId() { return ticketId; }
    public int getPassengerId() { return passengerId; }
    public String getRoute() { return route; }
    public double getPrice() { return price; }
    public TicketStatus getStatus() { return status; }
    public String getTransportationType() { return transportationType; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public LocalDateTime getTravelDate() { return travelDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTransactionFee() { return transactionFee; }
    public String getSeatNumber() { return seatNumber; }
    public boolean isRefundable() { return isRefundable; }
    public Payment getPaymentDetails() { return paymentDetails; }
    public String getTrainClass() { return trainClass; }

    public void setRoute(String route) { this.route = route; }
    public void setPrice(double price) { this.price = price; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public void setTransportationType(String transportationType) { this.transportationType = transportationType; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    public void setTravelDate(LocalDateTime travelDate) { this.travelDate = travelDate; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setTransactionFee(double transactionFee) { this.transactionFee = transactionFee; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public void setRefundable(boolean refundable) { this.isRefundable = refundable; }
    public void setTrainClass(String trainClass) { this.trainClass = trainClass; }
}
