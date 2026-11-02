package com.example.banking.dto.response;

import com.example.banking.domain.Account;
import com.example.banking.domain.Customer;
import java.math.BigDecimal;

/** Detailed account view including current balance and linked customer profile. */
public class AccountDetailsResponse {
    private String accountId;
    private BigDecimal balance;
    private CustomerDetails customer;

    public AccountDetailsResponse() {}

    public AccountDetailsResponse(String accountId, BigDecimal balance, CustomerDetails customer) {
        this.accountId = accountId;
        this.balance = balance;
        this.customer = customer;
    }

    public static AccountDetailsResponse from(Account account, Customer customer) {
        return new AccountDetailsResponse(
                account.id(), account.balance(),
                customer == null ? null : CustomerDetails.from(customer));
    }

    public String getAccountId() { return accountId; }
    public BigDecimal getBalance() { return balance; }
    public CustomerDetails getCustomer() { return customer; }

    public static class CustomerDetails {
        private String customerId;
        private String firstName;
        private String middleName;
        private String lastName;
        private String fullName;
        private String email;
        private String mobileNumber;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private java.time.LocalDate dateOfBirth;
        private java.time.Instant customerSince;

        public CustomerDetails() {}

        static CustomerDetails from(Customer c) {
            CustomerDetails d = new CustomerDetails();
            d.customerId = c.getCustomerId();
            d.firstName = c.getFirstName();
            d.middleName = c.getMiddleName();
            d.lastName = c.getLastName();
            d.fullName = java.util.stream.Stream.of(c.getFirstName(), c.getMiddleName(), c.getLastName())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(java.util.stream.Collectors.joining(" "));
            d.email = c.getEmail();
            d.mobileNumber = c.getMobileNumber();
            d.addressLine1 = c.getAddressLine1();
            d.addressLine2 = c.getAddressLine2();
            d.city = c.getCity();
            d.state = c.getState();
            d.postalCode = c.getPostalCode();
            d.country = c.getCountry();
            d.dateOfBirth = c.getDateOfBirth();
            d.customerSince = c.getCreatedAt();
            return d;
        }

        public String getCustomerId() { return customerId; }
        public String getFirstName() { return firstName; }
        public String getMiddleName() { return middleName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getMobileNumber() { return mobileNumber; }
        public String getAddressLine1() { return addressLine1; }
        public String getAddressLine2() { return addressLine2; }
        public String getCity() { return city; }
        public String getState() { return state; }
        public String getPostalCode() { return postalCode; }
        public String getCountry() { return country; }
        public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
        public java.time.Instant getCustomerSince() { return customerSince; }
    }
}
