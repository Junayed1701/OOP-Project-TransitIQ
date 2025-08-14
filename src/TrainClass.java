import java.util.HashMap;
import java.util.HashSet;

public enum TrainClass {
    ECONOMY,
    BUSINESS,
    FIRST_CLASS;

    private static final HashMap<TrainClass, Double> PRICE_MULTIPLIERS = new HashMap<>();
    private static final HashSet<TrainClass> PRIORITY_BOARDING = new HashSet<>();

    static {
        PRICE_MULTIPLIERS.put(ECONOMY, 1.0);
        PRICE_MULTIPLIERS.put(BUSINESS, 1.5);
        PRICE_MULTIPLIERS.put(FIRST_CLASS, 2.0);

        PRIORITY_BOARDING.add(FIRST_CLASS);
        PRIORITY_BOARDING.add(BUSINESS);
    }

    public double getPriceMultiplier() {
        return (Double) PRICE_MULTIPLIERS.getOrDefault(this, 1.0);
    }

    public boolean hasPriorityBoarding() {
        return PRIORITY_BOARDING.contains(this);
    }

    public String getDisplayName() {
        return this.name().replace("_", " ");
    }


    public String getDescription() {
        switch (this) {
            case ECONOMY:
                return "Standard seating with basic amenities";
            case BUSINESS:
                return "Enhanced comfort with priority boarding";
            case FIRST_CLASS:
                return "Premium service with luxury amenities";
            default:
                return "Unknown class";
        }
    }

    public boolean isUpgrade(TrainClass other) {
        return this.ordinal() > other.ordinal();
    }

    public static TrainClass fromString(String className) {
        try {
            return TrainClass.valueOf(className.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return ECONOMY;
        }
    }
}
