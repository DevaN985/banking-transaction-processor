package com.example.banking.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    @Test void createsAccountWithZeroOrPositiveOpeningBalance() {
        Account a = new Account("A1", BigDecimal.ZERO);
        assertEquals("A1", a.id()); assertEquals(BigDecimal.ZERO, a.balance());
        Account linked = new Account("A2", new BigDecimal("12.50"), "C1");
        assertEquals("C1", linked.customerId()); assertEquals(new BigDecimal("12.50"), linked.balance());
    }
    @Test void rejectsBlankIdNegativeOrNullOpeningBalance() {
        assertThrows(IllegalArgumentException.class, () -> new Account(" ", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Account("A", new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> new Account("A", null));
        assertThrows(IllegalArgumentException.class, () -> new Account("A", BigDecimal.ZERO, " "));
    }
    @Test void depositAndWithdrawUpdateBalance() {
        Account a = new Account("A", new BigDecimal("100.00"));
        a.deposit(new BigDecimal("10.25")); assertEquals(new BigDecimal("110.25"), a.balance());
        a.withdraw(new BigDecimal("10.25")); assertEquals(new BigDecimal("100.00"), a.balance());
    }
    @Test void rejectsInvalidAmountsAndOverdraftWithoutChangingBalance() {
        Account a = new Account("A", new BigDecimal("10"));
        assertThrows(IllegalArgumentException.class, () -> a.deposit(null));
        assertThrows(IllegalArgumentException.class, () -> a.deposit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> a.withdraw(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> a.withdraw(BigDecimal.ZERO));
        assertThrows(IllegalStateException.class, () -> a.withdraw(new BigDecimal("11")));
        assertEquals(new BigDecimal("10"), a.balance());
    }
}
