import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;

public enum ReportType {
    FINANCIAL,
    PASSENGER_STATS,
    VEHICLE_UTILIZATION,
    DELAY_ANALYSIS;

    private static final HashSet<ReportType> SENSITIVE_REPORTS = new HashSet<>();
    private static final HashSet<AdminRole> AUTHORIZED_ROLES = new HashSet<>();
    private static final HashMap<ReportType, String> TEMPLATE_PATHS = new HashMap<>();
    private static final HashMap<ReportType, List<String>> DEFAULT_COLUMNS = new HashMap<>();

    static {
        SENSITIVE_REPORTS.add(FINANCIAL);

        AUTHORIZED_ROLES.add(AdminRole.SYSTEM_ADMIN);
        AUTHORIZED_ROLES.add(AdminRole.FINANCE_OFFICER);

        TEMPLATE_PATHS.put(FINANCIAL, "/templates/financialReportTemplate");
        TEMPLATE_PATHS.put(PASSENGER_STATS, "/templates/passengerStatsReportTemplate");
        TEMPLATE_PATHS.put(VEHICLE_UTILIZATION, "/templates/vehicleUtilizationReportTemplate");
        TEMPLATE_PATHS.put(DELAY_ANALYSIS, "/templates/delayAnalysisReportTemplate");

        DEFAULT_COLUMNS.put(FINANCIAL, Arrays.asList("Date", "Amount (৳)", "Transaction Type", "Status"));
        DEFAULT_COLUMNS.put(PASSENGER_STATS, Arrays.asList("Passenger ID", "Name", "Route", "Date", "Fare (৳)"));
        DEFAULT_COLUMNS.put(VEHICLE_UTILIZATION, Arrays.asList("Vehicle ID", "Route", "Utilization %", "Revenue (৳)"));
        DEFAULT_COLUMNS.put(DELAY_ANALYSIS, Arrays.asList("Route", "Delay Duration", "Reason", "Impact"));
    }

    public boolean requiresSensitiveData() {
        return SENSITIVE_REPORTS.contains(this);
    }

    public String getTemplatePath() {
        return TEMPLATE_PATHS.getOrDefault(this, "/templates/defaultTemplate");
    }

    public List<String> getDefaultColumns() {
        return DEFAULT_COLUMNS.getOrDefault(this, Arrays.asList("ID", "Description", "Date"));
    }

    public boolean isAvailableTo(AdminRole role) {
        return AUTHORIZED_ROLES.contains(role) || role.getAccessLevel() >= 3;
    }
}
