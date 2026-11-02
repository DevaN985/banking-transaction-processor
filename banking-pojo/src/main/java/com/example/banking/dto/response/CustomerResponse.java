package com.example.banking.dto.response;
import com.example.banking.domain.Customer;
public class CustomerResponse  {
    private String customerId,firstName,lastName,email,mobileNumber;
    public CustomerResponse() {
    }
    public CustomerResponse(String id,String f,String l,String e,String m) {
        customerId=id;
        firstName=f;
        lastName=l;
        email=e;
        mobileNumber=m;
    }
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getCustomerId(),c.getFirstName(),c.getLastName(),c.getEmail(),c.getMobileNumber());
    }
    public String getCustomerId() {
        return customerId;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getMobileNumber() {
        return mobileNumber;
    }
}
