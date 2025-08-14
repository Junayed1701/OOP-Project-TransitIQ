import java.sql.*;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class User {
    protected int userId;
    protected String name;
    protected String email;
    protected char[] password;
    protected String phone;
    protected String preferredLanguage;
    protected Gender gender;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/transport_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zunayed101";

    public User(int userId, String name, String email, char[] password, String phone, String preferredLanguage, Gender gender) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password != null ? password.clone() : null;
        this.phone = phone;
        this.preferredLanguage = preferredLanguage;
        this.gender = gender;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static String hashPassword(char[] password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            if (salt != null) {
                md.update(salt.getBytes());
            }
            md.update(new String(password).getBytes());
            byte[] hashedPassword = md.digest();
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static boolean signUp(String name, String email, char[] password, String phone, String preferredLanguage) {
        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty() ||
                password == null || password.length == 0) {
            System.out.println("All required fields must be provided");
            return false;
        }

        if (emailExists(email)) {
            System.out.println("Email already exists. Please use a different email.");
            return false;
        }

        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        String query = "INSERT INTO users (name, email, password, salt, phone, preferredLanguage, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name.trim());
            stmt.setString(2, email.trim().toLowerCase());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, salt);
            stmt.setString(5, phone);
            stmt.setString(6, preferredLanguage);
            stmt.setString(7, Gender.PREFER_NOT_TO_SAY.toString()); // Default gender

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Sign Up successful! Welcome, " + name);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error during sign up: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Arrays.fill(password, '\0');
        }
        return false;
    }

    public static boolean registerUser(String username, String email, String phone, String password, String language) {
        return signUp(username, email, password.toCharArray(), phone, language);
    }

    private static boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static User signIn(String email, char[] password) {
        if (email == null || email.trim().isEmpty() || password == null || password.length == 0) {
            System.out.println("Email and password are required");
            return null;
        }

        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String salt = rs.getString("salt");
                    boolean passwordMatch = false;

                    if (salt == null) {
                        passwordMatch = storedHash.equals(new String(password));
                    } else {
                        String inputHash = hashPassword(password, salt);
                        passwordMatch = storedHash.equals(inputHash);
                    }

                    if (passwordMatch) {
                        int userId = rs.getInt("userId");
                        String name = rs.getString("name");
                        String phone = rs.getString("phone");
                        String preferredLanguage = rs.getString("preferredLanguage");

                        // Fix: Get gender from database or set default
                        Gender gender = Gender.PREFER_NOT_TO_SAY; // Default value
                        String genderStr = rs.getString("gender");
                        if (genderStr != null) {
                            gender = Gender.fromString(genderStr);
                            if (gender == null) {
                                gender = Gender.PREFER_NOT_TO_SAY;
                            }
                        }

                        User user = new User(userId, name, email, password.clone(), phone, preferredLanguage, gender);
                        System.out.println("Sign In successful! Welcome back, " + name);
                        return user;
                    } else {
                        System.out.println("Invalid email or password.");
                    }
                } else {
                    System.out.println("Invalid email or password.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during sign in: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Arrays.fill(password, '\0');
        }
        return null;
    }


    public static User authenticateUser(String username, String password) {
        return signIn(username, password.toCharArray());
    }

    public void logout() {
        System.out.println(name + " has logged out.");
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    public boolean setLanguagePreference(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return false;
        }

        this.preferredLanguage = languageCode.trim();
        String query = "UPDATE users SET preferredLanguage = ? WHERE userId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, this.preferredLanguage);
            stmt.setInt(2, this.userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating language preference: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProfile(String newName, String newPhone) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }

        String query = "UPDATE users SET name = ?, phone = ? WHERE userId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newName.trim());
            stmt.setString(2, newPhone);
            stmt.setInt(3, this.userId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                this.name = newName.trim();
                this.phone = newPhone;
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(char[] currentPassword, char[] newPassword) {
        if (!validatePassword(currentPassword) || newPassword == null || newPassword.length == 0) {
            return false;
        }

        String salt = generateSalt();
        String hashedNewPassword = hashPassword(newPassword, salt);
        String query = "UPDATE users SET password = ?, salt = ? WHERE userId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedNewPassword);
            stmt.setString(2, salt);
            stmt.setInt(3, this.userId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                Arrays.fill(this.password, '\0');
                this.password = newPassword.clone();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Arrays.fill(currentPassword, '\0');
            Arrays.fill(newPassword, '\0');
        }
        return false;
    }

    public boolean deleteAccount() {
        String query = "DELETE FROM users WHERE userId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.userId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                clearSensitiveData();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error deleting account: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void sendRefundNotification(double amount, String status) {
        String message = String.format("Refund %s: ৳%.2f has been %s for your booking",
                status.toLowerCase(), amount, status.toLowerCase());
        System.out.println("📧 Notification sent to " + this.email + ": " + message);
        Notification refundNotification = new Notification(0, message, "REFUND", LocalDateTime.now());
        refundNotification.sendNotification();
    }

    public static List<Refund> getRefundHistory(int userId) {
        List<Refund> userRefunds = new ArrayList<>();
        List<Ticket> userTickets = Ticket.getBookings(userId);
        for (Ticket ticket : userTickets) {
            try {
                System.out.println("Checking refunds for ticket: " + ticket.getTicketId());
            } catch (Exception e) {
                System.out.println("Error getting refunds for ticket " + ticket.getTicketId());
            }
        }
        return userRefunds;
    }

    public boolean canRequestRefund() {
        List<Ticket> userBookings = Ticket.getBookings(this.userId);
        return userBookings.stream().anyMatch(Ticket::isActive);
    }

    public void displayRefundSummary() {
        List<Refund> refunds = getRefundHistory(this.userId);
        if (refunds.isEmpty()) {
            System.out.println("No refund history found for " + this.name);
            return;
        }

        System.out.println("=== Refund Summary for " + this.name + " ===");
        double totalRefunded = 0;
        for (Refund refund : refunds) {
            System.out.println("Refund ID: " + refund.getRefundId() +
                    " | Amount: " + refund.getFormattedAmount() +
                    " | Status: " + refund.getStatus());
            if (refund.getStatus() == RefundStatus.COMPLETED) {
                totalRefunded += refund.getAmount();
            }
        }
        System.out.println("Total Refunded: ৳" + String.format("%.2f", totalRefunded));
        System.out.println("=====================================");
    }

    public static void initializeDatabase() {
        String createUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    userId INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    salt VARCHAR(255),
                    phone VARCHAR(20),
                    preferredLanguage VARCHAR(10) DEFAULT 'English',
                    gender VARCHAR(20) DEFAULT 'Prefer not to say',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUserTable);
            System.out.println("Database tables initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public String displayInfo() {
        return "User: " + name + ", Email: " + email + ", Gender: " + gender;
    }

    public String getUserType() {
        return "General User";
    }


    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public Gender getGender() { return gender; }

    public boolean validatePassword(char[] inputPassword) {
        if (inputPassword == null || password == null) {
            return false;
        }
        if (inputPassword.length != password.length) {
            return false;
        }
        for (int i = 0; i < password.length; i++) {
            if (password[i] != inputPassword[i]) {
                return false;
            }
        }
        return true;
    }

    public void clearSensitiveData() {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                ", gender=" + gender +
                '}';
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Database connection successful!");
            return true;
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}
