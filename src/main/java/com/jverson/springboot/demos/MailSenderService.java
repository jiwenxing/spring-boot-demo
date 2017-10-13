package com.jverson.springboot.demos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailSenderService {

	@Autowired
	private JavaMailSender mailSender;
	
	public void send(SimpleMailMessage message){
		mailSender.send(message);
	}
	
}
