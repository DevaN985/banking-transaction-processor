package com.example.banking.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
@Entity @Table(name="customers", uniqueConstraints={@UniqueConstraint(name="uk_customer_kyc_ref",columnNames="kyc_reference"),@UniqueConstraint(name="uk_customer_email",columnNames="email")})
public class Customer  {
    @Id @Column(length=36) private String customerId;
    @Column(nullable=false,length=80) private String firstName;
    @Column(length=80) private String middleName;
    @Column(nullable=false,length=80) private String lastName;
    @Column(nullable=false) private LocalDate dateOfBirth;
    @Column(nullable=false,length=254) private String email;
    @Column(length=20) private String mobileNumber;
    @Column(length=120) private String addressLine1;
    @Column(length=120) private String addressLine2;
    @Column(length=60) private String city;
    @Column(length=60) private String state;
    @Column(length=15) private String postalCode;
    @Column(length=2) private String country;
    @Column(nullable=false,length=100) private String kycReference;
    @Column(nullable=false) private Instant createdAt;
    protected Customer() {
    }
    public Customer(String first,String middle,String last,LocalDate dob,String email,String mobile,String a1,String a2,String city,String state,String postal,String country,String kycRef) {
        customerId=UUID.randomUUID().toString();
        firstName=first;
        middleName=middle;
        lastName=last;
        dateOfBirth=dob;
        this.email=email;
        mobileNumber=mobile;
        addressLine1=a1;
        addressLine2=a2;
        this.city=city;
        this.state=state;
        postalCode=postal;
        this.country=country;
        kycReference=kycRef;
        createdAt=Instant.now();
    }
    public String getCustomerId() {
        return customerId;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getMiddleName() {
        return middleName;
    }
    public String getLastName() {
        return lastName;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public String getEmail() {
        return email;
    }
    public String getMobileNumber() {
        return mobileNumber;
    }
    public String getAddressLine1() {
        return addressLine1;
    }
    public String getAddressLine2() {
        return addressLine2;
    }
    public String getCity() {
        return city;
    }
    public String getState() {
        return state;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public String getCountry() {
        return country;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
