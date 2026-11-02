package com.example.banking.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class AmountRequest  {
    @NotNull @DecimalMin(value="0.0001") @Digits(integer=15,fraction=4) private BigDecimal amount;
    public AmountRequest() {
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal v) {
        amount=v;
    }
}
