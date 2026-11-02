package com.example.banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ConfirmAccountOnboardingRequest {
    @NotBlank private String challengeId;
    @NotBlank @Pattern(regexp="^\\d{6}$") private String otp;

    public String getChallengeId(){return challengeId;}
    public void setChallengeId(String v){challengeId=v;}
    public String getOtp(){return otp;}
    public void setOtp(String v){otp=v;}
}
