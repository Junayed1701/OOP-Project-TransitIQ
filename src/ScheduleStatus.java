import java.util.HashMap;

public enum ScheduleStatus {
    ON_TIME,
    DELAYED,
    CANCELLED,
    BOARDING,
    DEPARTED;

    private static final HashMap<ScheduleStatus, String> STATUS_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<ScheduleStatus, Boolean> BOARDING_ALLOWED = new HashMap<>();
    private static final HashMap<ScheduleStatus, Boolean> REQUIRES_NOTIFICATION = new HashMap<>();
    private static final HashMap<ScheduleStatus, Integer> DELAY_SEVERITY = new HashMap<>();

    static {
        // Status descriptions
        STATUS_DESCRIPTIONS.put(ON_TIME, "On Time");
        STATUS_DESCRIPTIONS.put(DELAYED, "Delayed");
        STATUS_DESCRIPTIONS.put(CANCELLED, "Cancelled");
        STATUS_DESCRIPTIONS.put(BOARDING, "Boarding");
        STATUS_DESCRIPTIONS.put(DEPARTED, "Departed");

        // Boarding permissions
        BOARDING_ALLOWED.put(ON_TIME, true);
        BOARDING_ALLOWED.put(DELAYED, true);
        BOARDING_ALLOWED.put(CANCELLED, false);
        BOARDING_ALLOWED.put(BOARDING, true);
        BOARDING_ALLOWED.put(DEPARTED, false);

        // Notification requirements
        REQUIRES_NOTIFICATION.put(ON_TIME, false);
        REQUIRES_NOTIFICATION.put(DELAYED, true);
        REQUIRES_NOTIFICATION.put(CANCELLED, true);
        REQUIRES_NOTIFICATION.put(BOARDING, true);
        REQUIRES_NOTIFICATION.put(DEPARTED, true);

        // Delay severity levels
        DELAY_SEVERITY.put(ON_TIME, 0);
        DELAY_SEVERITY.put(DELAYED, 2);
        DELAY_SEVERITY.put(CANCELLED, 5);
        DELAY_SEVERITY.put(BOARDING, 0);
        DELAY_SEVERITY.put(DEPARTED, 0);
    }

    public String getDisplayText() {
        return STATUS_DESCRIPTIONS.getOrDefault(this, "Unknown");
    }

    public boolean allowsBoarding() {
        return BOARDING_ALLOWED.getOrDefault(this, false);
    }

    public boolean requiresNotification() {
        return REQUIRES_NOTIFICATION.getOrDefault(this, false);
    }

    public int getDelaySeverity() {
        return DELAY_SEVERITY.getOrDefault(this, 0);
    }

    public boolean isTerminal() {
        return this == CANCELLED || this == DEPARTED;
    }

    public boolean canTransitionTo(ScheduleStatus newStatus) {
        return switch (this) {
            case ON_TIME -> newStatus == DELAYED || newStatus == BOARDING || newStatus == CANCELLED;
            case DELAYED -> newStatus == BOARDING || newStatus == CANCELLED || newStatus == ON_TIME;
            case BOARDING -> newStatus == DEPARTED || newStatus == CANCELLED;
            case CANCELLED -> false; // Terminal state
            case DEPARTED -> false;  // Terminal state
        };
    }
}
