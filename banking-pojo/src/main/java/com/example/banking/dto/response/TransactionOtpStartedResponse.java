package com.example.banking.dto.response;
public class TransactionOtpStartedResponse  {
    private String challengeId;
    private String status;
    private String message;
    public TransactionOtpStartedResponse() {
    }
    public TransactionOtpStartedResponse(String id,String status,String message) {
        this.challengeId=id;
        this.status=status;
        this.message=message;
    }
    public String getChallengeId() {
        return challengeId;
    }
    public String getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
}
