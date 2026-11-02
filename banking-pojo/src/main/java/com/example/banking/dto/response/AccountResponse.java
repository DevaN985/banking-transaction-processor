package com.example.banking.dto.response;
import com.example.banking.domain.Account;
import java.math.BigDecimal;
public class AccountResponse  {
    private String accountId;
    private BigDecimal balance;
    public AccountResponse() {
    }
    public AccountResponse(String id,BigDecimal b) {
        accountId=id;
        balance=b;
    }
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.id(),a.balance());
    }
    public String getAccountId() {
        return accountId;
    }
    public BigDecimal getBalance() {
        return balance;
    }
}
