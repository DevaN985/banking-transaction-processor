package com.example.banking.dto.request;

import com.example.banking.domain.enums.GovernmentIdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StartAccountOnboardingRequest {
    @NotNull private GovernmentIdType governmentIdType;
    @NotBlank @Size(max = 30) private String governmentIdNumber;

    public GovernmentIdType getGovernmentIdType() { return governmentIdType; }
    public void setGovernmentIdType(GovernmentIdType value) { governmentIdType = value; }
    public String getGovernmentIdNumber() { return governmentIdNumber; }
    public void setGovernmentIdNumber(String value) { governmentIdNumber = value; }
}
