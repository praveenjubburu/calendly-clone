package com.mountblue.calendly.service;

import org.springframework.stereotype.Service;

@Service
public interface GmailService {

    public void sendMail(String toMail, String subject, String body);

}