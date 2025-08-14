import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.Timer;

public class TransportApp {
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color DANGER_COLOR = new Color(244, 67, 54);
    private static final Color INFO_COLOR = new Color(33, 150, 243);
    private static final Color PURPLE_COLOR = new Color(156, 39, 176);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField phoneField;
    private JLabel messageLabel;
    private User currentUser;
    private List<Route> availableRoutes;
    private List<Bus> availableBuses;
    private List<Train> availableTrains;
    private Map<String, Route> routeMap;
    private Set<String> uniqueLocations;
    private TreeMap<Double, String> priceToRouteId;
    private LinkedList<String> recentSearches;
    private List<Payment> paymentHistory;
    private List<Schedule> scheduleList;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                TransportApp window = new TransportApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TransportApp() {
        Notification.createNotificationsTable();
        Payment.createPaymentsTable();
        initializeData();
        initialize();
    }

    public String getTransportTypeFromTicket(Ticket ticket) {
        return ticket.getTransportationType();
    }

    private void initializeData() {
        availableRoutes = new ArrayList<>();
        availableBuses = new ArrayList<>();
        availableTrains = new ArrayList<>();
        routeMap = new HashMap<>();
        uniqueLocations = new HashSet<>();
        priceToRouteId = new TreeMap<>();
        recentSearches = new LinkedList<>();
        paymentHistory = new ArrayList<>();
        scheduleList = new ArrayList<>();

        // Initialize hardcoded routes for backward compatibility
        Route route1 = new Route(1, "Mirpur-10", "UCSI University Campus", 25.0);
        Route route2 = new Route(2, "Hazrat Shahjalal Airport", "New Market", 45.0);
        Route route3 = new Route(3, "Dhanmondi", "Bashundhara City Mall", 30.0);
        Route route4 = new Route(4, "Uttara", "Old Dhaka", 40.0);
        Route route5 = new Route(5, "Gulshan", "Motijheel", 35.0);
        Route route6 = new Route(6, "Banani", "Sadarghat", 50.0);
        Route route7 = new Route(7, "Wari", "Ramna Park", 20.0);

        availableRoutes.add(route1);
        availableRoutes.add(route2);
        availableRoutes.add(route3);
        availableRoutes.add(route4);
        availableRoutes.add(route5);
        availableRoutes.add(route6);
        availableRoutes.add(route7);

        // Initialize buses and trains
        Bus bus1 = new Bus("B001", 50, "AVAILABLE", 2022, LocalDate.now().minusMonths(6), "City Bus Service");
        Bus bus2 = new Bus("B002", 40, "AVAILABLE", 2021, LocalDate.now().minusMonths(8), "Private Line");
        Bus bus3 = new Bus("B003", 60, "AVAILABLE", 2023, LocalDate.now().minusMonths(3), "City Bus Service");

        bus1.setFuelType(FuelType.ELECTRIC);
        bus1.setHasAirConditioning(true);
        bus2.setFuelType(FuelType.HYBRID);
        bus2.setHasAirConditioning(false);
        bus3.setFuelType(FuelType.CNG);
        bus3.setHasAirConditioning(true);

        availableBuses.add(bus1);
        availableBuses.add(bus2);
        availableBuses.add(bus3);

        Train train1 = new Train("T001", 200, "AVAILABLE", 2023, LocalDate.now().minusMonths(4), TrainClass.BUSINESS, "Express Line");
        Train train2 = new Train("T002", 150, "AVAILABLE", 2020, LocalDate.now().minusMonths(12), TrainClass.ECONOMY, "Local Service");

        train1.setFuelType(FuelType.ELECTRIC);
        train1.setHasAccessibilityRamp(true);
        train2.setFuelType(FuelType.DIESEL);
        train2.setHasAccessibilityRamp(false);

        train1.assignToRoute(route1);
        train2.assignToRoute(route2);

        availableTrains.add(train1);
        availableTrains.add(train2);

        // Build maps and sets
        for (Route route : availableRoutes) {
            routeMap.put("R" + String.format("%03d", route.getRouteId()), route);
            uniqueLocations.add(route.getStartLocation());
            uniqueLocations.add(route.getEndLocation());
            priceToRouteId.put(route.getPrice(), "R" + String.format("%03d", route.getRouteId()));
        }

        initializeSampleSchedules();
    }

