package com.mountblue.calendly.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.beans.SimpleBeanInfo;

@Service
public class EmailSender {
    @Autowired
    private JavaMailSender mailSender;
    public void sendMail(String toEmail,String subject,String body,String fromEmail)       //update the email system so that it will now send from the from email
    {
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        mailSender.send(message);
    }
    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void sendHtmlMail(String toEmail, String subject, String htmlContent, String fromEmail) throws MessagingException, jakarta.mail.MessagingException {
        if (mailSender == null) {
            throw new MessagingException("mailSender instance is null");
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);
        mailSender.send(message);
    }
}
