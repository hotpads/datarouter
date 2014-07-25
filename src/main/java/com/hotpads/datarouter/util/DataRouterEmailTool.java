package com.hotpads.datarouter.util;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.core.ExceptionTool;

public class DataRouterEmailTool{
	protected static Logger logger = LoggerFactory.getLogger(DataRouterEmailTool.class);
	
	protected static Properties fMailServerConfig = new Properties();

//	static{
//		fetchConfig();
//	}
//
//	public static void refreshConfig(){
//		fMailServerConfig.clear();
//		fetchConfig();
//	}
//
//	protected static void fetchConfig(){
//		InputStream input = null;
//		try{
//			// If possible, one should try to avoid hard-coding a path in this
//			// manner; in a web application, one should place such a file in
//			// WEB-INF, and access it using ServletContext.getResourceAsStream.
//			// Another alternative is Class.getResourceAsStream.
//			// This file contains the javax.mail config properties mentioned above.
//			input = new FileInputStream("C:\\Temp\\MyMailServer.txt");
//			fMailServerConfig.load(input);
//		}catch(IOException ex){
//			System.err.println("Cannot open and load mail server properties file.");
//		}finally{
//			try{
//				if(input != null) input.close();
//			}catch(IOException ex){
//				System.err.println("Cannot close mail server properties file.");
//			}
//		}
//	}

	public static void sendEmail(String fromEmail, String toEmail, String subject, String body){
		Session session = Session.getDefaultInstance(fMailServerConfig, null);
		MimeMessage message = new MimeMessage(session);
		try{
			message.setFrom(new InternetAddress(fromEmail));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			message.setSubject(subject);
			message.setText(body);
			Transport.send(message);
		}catch(MessagingException ex){
			logger.error(ExceptionTool.getStackTraceAsString(ex));
		}
	}
	
	public static void sendHtmlEmail(String fromEmail, String toEmail, String subject, String body) throws MessagingException {
		Session session = Session.getDefaultInstance(fMailServerConfig, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromEmail));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
		message.setSubject(subject);
		message.setContent(body, "text/html");
		Transport.send(message);
	}

	public static void trySendHtmlEmail(String fromEmail, String toEmail, String subject, String body) {
		try {
			sendHtmlEmail(fromEmail, toEmail, subject, body);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public static void sendEmail(String fromEmail, List<String> toEmails, String subject, String body){
		Session session = Session.getDefaultInstance(fMailServerConfig, null);
		MimeMessage message = new MimeMessage(session);
		try{
			message.setFrom(new InternetAddress(fromEmail));
			for(String email : toEmails){
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			}
			message.setSubject(subject);
			message.setText(body);
			Transport.send(message);
		}catch(MessagingException ex){
			logger.error(ExceptionTool.getStackTraceAsString(ex));
		}
	}

	public static void main(String... aArguments){
		sendEmail("manimal@hotpads.com", "mcorgan@hotpads.com", "Testing 1-2-3", "blah blah blah");
	}
}
