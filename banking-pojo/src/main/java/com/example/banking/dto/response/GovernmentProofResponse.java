package com.example.banking.dto.response;
import com.example.banking.domain.GovernmentProof;
import com.example.banking.domain.enums.GovernmentIdType;
import java.time.Instant;
import java.time.LocalDate;
public class GovernmentProofResponse  {
    private String proofReference;
    private GovernmentIdType governmentIdType;
    private String maskedGovernmentIdNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String validationStatus;
    private Instant createdAt;
    public GovernmentProofResponse(GovernmentProof proof)  {
        proofReference = proof.getId();
        governmentIdType = proof.getGovernmentIdType();
        String number = proof.getGovernmentIdNumber();
        maskedGovernmentIdNumber = number.length() <= 4 ? "****" : "*".repeat(number.length() - 4) + number.substring(number.length() - 4);
        firstName = proof.getFirstName();
        middleName = proof.getMiddleName();
        lastName = proof.getLastName();
        dateOfBirth = proof.getDateOfBirth();
        validationStatus = proof.getValidationStatus();
        createdAt = proof.getCreatedAt();
    }
    public String getProofReference()  {
        return proofReference;
    }
    public GovernmentIdType getGovernmentIdType()  {
        return governmentIdType;
    }
    public String getMaskedGovernmentIdNumber()  {
        return maskedGovernmentIdNumber;
    }
    public String getFirstName()  {
        return firstName;
    }
    public String getMiddleName()  {
        return middleName;
    }
    public String getLastName()  {
        return lastName;
    }
    public LocalDate getDateOfBirth()  {
        return dateOfBirth;
    }
    public String getValidationStatus()  {
        return validationStatus;
    }
    public Instant getCreatedAt()  {
        return createdAt;
    }
}
