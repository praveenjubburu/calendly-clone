package com.mountblue.calendly;

import com.mountblue.calendly.email.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CalendlyApplication {
	private static EmailSender emailSender;
	@Autowired
	public void setEmailSender(EmailSender emailSender) {
		CalendlyApplication.emailSender = emailSender;
	}

	public static void main(String[] args) {
		SpringApplication.run(CalendlyApplication.class, args);
	}

}

