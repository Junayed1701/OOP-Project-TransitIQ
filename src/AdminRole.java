import java.util.HashMap;
import java.util.HashSet;

public enum AdminRole {
    SYSTEM_ADMIN,
    TRANSPORT_MANAGER,
    FINANCE_OFFICER,
    SUPPORT_STAFF;

    private static final HashSet<AdminRole> SCHEDULE_ACCESS_ROLES = new HashSet<>();
    private static final HashSet<AdminRole> REFUND_APPROVAL_ROLES = new HashSet<>();
    private static final HashMap<AdminRole, Integer> ACCESS_LEVELS = new HashMap<>();

    static {
        SCHEDULE_ACCESS_ROLES.add(SYSTEM_ADMIN);
        SCHEDULE_ACCESS_ROLES.add(TRANSPORT_MANAGER);

        REFUND_APPROVAL_ROLES.add(SYSTEM_ADMIN);
        REFUND_APPROVAL_ROLES.add(FINANCE_OFFICER);

        ACCESS_LEVELS.put(SYSTEM_ADMIN, 4);
        ACCESS_LEVELS.put(TRANSPORT_MANAGER, 3);
        ACCESS_LEVELS.put(FINANCE_OFFICER, 2);
        ACCESS_LEVELS.put(SUPPORT_STAFF, 1);
    }

    public boolean hasScheduleAccess() {
        return SCHEDULE_ACCESS_ROLES.contains(this);
    }

    public boolean canApproveRefunds() {
        return REFUND_APPROVAL_ROLES.contains(this);
    }

    public int getAccessLevel() {
        return ACCESS_LEVELS.getOrDefault(this, 0);
    }

    public String getDisplayName() {
        return this.name().replace("_", " ").toUpperCase();
    }

    public boolean hasPermission(String permission) {
        switch (permission.toUpperCase()) {
            case "APPROVE_REFUNDS":
                return canApproveRefunds();
            case "SCHEDULE_ACCESS":
                return hasScheduleAccess();
            case "VIEW_REPORTS":
                return canViewReports();
            case "MANAGE_SCHEDULES":
                return canManageSchedules();
            case "SYSTEM_ADMIN":
                return this == SYSTEM_ADMIN;
            default:
                return false;
        }
    }

    public boolean canManageSchedules() {
        return this == SYSTEM_ADMIN || this == TRANSPORT_MANAGER;
    }

    public boolean canViewReports() {
        return this == SYSTEM_ADMIN || this == TRANSPORT_MANAGER || this == FINANCE_OFFICER;
    }

    public boolean canModifyUsers() {
        return this == SYSTEM_ADMIN;
    }

    public boolean canViewFinancialData() {
        return this == SYSTEM_ADMIN || this == FINANCE_OFFICER;
    }

    public boolean canHandleSupport() {
        return true; // All admin roles can handle basic support
    }

    public boolean hasHigherAccessThan(AdminRole otherRole) {
        return this.getAccessLevel() > otherRole.getAccessLevel();
    }

    public String[] getPermissionsList() {
        switch (this) {
            case SYSTEM_ADMIN:
                return new String[]{"ALL_PERMISSIONS", "APPROVE_REFUNDS", "SCHEDULE_ACCESS",
                        "VIEW_REPORTS", "MANAGE_SCHEDULES", "MODIFY_USERS", "FINANCIAL_DATA"};
            case TRANSPORT_MANAGER:
                return new String[]{"SCHEDULE_ACCESS", "VIEW_REPORTS", "MANAGE_SCHEDULES"};
            case FINANCE_OFFICER:
                return new String[]{"APPROVE_REFUNDS", "VIEW_REPORTS", "FINANCIAL_DATA"};
            case SUPPORT_STAFF:
                return new String[]{"HANDLE_SUPPORT"};
            default:
                return new String[]{};
        }
    }

    public String getRoleDescription() {
        switch (this) {
            case SYSTEM_ADMIN:
                return "Full system access with all administrative privileges";
            case TRANSPORT_MANAGER:
                return "Manages transportation schedules and operations";
            case FINANCE_OFFICER:
                return "Handles financial operations including refund approvals";
            case SUPPORT_STAFF:
                return "Provides customer support and basic assistance";
            default:
                return "Unknown role";
        }
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
