package com.example.banking.dto.response;
import java.math.BigDecimal;
public class BalanceResponse  {
    private String accountId;
    private BigDecimal balance;
    public BalanceResponse() {
    }
    public BalanceResponse(String id,BigDecimal b) {
        accountId=id;
        balance=b;
    }
    public String getAccountId() {
        return accountId;
    }
    public BigDecimal getBalance() {
        return balance;
    }
}
