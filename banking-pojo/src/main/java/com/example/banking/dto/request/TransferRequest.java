package com.example.banking.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class TransferRequest  {
    @NotBlank @Size(max=100) private String fromAccountId;
    @NotBlank @Size(max=100) private String toAccountId;
    @NotNull @DecimalMin(value="0.0001") @Digits(integer=15,fraction=4) private BigDecimal amount;
    public TransferRequest() {
    }
    public String getFromAccountId() {
        return fromAccountId;
    }
    public void setFromAccountId(String v) {
        fromAccountId=v;
    }
    public String getToAccountId() {
        return toAccountId;
    }
    public void setToAccountId(String v) {
        toAccountId=v;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal v) {
        amount=v;
    }
}
