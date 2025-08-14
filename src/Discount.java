import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Discount {
    private int discountId;
    private String type;
    private double percentage;
    private LocalDate validUntil;
    private boolean isActive;

    public Discount(int discountId, String type, double percentage, LocalDate validUntil) {
        this.discountId = discountId;
        this.type = type;
        this.percentage = percentage;
        this.validUntil = validUntil;
        this.isActive = true;
    }

    public double applyDiscount(double originalPrice) {
        if (!isValidDiscount() || originalPrice < 0) {
            return originalPrice;
        }
        double discountedPrice = originalPrice * (1 - (percentage / 100));
        return Math.max(0, discountedPrice);
    }

    public boolean isValidDiscount() {
        return isActive &&
                percentage > 0 &&
                percentage <= 100 &&
                (validUntil == null || !validUntil.isBefore(LocalDate.now()));
    }

    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDate.now());
    }

    public double calculateSavings(double originalPrice) {
        if (!isValidDiscount() || originalPrice < 0) {
            return 0;
        }
        return originalPrice * (percentage / 100);
    }

    public void deactivate() {
        isActive = false;
    }

    public void activate() {
        isActive = true;
    }

    public void displayDiscountInfo() {
        System.out.println("Discount ID: " + discountId);
        System.out.println("Type: " + type);
        System.out.println("Percentage: " + percentage + "%");
        System.out.println("Valid Until: " + (validUntil != null ?
                validUntil.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "No expiry"));
        System.out.println("Status: " + (isActive ? "Active" : "Inactive"));
        System.out.println("Valid: " + (isValidDiscount() ? "Yes" : "No"));
    }

    public int getDiscountId() {
        return discountId;
    }

    public String getType() {
        return type;
    }

    public double getPercentage() {
        return percentage;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public boolean isActive() {
        return isActive;
    }
}

