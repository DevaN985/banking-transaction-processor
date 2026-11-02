package com.example.banking.notification;

/** Application port for sending notifications without depending on SMTP. */
public interface EmailProvider {

    void sendEmail(String recipient, String subject, String body);
}
