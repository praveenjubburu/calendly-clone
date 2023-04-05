package com.mountblue.calendly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class GmailServiceImplementation implements GmailService {

    @Autowired
    private JavaMailSender javaMailSender;


    public void sendMail(String toMail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("saiaravind114@gmail.com");
        message.setTo(toMail);
        message.setText("http://localhost:8080/home");
        message.setSubject(subject);
        javaMailSender.send(message);
        System.err.println("Mail sent successFully");

    }

}
