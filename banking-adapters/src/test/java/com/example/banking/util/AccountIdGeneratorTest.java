package com.example.banking.util;

import com.example.banking.service.TransactionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountIdGeneratorTest {
    @Test
    void returnsGeneratedIdWhenNoCollision() {
        TransactionService service = mock(TransactionService.class);
        when(service.accountExists(anyString())).thenReturn(false);
        String id = new AccountIdGenerator(service).generate();
        assertTrue(id.matches("\\d{15}"));
        verify(service).accountExists(id);
    }

    @Test
    void retriesCollisionsAndReturnsNextAvailableId() {
        TransactionService service = mock(TransactionService.class);
        when(service.accountExists(anyString())).thenReturn(true, false);
        String id = new AccountIdGenerator(service).generate();
        assertTrue(id.matches("\\d{15}"));
        verify(service, times(2)).accountExists(anyString());
    }

    @Test
    void failsAfterTenCollisions() {
        TransactionService service = mock(TransactionService.class);
        when(service.accountExists(anyString())).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> new AccountIdGenerator(service).generate());
        verify(service, times(10)).accountExists(anyString());
    }
}
