package com.example.portal.service;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import static com.example.portal.constant.EmailConstant.*;

@Service
public class EmailService {
	
	public void sendNewPasswordEmail(String firstName, String password, String email) throws MessagingException {
		Message message = createEmail(firstName, password, email);
		Transport transport = (Transport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
		transport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}
	
	private Message createEmail(String firstName, String password, String email) throws MessagingException {
		Message message = new MimeMessage(getEmailSession());
		message.setFrom(new InternetAddress(FROM_EMAIL));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
		message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
		message.setSubject(EMAIL_SUBJECT);
		message.setText("Hello "+ firstName + ", \n \n Your new account password is: " + password + "\n \n The Support Team");
		message.setSentDate(new Date());
		message.saveChanges();
		return message;
	}
	
	private Session getEmailSession() {
		Properties properties = System.getProperties();
		properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
		properties.put(SMTP_AUTH, true);
		properties.put(SMTP_PORT, DEFAULT_PORT);
		properties.put(SMTP_STARTTLS_ENABLE, true);
		properties.put(SMTP_STARTTLS_REQUIRED, true);
		return Session.getInstance(properties, null);
	}
}
