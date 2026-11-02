package com.example.banking.notification;
import com.example.banking.notification.EmailProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
@Component public class SmtpEmailProvider implements EmailProvider  {
    private final JavaMailSender sender;
    private final String from;
    public SmtpEmailProvider(JavaMailSender sender,@Value("${app.notification.from:banking-app@localhost}") String from) {
        this.sender=sender;
        this.from=from;
    }
    public void sendEmail(String to,String subject,String body) {
        SimpleMailMessage m=new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(to);
        m.setSubject(subject);
        m.setText(body);
        try {
            sender.send(m);
        }
        catch(MailException e) {
            throw new EmailDeliveryException("Email delivery failed",e);
        }
    }
}
