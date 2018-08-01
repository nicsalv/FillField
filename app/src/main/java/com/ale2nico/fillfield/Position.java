/**
 * Rappresentazione di una posizione geografica.
 */
public class Position {

    private String address;

    private Integer streetNumber;

    private Integer postCode;

    private String province;

    private String region;

    public Position(String address, Integer streetNumber,
                    Integer postCode, String province, String region) {
        this.address = address;
        this.streetNumber = streetNumber;
        this.postCode = postCode;
        this.province = province;
        this.region = region;
    }

    public String getAddress() {
        return address;
    }

    public Integer getStreetNumber() {
        return streetNumber;
    }

    public Integer getPostCode() {
        return postCode;
    }

    public String getProvince() {
        return province;
    }

    public String getRegion() {
        return region;
    }

}