    private void initializeSampleSchedules() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            Schedule schedule = new Schedule(
                    i + 1,
                    LocalTime.of(8 + (i * 2), 0),
                    LocalTime.of(9 + (i * 2), 0),
                    today,
                    40 - (i * 5),
                    "ACTIVE"
            );
            scheduleList.add(schedule);
        }
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("TransitIQ - Smart Transport System Bangladesh");
        frame.setBounds(100, 100, 750, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setLocationRelativeTo(null);
        showLoginScreen();
    }

    private void showLoginScreen() {
        frame.getContentPane().removeAll();
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(50, 100, 50, 100));

        JLabel titleLabel = new JLabel("TransitIQ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Bangladesh's Premier Transport Booking Platform");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loginPromptLabel = new JLabel("Login to your account");
        loginPromptLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginPromptLabel.setForeground(PRIMARY_COLOR);
        loginPromptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(loginPromptLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton loginButton = createStyledButton("Login", SUCCESS_COLOR);
        JButton signUpButton = createStyledButton("Sign Up", PRIMARY_COLOR);

        loginButton.addActionListener(e -> handleLogin());
        signUpButton.addActionListener(e -> showSignUpScreen());

        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);

        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        messageLabel = new JLabel(" ");
        messageLabel.setFont(NORMAL_FONT);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageLabel);

        JLabel demoLabel = new JLabel("💡 Demo: Use any username/password to test the system");
        demoLabel.setFont(SMALL_FONT);
        demoLabel.setForeground(Color.GRAY);
        demoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(demoLabel);

        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Login Credentials",
                0, 0, NORMAL_FONT, PRIMARY_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(NORMAL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(NORMAL_FONT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(NORMAL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(NORMAL_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);

        return formPanel;
    }

    private void showSignUpScreen() {
        frame.getContentPane().removeAll();
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel titleLabel = new JLabel("Create New Account - TransitIQ");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel signUpFormPanel = createSignUpFormPanel();
        mainPanel.add(signUpFormPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton createAccountButton = createStyledButton("Create Account", SUCCESS_COLOR);
        JButton backButton = createStyledButton("Back to Login", WARNING_COLOR);

        createAccountButton.addActionListener(e -> handleSignUp());
        backButton.addActionListener(e -> showLoginScreen());

        buttonPanel.add(createAccountButton);
        buttonPanel.add(backButton);

        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        messageLabel = new JLabel(" ");
        messageLabel.setFont(NORMAL_FONT);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageLabel);

        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createSignUpFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Account Information",
                0, 0, NORMAL_FONT, PRIMARY_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        String[] labels = {"Username:", "Email:", "Phone:", "Password:"};
        JTextField[] fields = new JTextField[4];

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(NORMAL_FONT);
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.NONE;
            formPanel.add(label, gbc);

            if (i == 3) {
                passwordField = new JPasswordField(20);
                passwordField.setFont(NORMAL_FONT);
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
                fields[i] = passwordField;
            } else {
                fields[i] = new JTextField(20);
                fields[i].setFont(NORMAL_FONT);
                fields[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            }

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(fields[i], gbc);
        }

        usernameField = fields[0];
        emailField = fields[1];
        phoneField = fields[2];

        return formPanel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.brighter());
                button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setBorder(null);
            }
        });

        return button;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields.", DANGER_COLOR);
            return;
        }

        User user = User.authenticateUser(username, password);
        if (user != null) {
            currentUser = user;
            showMessage("Login successful! Welcome, " + user.getName() + "!", SUCCESS_COLOR);
            Timer timer = new Timer(1500, e -> showMainMenu());
            timer.setRepeats(false);
            timer.start();
        } else {
            showMessage("Invalid username or password. Please try again.", DANGER_COLOR);
        }
    }

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields.", DANGER_COLOR);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showMessage("Please enter a valid email address.", DANGER_COLOR);
            return;
        }

        if (phone.length() < 10) {
            showMessage("Please enter a valid phone number.", DANGER_COLOR);
            return;
        }

        boolean success = User.registerUser(username, email, phone, password, "English");
        if (success) {
            showMessage("Account created successfully! You can now login.", SUCCESS_COLOR);
            Timer timer = new Timer(2000, e -> showLoginScreen());
            timer.setRepeats(false);
            timer.start();
        } else {
            showMessage("Username already exists. Please choose a different username.", DANGER_COLOR);
        }
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }

    private void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.setSize(800, 700);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createEnhancedHeader();
        JPanel menuPanel = createEnhancedMenuPanel();
        JPanel statusPanel = createStatusBar();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createEnhancedHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(750, 100));
        headerPanel.setLayout(new BorderLayout());

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(PRIMARY_COLOR);

        JLabel welcomeLabel = new JLabel("Welcome to TransitIQ, " + currentUser.getName() + "!");
        welcomeLabel.setFont(TITLE_FONT);
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(PRIMARY_COLOR);

        JButton profileButton = createStyledButton("Profile", INFO_COLOR);
        JButton logoutButton = createStyledButton("Logout", DANGER_COLOR);

        profileButton.addActionListener(e -> showUserProfile());
        logoutButton.addActionListener(e -> handleLogout());

        actionPanel.add(profileButton);
        actionPanel.add(logoutButton);

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createEnhancedMenuPanel() {
        JPanel menuPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        menuPanel.setBackground(BACKGROUND_COLOR);
        menuPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        String[] buttonTexts = {
                "View Routes", "Book Ticket", "My Bookings",
                "Routes by Price", "All Locations", "Search Routes",
                "Payment History", "My Statistics", "Settings"
        };

        Color[] buttonColors = {
                PRIMARY_COLOR, SUCCESS_COLOR, INFO_COLOR,
                WARNING_COLOR, PURPLE_COLOR, new Color(76, 175, 80),
                new Color(255, 87, 34), new Color(156, 39, 176), new Color(96, 125, 139)
        };

        ActionListener[] actions = {
                e -> viewAvailableRoutes(),
                e -> bookTicket(),
                e -> showMyBookings(),
                e -> viewRoutesByPrice(),
                e -> viewAllLocations(),
                e -> searchRoutes(),
                e -> showPaymentHistory(),
                e -> showUserStatistics(),
                e -> showSettings()
        };

        for (int i = 0; i < buttonTexts.length; i++) {
            JButton button = createEnhancedMenuButton(buttonTexts[i], buttonColors[i]);
            button.addActionListener(actions[i]);
            menuPanel.add(button);
        }

        return menuPanel;
    }

    private JButton createEnhancedMenuButton(String text, Color backgroundColor) {
        JButton button = new JButton("📍 " + text + " 📍");
        button.setFont(BUTTON_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 90));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.brighter());
                button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setBorder(null);
            }
        });

        return button;
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(250, 250, 250));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel statusLabel = new JLabel("System Online | BDT Currency | " +
                LocalDateTime.now().toString().substring(0, 16));
        statusLabel.setFont(SMALL_FONT);
        statusLabel.setForeground(Color.GRAY);
        statusPanel.add(statusLabel);

        return statusPanel;
    }

    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            currentUser = null;
            paymentHistory.clear();
            frame.setSize(750, 650);
            frame.setLocationRelativeTo(null);
            showLoginScreen();
        }
    }

    private void showUserProfile() {
        StringBuilder profileInfo = new StringBuilder();
        profileInfo.append("User Profile\n\n");
        profileInfo.append("Name: ").append(currentUser.getName()).append("\n");
        profileInfo.append("Email: ").append(currentUser.getEmail()).append("\n");
        profileInfo.append("User ID: ").append(currentUser.getUserId()).append("\n");
        profileInfo.append("Account Status: Active\n");
        profileInfo.append("Member Since: Recent\n");

        List<Ticket> bookings = Ticket.getBookings(currentUser.getUserId());
        profileInfo.append("\nQuick Stats:\n");
        profileInfo.append("Total Bookings: ").append(bookings.size()).append("\n");
        double totalSpent = bookings.stream().mapToDouble(Ticket::getPrice).sum();
        profileInfo.append("Total Spent: ৳").append(String.format("%.2f", totalSpent)).append("\n");

        JTextArea textArea = new JTextArea(profileInfo.toString());
        textArea.setFont(NORMAL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(frame, scrollPane, "User Profile",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewAvailableRoutes() {
        StringBuilder routeInfo = new StringBuilder("Available Routes:\n\n");
        for (Route route : availableRoutes) {
            routeInfo.append(String.format("Route R%03d: %s → %s\n",
                    route.getRouteId(), route.getStartLocation(), route.getEndLocation()));
            routeInfo.append(String.format("Price: ৳%.2f\n\n", route.getPrice()));
        }

        JTextArea textArea = new JTextArea(routeInfo.toString());
        textArea.setFont(SMALL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(frame, scrollPane, "Available Routes",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void bookTicket() {
        String[] transportOptions = {"Bus", "Train"};
        String selectedTransport = (String) JOptionPane.showInputDialog(frame,
                "Select transport type:", "Transport Selection",
                JOptionPane.QUESTION_MESSAGE, null, transportOptions, transportOptions[0]);

        if (selectedTransport == null) return; // User cancelled

        String[] routeOptions = availableRoutes.stream()
                .map(route -> String.format("R%03d: %s → %s | ৳%.2f",
                        route.getRouteId(), route.getStartLocation(), route.getEndLocation(),
                        route.getPrice()))
                .toArray(String[]::new);

        String selectedRoute = (String) JOptionPane.showInputDialog(frame,
                "Select a route to book:",
                "Book Ticket - Route Selection",
                JOptionPane.QUESTION_MESSAGE, null, routeOptions, routeOptions[0]);

        if (selectedRoute != null) {
            String routeIdStr = selectedRoute.substring(1, 4);
            int routeId = Integer.parseInt(routeIdStr);

            Route route = availableRoutes.stream()
                    .filter(r -> r.getRouteId() == routeId)
                    .findFirst()
                    .orElse(null);

            if (route != null) {
                double finalPrice = route.getPrice();
                TrainClass selectedClass = TrainClass.ECONOMY; // Default

                if (selectedTransport.equals("Train")) {
                    selectedClass = selectTrainClass(route.getPrice());
                    if (selectedClass == null) return; // User cancelled
                    finalPrice = route.getPrice() * selectedClass.getPriceMultiplier();
                }

                if (showRouteDetailsConfirmation(route, finalPrice, selectedTransport, selectedClass)) {
                    PaymentMethod selectedPaymentMethod = showEnhancedPaymentMethodSelection(route, finalPrice);
                    if (selectedPaymentMethod != null) {
                        processEnhancedBookingWithPayment(route, selectedPaymentMethod, selectedTransport, selectedClass, finalPrice);
                    }
                }
            }
        }
    }

    private TrainClass selectTrainClass(double basePrice) {
        StringBuilder classInfo = new StringBuilder("Train Class Selection\n\n");
        classInfo.append("Base Route Price: ৳").append(String.format("%.2f", basePrice)).append("\n\n");

        TrainClass[] classes = TrainClass.values();
        String[] classOptions = new String[classes.length];

        for (int i = 0; i < classes.length; i++) {
            TrainClass trainClass = classes[i];
            double classPrice = basePrice * trainClass.getPriceMultiplier();
            classOptions[i] = String.format("%s - %s\n" +
                            " Price: ৳%.2f (%.1fx multiplier)\n" +
                            " Features: %s%s",
                    trainClass.getDisplayName(),
                    trainClass.getDescription(),
                    classPrice,
                    trainClass.getPriceMultiplier(),
                    trainClass.getDescription(),
                    trainClass.hasPriorityBoarding() ? " | Priority Boarding" : ""
            );
        }

        String selectedOption = (String) JOptionPane.showInputDialog(frame,
                classInfo.toString() + "Choose your train class:",
                "Train Class Selection",
                JOptionPane.QUESTION_MESSAGE, null, classOptions, classOptions[0]);

        if (selectedOption != null) {
            // Find the corresponding TrainClass
            for (int i = 0; i < classOptions.length; i++) {
                if (classOptions[i].equals(selectedOption)) {
                    return classes[i];
                }
            }
        }

        return null; // User cancelled
    }

    private boolean showRouteDetailsConfirmation(Route route, double finalPrice, String transport, TrainClass trainClass) {
        StringBuilder details = new StringBuilder();
        details.append("Booking Details Confirmation\n\n");
        details.append("ROUTE INFORMATION\n");
        details.append("Route ID: R").append(String.format("%03d", route.getRouteId())).append("\n");
        details.append("From: ").append(route.getStartLocation()).append("\n");
        details.append("To: ").append(route.getEndLocation()).append("\n");
        details.append("Transport: ").append(transport).append("\n");

        if ("Train".equals(transport)) {
            details.append("\nTRAIN CLASS DETAILS\n");
            details.append("Class: ").append(trainClass.getDisplayName()).append("\n");
            details.append("Description: ").append(trainClass.getDescription()).append("\n");
            details.append("Base Price: ৳").append(String.format("%.2f", route.getPrice())).append("\n");
            details.append("Class Multiplier: ").append(trainClass.getPriceMultiplier()).append("x\n");
            details.append("Final Price: ৳").append(String.format("%.2f", finalPrice)).append("\n");

            if (trainClass.hasPriorityBoarding()) {
                details.append("✓ Priority Boarding Included\n");
            }

            // Show upgrade suggestions
            for (TrainClass otherClass : TrainClass.values()) {
                if (otherClass.isUpgrade(trainClass)) {
                    double upgradePrice = route.getPrice() * otherClass.getPriceMultiplier();
                    double upgradeCost = upgradePrice - finalPrice;
                    details.append(String.format("💡 Upgrade to %s for only ৳%.2f more\n",
                            otherClass.getDisplayName(), upgradeCost));
                }
            }
        } else {
            details.append("Price: ৳").append(String.format("%.2f", finalPrice)).append("\n");
        }

        details.append("\nDo you want to proceed with this booking?");

        int result = JOptionPane.showConfirmDialog(frame, details.toString(),
                "Confirm Booking Details", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        return result == JOptionPane.YES_OPTION;
    }

    private void showBookingConfirmation(Route route, Payment payment, PaymentMethod paymentMethod,
                                         String transport, TrainClass trainClass, double finalPrice) {
        StringBuilder confirmation = new StringBuilder();
        confirmation.append("🎉 Booking Confirmed Successfully!\n\n");
        confirmation.append("BOOKING DETAILS\n");
        confirmation.append("Route: R").append(String.format("%03d", route.getRouteId())).append("\n");
        confirmation.append("From: ").append(route.getStartLocation()).append("\n");
        confirmation.append("To: ").append(route.getEndLocation()).append("\n");
        confirmation.append("Transport: ").append(transport).append("\n");
        confirmation.append("Travel Date: ").append(LocalDate.now().plusDays(1)).append("\n");

        if ("Train".equals(transport)) {
            confirmation.append("\nTRAIN CLASS DETAILS\n");
            confirmation.append("Class: ").append(trainClass.getDisplayName()).append("\n");
            confirmation.append("Service Level: ").append(trainClass.getDescription()).append("\n");
            if (trainClass.hasPriorityBoarding()) {
                confirmation.append("✅ Priority Boarding: Included\n");
            }
        }

        confirmation.append("\nPAYMENT DETAILS\n");
        confirmation.append("Payment Method: ").append(paymentMethod.getDisplayName()).append("\n");

        if ("Train".equals(transport)) {
            confirmation.append("Base Ticket Price: ৳").append(String.format("%.2f", route.getPrice())).append("\n");
            confirmation.append("Class Multiplier: ").append(trainClass.getPriceMultiplier()).append("x\n");
            confirmation.append("Class-adjusted Price: ৳").append(String.format("%.2f", finalPrice)).append("\n");
        } else {
            confirmation.append("Ticket Price: ৳").append(String.format("%.2f", finalPrice)).append("\n");
        }

        confirmation.append("Transaction Fee: ৳").append(String.format("%.2f", payment.getAmount() - finalPrice)).append("\n");
        confirmation.append("Total Paid: ৳").append(String.format("%.2f", payment.getAmount())).append("\n");
        confirmation.append("Payment Status: ").append(payment.getPaymentStatus().getStatusDescription()).append("\n");
        confirmation.append("Transaction ID: ").append(payment.getPaymentId()).append("\n");

        confirmation.append("\nCONFIRMATION\n");
        confirmation.append("Confirmation sent to: ").append(currentUser.getEmail()).append("\n");
        confirmation.append("Processing Time: ").append(paymentMethod.isInstant() ? "Instant" : paymentMethod.getProcessingTimeHours() + " hours").append("\n");
        confirmation.append("Booking Time: ").append(LocalDateTime.now().toString().substring(0, 16)).append("\n\n");

        // Special privileges section
        if ("Train".equals(transport) && trainClass.hasPriorityBoarding()) {
            confirmation.append("🎫 SPECIAL PRIVILEGES\n");
            confirmation.append("• Priority boarding at the station\n");
            confirmation.append("• Express check-in available\n\n");
        }

        confirmation.append("You can view this booking in 'My Bookings' section.\n");
        confirmation.append("Have a great journey! 🚆");

        JTextArea textArea = new JTextArea(confirmation.toString());
        textArea.setFont(NORMAL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(new Color(240, 255, 240));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 450));

        JOptionPane.showMessageDialog(frame, scrollPane,
                "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    private PaymentMethod showEnhancedPaymentMethodSelection(Route route, double ticketPrice) {
        StringBuilder paymentInfo = new StringBuilder();
        paymentInfo.append("Payment Method Selection\n\n");
        paymentInfo.append("Route: ").append(route.getStartLocation()).append(" → ").append(route.getEndLocation()).append("\n");
        paymentInfo.append("Ticket Price: ৳").append(String.format("%.2f", ticketPrice)).append("\n\n");
        paymentInfo.append("Available Payment Methods:\n");
        paymentInfo.append("(Fees and processing times included)\n\n");

        PaymentMethod[] availableMethods = PaymentMethod.values();
        List<String> validOptions = new ArrayList<>();
        List<PaymentMethod> validMethods = new ArrayList<>();

        for (PaymentMethod method : availableMethods) {
            if (ticketPrice >= method.getMinimumAmount() && ticketPrice <= method.getMaximumAmount()) {
                double fee = ticketPrice * method.getTransactionFee();
                double total = ticketPrice + fee;

                String option = String.format("%s\n" +
                                " Transaction Fee: ৳%.2f (%.1f%%)\n" +
                                " Total Amount: ৳%.2f\n" +
                                " Processing: %s\n" +
                                " Features: %s%s",
                        method.getDisplayName(), fee, method.getTransactionFee() * 100, total,
                        method.isInstant() ? "Instant" : method.getProcessingTimeHours() + " hours",
                        method.requiresAuth() ? " Secure Auth" : " No Auth",
                        method.supportsRefunds() ? " Refundable" : " Non-refundable");

                validOptions.add(option);
                validMethods.add(method);
            }
        }

        if (validOptions.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "No payment methods available for this amount.\n" +
                            "Amount: ৳" + String.format("%.2f", ticketPrice),
                    "Payment Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        String[] optionsArray = validOptions.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(frame,
                paymentInfo.toString() + "Choose your preferred payment method:",
                "Payment Method Selection",
                JOptionPane.QUESTION_MESSAGE, null, optionsArray, optionsArray[0]);

        if (selected != null) {
            int selectedIndex = validOptions.indexOf(selected);
            return validMethods.get(selectedIndex);
        }

        return null;
    }

    private void processEnhancedBookingWithPayment(Route route, PaymentMethod paymentMethod, String transportType, TrainClass trainClass, double finalPrice) {
        double transactionFee = finalPrice * paymentMethod.getTransactionFee();
        double totalAmount = finalPrice + transactionFee;

        Payment payment = new Payment(
                paymentHistory.size() + 1,
                paymentMethod,
                totalAmount,
                PaymentStatus.PENDING,
                LocalDateTime.now(),
                "BDT"
        );

        boolean paymentSuccess = payment.processPayment();

        if (paymentSuccess && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            paymentHistory.add(payment);

            String searchEntry = route.getStartLocation() + " → " + route.getEndLocation();
            recentSearches.addFirst(searchEntry);
            if (recentSearches.size() > 5) {
                recentSearches.removeLast();
            }

            boolean bookingSuccess = Ticket.addBooking(currentUser.getUserId(),
                    route.getStartLocation() + " to " + route.getEndLocation(),
                    totalAmount, transportType, trainClass.name());

            if (bookingSuccess) {
                showBookingConfirmation(route, payment, paymentMethod, transportType, trainClass, finalPrice);
            } else {
                payment.refund(totalAmount);
                JOptionPane.showMessageDialog(frame,
                        "Booking failed after payment. Refund initiated.\n" +
                                "Refund amount: ৳" + String.format("%.2f", totalAmount),
                        "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Payment processing failed. Please try again.\n" +
                            "Payment Status: " + payment.getPaymentStatus().getStatusDescription(),
                    "Payment Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMyBookings() {
        List<Ticket> bookings = Ticket.getBookings(currentUser.getUserId());

        if (bookings.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "No bookings found.\n\nYou haven't made any bookings yet.\nUse 'Book Ticket' to make your first booking!",
                    "My Bookings", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder bookingInfo = new StringBuilder("Your Bookings:\n\n");
        double totalSpent = 0;

        for (int i = 0; i < bookings.size(); i++) {
            Ticket booking = bookings.get(i);
            bookingInfo.append(String.format("%d. Ticket #%d: %s\n",
                    i + 1, booking.getTicketId(), booking.getRoute()));
            bookingInfo.append(String.format(" Price: ৳%.2f | Status: %s\n",
                    booking.getPrice(), booking.getStatus().name()));
            bookingInfo.append(String.format(" Booked: %s\n",
                    booking.getBookingDate().toString().substring(0, 16)));

            if (booking.canBeCancelled()) {
                bookingInfo.append(" Can be cancelled\n");
            }

            if (booking.canBeRefunded()) {
                bookingInfo.append(String.format(" Refund available: %s\n",
                        booking.getFormattedRefundAmount()));
            }

            bookingInfo.append("\n");
            totalSpent += booking.getPrice();
        }

        bookingInfo.append(String.format("Total Amount Spent: ৳%.2f\n", totalSpent));

        if (!recentSearches.isEmpty()) {
            bookingInfo.append("\nRecent Searches:\n");
            for (String search : recentSearches) {
                bookingInfo.append("• ").append(search).append("\n");
            }
        }

        JTextArea textArea = new JTextArea(bookingInfo.toString());
        textArea.setFont(SMALL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 350));

        JOptionPane.showMessageDialog(frame, scrollPane, "My Bookings",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewRoutesByPrice() {
        StringBuilder priceInfo = new StringBuilder("Routes Sorted by Price (Low to High):\n\n");

        for (Map.Entry<Double, String> entry : priceToRouteId.entrySet()) {
            String routeId = entry.getValue();
            Route route = routeMap.get(routeId);
            priceInfo.append(String.format("৳%.2f - %s: %s → %s\n",
                    entry.getKey(), routeId, route.getStartLocation(),
                    route.getEndLocation()));
        }

        JTextArea textArea = new JTextArea(priceInfo.toString());
        textArea.setFont(SMALL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(frame, scrollPane, "Routes by Price",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewAllLocations() {
        StringBuilder locationInfo = new StringBuilder("All Available Locations:\n\n");
        TreeSet<String> sortedLocations = new TreeSet<>(uniqueLocations);
        int count = 1;

        for (String location : sortedLocations) {
            locationInfo.append(String.format("%d. %s\n", count++, location));
        }

        locationInfo.append(String.format("\nStatistics:\n"));
        locationInfo.append(String.format("Total unique locations: %d\n", uniqueLocations.size()));
        locationInfo.append(String.format("Total routes available: %d\n", availableRoutes.size()));

        JTextArea textArea = new JTextArea(locationInfo.toString());
        textArea.setFont(NORMAL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 300));

        JOptionPane.showMessageDialog(frame, scrollPane, "All Locations",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // FIXED SEARCH ROUTES METHOD - Now uses database integration
    private void searchRoutes() {
        String searchLocation = JOptionPane.showInputDialog(frame,
                "Enter a location to search for routes:\n" +
                        "(Search will look in both start and end points)",
                "Route Search", JOptionPane.QUESTION_MESSAGE);

        if (searchLocation != null && !searchLocation.trim().isEmpty()) {
            // Use database search instead of hardcoded routes
            List<Route> searchResults = Route.searchRoutes(searchLocation.trim());

            StringBuilder searchResultsText = new StringBuilder("Search Results for: \"" + searchLocation + "\"\n\n");

            if (searchResults.isEmpty()) {
                searchResultsText.append("No routes found containing: \"").append(searchLocation).append("\"\n\n");
                searchResultsText.append("Tips:\n");
                searchResultsText.append("• Try searching with partial names\n");
                searchResultsText.append("• Check spelling\n");
                searchResultsText.append("• Browse 'All Locations' for available areas");
            } else {
                for (Route route : searchResults) {
                    searchResultsText.append(String.format("Route R%03d: %s → %s\n",
                            route.getRouteId(), route.getStartLocation(), route.getEndLocation()));
                    searchResultsText.append(String.format(" Price: ৳%.2f\n",
                            route.getBaseFare()));
                    if (!route.getStops().isEmpty()) {
                        searchResultsText.append(" Stops: ").append(String.join(", ", route.getStops())).append("\n");
                    }
                    searchResultsText.append("\n");
                }

                // Add to recent searches
                recentSearches.addFirst("Search: " + searchLocation);
                if (recentSearches.size() > 5) {
                    recentSearches.removeLast();
                }
            }

            JTextArea textArea = new JTextArea(searchResultsText.toString());
            textArea.setFont(SMALL_FONT);
            textArea.setEditable(false);
            textArea.setBackground(BACKGROUND_COLOR);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 300));
            JOptionPane.showMessageDialog(frame, scrollPane, "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showPaymentHistory() {
        if (paymentHistory.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "No payment history found.\n\nMake some bookings to see your payment history here!",
                    "Payment History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder historyInfo = new StringBuilder("Payment History:\n\n");
        double totalPaid = 0;

        for (int i = 0; i < paymentHistory.size(); i++) {
            Payment payment = paymentHistory.get(i);
            historyInfo.append(String.format("%d. Transaction #%d\n",
                    i + 1, payment.getPaymentId()));
            historyInfo.append(String.format(" Amount: ৳%.2f\n", payment.getAmount()));
            historyInfo.append(String.format(" Method: %s\n", payment.getPaymentMethod().getDisplayName()));
            historyInfo.append(String.format(" Date: %s\n", payment.getTransactionDate().toString().substring(0, 16)));
            historyInfo.append(String.format(" Status: %s\n\n", payment.getPaymentStatus().getStatusDescription()));
            totalPaid += payment.getAmount();
        }

        historyInfo.append(String.format("Total Paid: ৳%.2f\n", totalPaid));
        historyInfo.append(String.format("Total Transactions: %d", paymentHistory.size()));

        JTextArea textArea = new JTextArea(historyInfo.toString());
        textArea.setFont(SMALL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 350));
        JOptionPane.showMessageDialog(frame, scrollPane, "Payment History",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUserStatistics() {
        List<Ticket> bookings = Ticket.getBookings(currentUser.getUserId());

        StringBuilder stats = new StringBuilder("Your Travel Statistics:\n\n");

        stats.append("BOOKING STATISTICS\n");
        stats.append("Total Bookings: ").append(bookings.size()).append("\n");

        if (!bookings.isEmpty()) {
            double totalSpent = bookings.stream().mapToDouble(Ticket::getPrice).sum();
            double avgSpent = totalSpent / bookings.size();

            stats.append("Total Spent: ৳").append(String.format("%.2f", totalSpent)).append("\n");
            stats.append("Average per Booking: ৳").append(String.format("%.2f", avgSpent)).append("\n");

            Map<TicketStatus, Long> statusCounts = new HashMap<>();
            for (Ticket ticket : bookings) {
                statusCounts.merge(ticket.getStatus(), 1L, Long::sum);
            }

            stats.append("\nTICKET STATUS BREAKDOWN\n");
            for (Map.Entry<TicketStatus, Long> entry : statusCounts.entrySet()) {
                stats.append(entry.getKey().name()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        stats.append("\nPAYMENT STATISTICS\n");
        stats.append("Total Transactions: ").append(paymentHistory.size()).append("\n");

        if (!paymentHistory.isEmpty()) {
            double totalPayments = paymentHistory.stream().mapToDouble(Payment::getAmount).sum();
            stats.append("Total Payments: ৳").append(String.format("%.2f", totalPayments)).append("\n");

            Map<PaymentMethod, Long> methodCounts = new HashMap<>();
            for (Payment payment : paymentHistory) {
                methodCounts.merge(payment.getPaymentMethod(), 1L, Long::sum);
            }

            stats.append("\nPREFERRED PAYMENT METHODS\n");
            for (Map.Entry<PaymentMethod, Long> entry : methodCounts.entrySet()) {
                stats.append(entry.getKey().getDisplayName()).append(": ").append(entry.getValue()).append(" times\n");
            }
        }

        stats.append("\nSEARCH ACTIVITY\n");
        stats.append("Recent Searches: ").append(recentSearches.size()).append("\n");
        if (!recentSearches.isEmpty()) {
            stats.append("Last Search: ").append(recentSearches.getFirst()).append("\n");
        }

        JTextArea textArea = new JTextArea(stats.toString());
        textArea.setFont(SMALL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 400));

        JOptionPane.showMessageDialog(frame, scrollPane, "My Statistics",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettings() {
        StringBuilder settings = new StringBuilder("System Settings & Information:\n\n");

        settings.append("APPLICATION INFO\n");
        settings.append("Name: TransitIQ\n");
        settings.append("Version: 2.0\n");
        settings.append("Country: Bangladesh\n");
        settings.append("Currency: BDT (৳)\n");
        settings.append("Language: English\n\n");

        settings.append("USER PREFERENCES\n");
        settings.append("Username: ").append(currentUser.getName()).append("\n");
        settings.append("Email: ").append(currentUser.getEmail()).append("\n");
        settings.append("Notifications: Enabled\n");
        settings.append("Auto-logout: 30 minutes\n\n");

        settings.append("SYSTEM STATUS\n");
        settings.append("Database: Connected\n");
        settings.append("Payment Gateway: Active\n");
        settings.append("Routes Available: ").append(availableRoutes.size()).append("\n");
        settings.append("Locations Served: ").append(uniqueLocations.size()).append("\n\n");

        settings.append("TIPS\n");
        settings.append("• Book tickets in advance for better prices\n");
        settings.append("• Choose eco-friendly routes to help environment\n");
        settings.append("• Check your bookings regularly for updates\n");
        settings.append("• Use mobile banking for instant payments");

        JTextArea textArea = new JTextArea(settings.toString());
        textArea.setFont(SMALL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 400));

        JOptionPane.showMessageDialog(frame, scrollPane, "Settings & Info",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
