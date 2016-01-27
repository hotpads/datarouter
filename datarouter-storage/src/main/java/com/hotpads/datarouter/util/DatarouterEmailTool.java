package com.hotpads.datarouter.util;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatarouterEmailTool{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterEmailTool.class);

	private static final Session MAILING_SESSION = Session.getDefaultInstance(new Properties());

	public static void trySendHtmlEmail(String fromEmail, String toEmail, String subject, String body){
		trySendEmail(fromEmail, toEmail, subject, body, true);
	}

	public static void trySendEmail(String fromEmail, String toEmail, String subject, String body){
		trySendEmail(fromEmail, toEmail, subject, body, false);
	}

	private static void trySendEmail(String fromEmail, String toEmail, String subject, String body, boolean html){
		try{
			sendEmail(fromEmail, toEmail, subject, body, html);
		}catch(MessagingException e){
			logger.error("", e);
		}
	}

	private static void sendEmail(String fromEmail, String toEmail, String subject, String body, boolean html)
	throws MessagingException{
		MimeMessage message = new MimeMessage(MAILING_SESSION);
		message.setFrom(new InternetAddress(fromEmail));
		InternetAddress toAddress = new InternetAddress(toEmail);
		message.addRecipient(RecipientType.TO, toAddress);
		message.setReplyTo(new Address[]{toAddress});
		message.setSubject(subject);
		String subType;
		if(html){
			subType = "html";
		}else{
			subType = "plain";
		}
		message.setText(body, "UTF-8", subType);
		Transport.send(message);
	}

}
