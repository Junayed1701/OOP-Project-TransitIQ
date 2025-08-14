public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    MOBILE_WALLET,
    CRYPTO;

    public boolean supportsRefunds() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PAYPAL;
    }

    public double getTransactionFee() {
        switch (this) {
            case CREDIT_CARD: return 0.025;
            case DEBIT_CARD: return 0.015;
            case PAYPAL: return 0.03;
            case MOBILE_WALLET: return 0.02;
            case CRYPTO: return 0.01;
            default: return 0;
        }
    }

    public boolean requiresAuth() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PAYPAL;
    }

    public String getDisplayName() {
        switch (this) {
            case CREDIT_CARD: return "Credit Card";
            case DEBIT_CARD: return "Debit Card";
            case PAYPAL: return "PayPal";
            case MOBILE_WALLET: return "Mobile Wallet";
            case CRYPTO: return "Cryptocurrency";
            default: return this.name().replace("_", " ");
        }
    }

    public boolean isDigital() {
        return this == PAYPAL || this == MOBILE_WALLET || this == CRYPTO;
    }

    public boolean isInstant() {
        return this == DEBIT_CARD || this == MOBILE_WALLET;
    }

    public int getProcessingTimeHours() {
        switch (this) {
            case DEBIT_CARD:
            case MOBILE_WALLET: return 0;
            case CREDIT_CARD: return 24;
            case PAYPAL: return 2;
            case CRYPTO: return 1;
            default: return 0;
        }
    }

    public boolean requiresVerification() {
        return this == CRYPTO || this == MOBILE_WALLET;
    }

    public double getMinimumAmount() {
        switch (this) {
            case CRYPTO: return 1200.0;
            case MOBILE_WALLET: return 120.0;
            default: return 60.0;
        }
    }

    public double getMaximumAmount() {
        switch (this) {
            case MOBILE_WALLET: return 60000.0;
            case DEBIT_CARD: return 120000.0;
            case CREDIT_CARD: return 600000.0;
            case PAYPAL: return 1200000.0;
            case CRYPTO: return Double.MAX_VALUE;
            default: return 120000.0;
        }
    }
}

