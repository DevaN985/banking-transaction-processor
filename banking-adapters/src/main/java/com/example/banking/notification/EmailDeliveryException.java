package com.example.banking.notification;
public class EmailDeliveryException extends RuntimeException  {
    public EmailDeliveryException(String m,Throwable c) {
        super(m,c);
    }
}
