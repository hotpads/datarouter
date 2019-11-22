/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.email;

import java.util.Date;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.test.DatarouterStorageTestNgModuleFactory;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterEmailSettings;
import io.datarouter.web.config.DatarouterEmailSettings.DatarouterEmailHostDetails;

@Singleton
public class DatarouterEmailService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterEmailService.class);

	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterEmailSettings datarouterEmailSettings;

	public DatarouterEmailLinkBuilder startLinkBuilder(){
		return new DatarouterEmailLinkBuilder()
				.withProtocol("https")
				.withHostPort(datarouterEmailSettings.emailLinkHostPort.get())
				.withContextPath(datarouterService.getContextPath());
	}

	public void trySend(String fromEmail, String toEmail, String subject, String body){
		trySend(fromEmail, toEmail, subject, body, false);
	}

	public void trySend(String fromEmail, String toEmail, String subject, String body, boolean html){
		try{
			send(fromEmail, toEmail, subject, body, html);
		}catch(MessagingException e){
			logger.error("failed to send email from={} to={}", fromEmail, toEmail, e);
		}
	}

	public void send(String fromEmail, String toEmail, String subject, String body, boolean html)
	throws MessagingException{
		if(!datarouterEmailSettings.sendDatarouterEmails.get()){
			return;
		}
		Properties props = new Properties();
		DatarouterEmailHostDetails emailHostDetails = datarouterEmailSettings.getDatarouterEmailHostDetails();
		if(StringTool.notNullNorEmpty(emailHostDetails.smtpPassword)){
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
		}
		String host = StringTool.nullIfEmpty(emailHostDetails.smtpHost);
		int port = emailHostDetails.smtpPort;
		String username = StringTool.nullIfEmpty(emailHostDetails.smtpUsername);
		String password = StringTool.nullIfEmpty(emailHostDetails.smtpPassword);
		Session session = Session.getInstance(props);
		try(Transport transport = session.getTransport()){
			transport.connect(host, port, username, password);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromEmail));
			InternetAddress[] addresses = InternetAddress.parse(toEmail);// one or more addresses
			message.addRecipients(RecipientType.TO, addresses);
			message.setReplyTo(addresses);
			message.setSubject(subject);
			String subType = html ? "html" : "plain";
			message.setText(body, "UTF-8", subType);
			transport.sendMessage(message, addresses);
		}
	}

	@Guice(moduleFactory = DatarouterStorageTestNgModuleFactory.class)
	public static class DatarouterEmailServiceIntegrartionTester{

		@Inject
		private DatarouterEmailService datarouterEmailService;
		@Inject
		private DatarouterProperties datarouterProperties;

		@Test
		public void trySendEmailTest(){
			datarouterEmailService.trySend(
					datarouterProperties.getAdministratorEmail(),
					datarouterProperties.getAdministratorEmail(),
					getClass().getName(),
					"Hello there, it's " + new Date(),
					false);
		}

	}

}
