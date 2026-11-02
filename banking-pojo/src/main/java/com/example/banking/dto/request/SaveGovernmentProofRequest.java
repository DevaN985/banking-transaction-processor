package com.example.banking.dto.request;
import com.example.banking.domain.enums.GovernmentIdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
/** Basic government-proof details submitted by the customer. */
public class SaveGovernmentProofRequest  {
    @NotNull private GovernmentIdType governmentIdType;
    @NotBlank @Size(max = 30) private String governmentIdNumber;
    @NotBlank @Size(max = 80) private String firstName;
    @Size(max = 80) private String middleName;
    @NotBlank @Size(max = 80) private String lastName;
    @NotNull @Past private LocalDate dateOfBirth;
    @jakarta.validation.constraints.Email @Size(max=254) private String registeredEmail;
    public GovernmentIdType getGovernmentIdType()  {
        return governmentIdType;
    }
    public void setGovernmentIdType(GovernmentIdType governmentIdType)  {
        this.governmentIdType = governmentIdType;
    }
    public String getGovernmentIdNumber()  {
        return governmentIdNumber;
    }
    public void setGovernmentIdNumber(String governmentIdNumber)  {
        this.governmentIdNumber = governmentIdNumber;
    }
    public String getFirstName()  {
        return firstName;
    }
    public void setFirstName(String firstName)  {
        this.firstName = firstName;
    }
    public String getMiddleName()  {
        return middleName;
    }
    public void setMiddleName(String middleName)  {
        this.middleName = middleName;
    }
    public String getLastName()  {
        return lastName;
    }
    public void setLastName(String lastName)  {
        this.lastName = lastName;
    }
    public String getRegisteredEmail() { return registeredEmail; }
    public void setRegisteredEmail(String registeredEmail) { this.registeredEmail = registeredEmail; }
    public LocalDate getDateOfBirth()  {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth)  {
        this.dateOfBirth = dateOfBirth;
    }
}
