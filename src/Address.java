public class Address {
    private String streetLine1;
    private String streetLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public Address(String line1, String city, String state, String postalCode, String country) {
        this.streetLine1 = line1;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public Address(String line1, String line2, String city, String state, String postalCode, String country) {
        this.streetLine1 = line1;
        this.streetLine2 = line2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public boolean validate() {
        return streetLine1 != null && city != null && state != null && postalCode != null && country != null;
    }

    public String format() {
        return streetLine1 + ", " + (streetLine2 != null ? streetLine2 + ", " : "") + city + ", " + state + " " + postalCode + ", " + country;
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Address address = (Address) other;
        return streetLine1.equals(address.streetLine1) && city.equals(address.city) && state.equals(address.state) && postalCode.equals(address.postalCode) && country.equals(address.country);
    }
}
