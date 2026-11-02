package com.example.banking.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TransactionOtpConfirmRequest {
    @NotBlank
    private String challengeId;

    @NotBlank
    private String otp;

    public TransactionOtpConfirmRequest() {
    }

    public String getChallengeId() { return challengeId; }
    public void setChallengeId(String challengeId) { this.challengeId = challengeId; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
