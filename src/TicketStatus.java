import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.EnumSet;

public enum TicketStatus {
    CONFIRMED,
    PENDING,
    CANCELLED,
    BOARDED;

    private static final Set<TicketStatus> ACTIVE_STATUSES = EnumSet.of(CONFIRMED, PENDING);
    private static final Set<TicketStatus> CANCELLATION_ALLOWED = EnumSet.of(PENDING, CONFIRMED);
    private static final HashMap<TicketStatus, TicketStatus> NEXT_STATUS = new HashMap<>();

    static {
        NEXT_STATUS.put(PENDING, CONFIRMED);
        NEXT_STATUS.put(CONFIRMED, BOARDED);
    }

    public boolean isActive() {
        return this != TicketStatus.CANCELLED;
    }
    public boolean allowsRefund() {
        return this == CONFIRMED || this == PENDING;
    }

    public double getRefundPercentage() {
        switch (this) {
            case PENDING: return 1.0;    // 100% refund
            case CONFIRMED: return 0.8;  // 80% refund
            case CANCELLED:
            case BOARDED:
            default: return 0.0;         // No refund
        }
    }

    public boolean requiresApproval() {
        return this == CONFIRMED; // Confirmed tickets need admin approval
    }


    public boolean allowsCancellation() {
        return CANCELLATION_ALLOWED.contains(this);
    }

    public TicketStatus getNextStatus() {
        return NEXT_STATUS.get(this);
    }

    public boolean isTerminal() {
        return this == CANCELLED || this == BOARDED;
    }

    public boolean canTransitionTo(TicketStatus targetStatus) {
        if (targetStatus == CANCELLED) {
            return this.allowsCancellation();
        }

        return this.getNextStatus() == targetStatus;
    }

    public Set<TicketStatus> getPossibleTransitions() {
        Set<TicketStatus> transitions = EnumSet.noneOf(TicketStatus.class);

        if (getNextStatus() != null) {
            transitions.add(getNextStatus());
        }

        if (allowsCancellation()) {
            transitions.add(CANCELLED);
        }

        return transitions;
    }

    public String getDescription() {
        return switch (this) {
            case PENDING -> "Awaiting confirmation";
            case CONFIRMED -> "Confirmed and ready for travel";
            case CANCELLED -> "Cancelled";
            case BOARDED -> "Successfully boarded";
        };
    }

    public static Set<TicketStatus> getActiveStatuses() {
        return EnumSet.copyOf(ACTIVE_STATUSES);
    }

    public static Set<TicketStatus> getCancellableStatuses() {
        return EnumSet.copyOf(CANCELLATION_ALLOWED);
    }

    @Override
    public String toString() {
        return this.name();
    }
}
