public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == REFUNDED;
    }

    public boolean allowsModification() {
        return this == PENDING || this == PARTIALLY_REFUNDED;
    }

    public boolean triggersReceipt() {
        return this == COMPLETED;
    }

    public boolean canBeRefunded() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public String getStatusDescription() {
        switch (this) {
            case PENDING: return "Payment is pending processing.";
            case COMPLETED: return "Payment completed successfully.";
            case FAILED: return "Payment failed to process.";
            case REFUNDED: return "Payment has been fully refunded.";
            case PARTIALLY_REFUNDED: return "Payment has been partially refunded.";
            default: return "Unknown payment status.";
        }
    }

    public PaymentStatus[] getNextValidStatuses() {
        switch (this) {
            case PENDING: return new PaymentStatus[]{COMPLETED, FAILED};
            case COMPLETED: return new PaymentStatus[]{REFUNDED, PARTIALLY_REFUNDED};
            case PARTIALLY_REFUNDED: return new PaymentStatus[]{REFUNDED};
            case FAILED:
            case REFUNDED:
            default: return new PaymentStatus[]{};
        }
    }

    public int toStatusCode() {
        return ordinal();
    }

    public static PaymentStatus fromStatusCode(int code) {
        PaymentStatus[] values = values();
        if (code >= 0 && code < values.length) {
            return values[code];
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}