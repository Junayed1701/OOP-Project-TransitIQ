import java.util.HashMap;
import java.util.HashSet;

public enum VehicleStatus {
    AVAILABLE,
    MAINTENANCE,
    OUT_OF_SERVICE;

    private static final HashSet<VehicleStatus> BOOKABLE_STATUSES = new HashSet<>();
    private static final HashSet<VehicleStatus> INSPECTION_REQUIRED = new HashSet<>();
    private static final HashMap<VehicleStatus, String> STATUS_ICONS = new HashMap<>();

    static {
        BOOKABLE_STATUSES.add(AVAILABLE);

        INSPECTION_REQUIRED.add(MAINTENANCE);
        INSPECTION_REQUIRED.add(OUT_OF_SERVICE);

        STATUS_ICONS.put(AVAILABLE, "🟢");
        STATUS_ICONS.put(MAINTENANCE, "🟡");
        STATUS_ICONS.put(OUT_OF_SERVICE, "🔴");
    }

    public boolean canBook() {
        return BOOKABLE_STATUSES.contains(this);
    }

    public String getIcon() {
        return STATUS_ICONS.getOrDefault(this, "⚪");
    }

    public boolean requiresInspection() {
        return INSPECTION_REQUIRED.contains(this);
    }
}