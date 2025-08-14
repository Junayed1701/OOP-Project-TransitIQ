public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    NON_BINARY("Non-binary"),
    PREFER_NOT_TO_SAY("Prefer not to say"),
    OTHER("Other");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Gender fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }

        for (Gender gender : values()) {
            if (gender.displayName.equalsIgnoreCase(displayName.trim())) {
                return gender;
            }
        }
        return null;
    }

    public static Gender fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String trimmed = input.trim();

        try {
            return valueOf(trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fromDisplayName(trimmed);
        }
    }

    public boolean requiresCustomInput() {
        return this == OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}



