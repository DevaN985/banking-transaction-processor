package com.example.banking.dto.request;
import jakarta.validation.constraints.*;
public class AddressRequest  {
    @NotBlank @Size(max=120) private String addressLine1;
    @Size(max=120) private String addressLine2;
    @NotBlank @Size(max=60) private String city;
    @NotBlank @Size(max=60) private String state;
    @NotBlank @Size(max=15) private String postalCode;
    @NotBlank @Pattern(regexp="^[A-Z]{2}$") private String country;
    public AddressRequest() {
    }
    public String getAddressLine1() {
        return addressLine1;
    }
    public void setAddressLine1(String v) {
        addressLine1=v;
    }
    public String getAddressLine2() {
        return addressLine2;
    }
    public void setAddressLine2(String v) {
        addressLine2=v;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String v) {
        city=v;
    }
    public String getState() {
        return state;
    }
    public void setState(String v) {
        state=v;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String v) {
        postalCode=v;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String v) {
        country=v;
    }
}
