import java.util.HashMap;
import java.util.HashSet;

public enum FuelType {
    DIESEL,
    ELECTRIC,
    HYBRID,
    PETROL,
    CNG;

    private static final HashSet<FuelType> ECO_FRIENDLY_FUELS = new HashSet<>();
    private static final HashMap<FuelType, Integer> CARBON_RATINGS = new HashMap<>();
    private static final HashMap<FuelType, Double> COST_PER_KM = new HashMap<>();

    static {
        ECO_FRIENDLY_FUELS.add(ELECTRIC);
        ECO_FRIENDLY_FUELS.add(HYBRID);

        CARBON_RATINGS.put(DIESEL, 5);
        CARBON_RATINGS.put(ELECTRIC, 1);
        CARBON_RATINGS.put(HYBRID, 3);
        CARBON_RATINGS.put(CNG, 2);
        CARBON_RATINGS.put(PETROL, 4);

        COST_PER_KM.put(DIESEL, 14.40);
        COST_PER_KM.put(ELECTRIC, 6.00);
        COST_PER_KM.put(HYBRID, 9.60);
        COST_PER_KM.put(CNG, 12.00);
        COST_PER_KM.put(PETROL, 15.60);
    }

    public int getCarbonRating() {
        return CARBON_RATINGS.getOrDefault(this, 0);
    }

    public boolean isEcoFriendly() {
        return ECO_FRIENDLY_FUELS.contains(this);
    }

    public double getCostPerKm() {
        return COST_PER_KM.getOrDefault(this, 0.0);
    }

    public String getDisplayName() {
        return this.name().replace("_", " ");
    }
}


