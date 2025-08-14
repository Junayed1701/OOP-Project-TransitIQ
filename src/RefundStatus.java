import java.util.HashMap;

public enum RefundStatus {
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED,
    CANCELED,
    FAILED;

    private static final HashMap<RefundStatus, String> STATUS_DESCRIPTIONS = new HashMap<>();

    static {
        STATUS_DESCRIPTIONS.put(PENDING, "Refund request is pending.");
        STATUS_DESCRIPTIONS.put(APPROVED, "Refund request has been approved.");
        STATUS_DESCRIPTIONS.put(REJECTED, "Refund request has been rejected.");
        STATUS_DESCRIPTIONS.put(COMPLETED, "Refund has been completed.");
        STATUS_DESCRIPTIONS.put(CANCELED, "Refund request has been canceled.");
        STATUS_DESCRIPTIONS.put(FAILED, "Refund request failed.");
    }

    public String getStatusDescription() {
        return STATUS_DESCRIPTIONS.getOrDefault(this, "Unknown status");
    }

    public boolean canBeModified() {
        return this == PENDING;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == CANCELED || this == FAILED;
    }

    public boolean allowsProcessing() {
        return this == PENDING || this == APPROVED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean requiresAdminAction() {
        return this == PENDING;
    }

    public boolean canTransitionTo(RefundStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == APPROVED || newStatus == REJECTED || newStatus == CANCELED;
            case APPROVED:
                return newStatus == COMPLETED || newStatus == FAILED;
            case REJECTED:
            case COMPLETED:
            case CANCELED:
            case FAILED:
                return false;
            default:
                return false;
        }
    }

    public RefundStatus[] getNextValidStatuses() {
        switch (this) {
            case PENDING:
                return new RefundStatus[]{APPROVED, REJECTED, CANCELED};
            case APPROVED:
                return new RefundStatus[]{COMPLETED, FAILED};
            case REJECTED:
            case COMPLETED:
            case CANCELED:
            case FAILED:
            default:
                return new RefundStatus[]{};
        }
    }

    public int getStatusCode() {
        return ordinal();
    }

    public static RefundStatus fromStatusCode(int code) {
        RefundStatus[] values = values();
        if (code >= 0 && code < values.length) {
            return values[code];
        }
        throw new IllegalArgumentException("Invalid refund status code: " + code);
    }

    @Override
    public String toString() {
        return this.name();
    }
}


