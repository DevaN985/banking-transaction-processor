package com.example.banking.controller;

import com.example.banking.domain.Transaction;
import com.example.banking.dto.request.*;
import com.example.banking.dto.response.TransferResponse;
import com.example.banking.dto.request.TransactionOtpRequest;
import org.springframework.http.ResponseEntity;
import com.example.banking.dto.response.TransactionOtpStartedResponse;
import com.example.banking.service.TransactionService;
import com.example.banking.service.TransactionAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Inbound REST adapter for account transactions.
 * Business rules and transaction execution remain in application services.
 */
@RestController
@RequestMapping("/api/v1")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionAuthorizationService authorizationService;

    public TransactionController(
            TransactionService transactionService,
            TransactionAuthorizationService authorizationService) {
        this.transactionService = transactionService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/accounts/{id}/deposits")
    public Transaction deposit(
            @PathVariable String id,
            @Valid @RequestBody AmountRequest request) {
        return transactionService.deposit(id, request.getAmount());
    }

    @PostMapping("/accounts/{id}/withdrawals")
    public ResponseEntity<?> withdraw(
            @PathVariable String id,
            @Valid @RequestBody AmountRequest request) {
        if (!authorizationService.requiresOtp(request.getAmount())) {
            return ResponseEntity.ok(transactionService.withdraw(id, request.getAmount()));
        }
        TransactionOtpRequest otpRequest = new TransactionOtpRequest();
        otpRequest.setOperation(TransactionOtpRequest.Operation.WITHDRAWAL);
        otpRequest.setAccountId(id);
        otpRequest.setAmount(request.getAmount());
        return ResponseEntity.accepted().body(authorizationService.requestOtp(otpRequest));
    }

    @PostMapping("/transfers")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) {
        if (!authorizationService.requiresOtp(request.getAmount())) {
            String transactionId = transactionService.transfer(
                    request.getFromAccountId(), request.getToAccountId(), request.getAmount());
            return ResponseEntity.ok(new TransferResponse(transactionId, "SUCCESS"));
        }
        TransactionOtpRequest otpRequest = new TransactionOtpRequest();
        otpRequest.setOperation(TransactionOtpRequest.Operation.TRANSFER);
        otpRequest.setAccountId(request.getFromAccountId());
        otpRequest.setDestinationAccountId(request.getToAccountId());
        otpRequest.setAmount(request.getAmount());
        return ResponseEntity.accepted().body(authorizationService.requestOtp(otpRequest));
    }

    @GetMapping("/accounts/{id}/transactions")
    public List<Transaction> getTransactionHistory(@PathVariable String id) {
        return transactionService.history(id);
    }

    @PostMapping("/transactions/otp/confirm")
    public TransactionConfirmedResponse confirmOtp(
            @Valid @RequestBody TransactionOtpConfirmRequest request) {
        String transactionReference = authorizationService.confirm(request);
        return new TransactionConfirmedResponse("SUCCESS", transactionReference);
    }


}
