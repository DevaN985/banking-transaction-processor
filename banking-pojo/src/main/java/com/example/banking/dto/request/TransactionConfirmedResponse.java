package com.example.banking.dto.request;

public class TransactionConfirmedResponse {
        private String status;
        private String transactionReference;

        public TransactionConfirmedResponse() {
        }

        public TransactionConfirmedResponse(String status, String transactionReference) {
            this.status = status;
            this.transactionReference = transactionReference;
        }

        public String getStatus() {
            return status;
        }

        public String getTransactionReference() {
            return transactionReference;
        }
    }