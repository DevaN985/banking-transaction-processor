package com.example.banking.dto.response;
public class TransferResponse  {
    private String transferId;
    private String status;
    public TransferResponse() {
    }
    public TransferResponse(String id,String s) {
        transferId=id;
        status=s;
    }
    public String getTransferId() {
        return transferId;
    }
    public String getStatus() {
        return status;
    }
}
