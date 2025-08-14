import java.util.HashMap;
import java.util.HashSet;

public enum BusOperator {
    CITY,
    PRIVATE,
    SCHOOL,
    TOUR;

    private static final HashMap<BusOperator, String> CONTACT_EMAILS = new HashMap<>();
    private static final HashSet<BusOperator> PUBLIC_SERVICES = new HashSet<>();
    private static final HashMap<BusOperator, String> LICENSE_TYPES = new HashMap<>();

    static {
        CONTACT_EMAILS.put(CITY, "city_operator@example.com");
        CONTACT_EMAILS.put(PRIVATE, "private_operator@example.com");
        CONTACT_EMAILS.put(SCHOOL, "school_operator@example.com");
        CONTACT_EMAILS.put(TOUR, "tour_operator@example.com");

        PUBLIC_SERVICES.add(CITY);
        PUBLIC_SERVICES.add(SCHOOL);

        LICENSE_TYPES.put(SCHOOL, "School Bus License");
        LICENSE_TYPES.put(CITY, "General License");
        LICENSE_TYPES.put(PRIVATE, "General License");
        LICENSE_TYPES.put(TOUR, "General License");
    }

    public String getContactEmail() {
        return CONTACT_EMAILS.getOrDefault(this, "");
    }

    public boolean isPublicService() {
        return PUBLIC_SERVICES.contains(this);
    }

    public String getLicenseRequired() {
        return "Required license: " + LICENSE_TYPES.getOrDefault(this, "General License");
    }

    public String getDisplayName() {
        return this.name().replace("_", " ");
    }
}

