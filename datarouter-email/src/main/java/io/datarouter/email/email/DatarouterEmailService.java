/*
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
package io.datarouter.email.email;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.config.DatarouterEmailSettings.DatarouterEmailHostDetails;
import io.datarouter.email.config.DatarouterEmailSettingsProvider;
import io.datarouter.email.dto.DatarouterEmailFileAttachmentDto;
import io.datarouter.email.util.MimeMessageTool;
import io.datarouter.httpclient.client.service.ContextName;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;

@Singleton
public class DatarouterEmailService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterEmailService.class);

	@Inject
	private ContextName contextName;
	@Inject
	private DatarouterEmailSettingsProvider datarouterEmailSettingsProvider;

	// move this out of DatarouterEmailService
	public DatarouterEmailLinkBuilder startLinkBuilder(){
		return new DatarouterEmailLinkBuilder()
				.withProtocol("https")
				.withHostPort(datarouterEmailSettingsProvider.get().emailLinkHostPort.get())
				.withContextPath(contextName.getContextPath());
	}

	@Deprecated
	public void trySend(String fromEmail, String toEmails, String subject, String body){
		trySend(fromEmail, List.of(toEmails), subject, body, false);
	}

	public void trySend(String fromEmail, Collection<String> toEmails, String subject, String body){
		trySend(fromEmail, toEmails, subject, body, false);
	}

	@Deprecated
	public void trySend(String fromEmail, String toEmails, String subject, String body, boolean html){
		trySend(fromEmail, List.of(toEmails), subject, body, html);
	}

	public void trySend(String fromEmail, Collection<String> toEmails, String subject, String body, boolean html){
		try{
			send(fromEmail, toEmails, subject, body, html);
		}catch(MessagingException e){
			logger.error("failed to send email from={} to={}", fromEmail, String.join(",", toEmails), e);
		}
	}

	@Deprecated
	public void send(String fromEmail, String toEmails, String subject, String body, boolean html)
	throws MessagingException{
		send(fromEmail, List.of(toEmails), subject, body, html);
	}

	public void send(String fromEmail, Collection<String> toEmails, String subject, String body, boolean html)
	throws MessagingException{
		sendAndGetMessageId(fromEmail, toEmails, toEmails, subject, body, html, Collections.emptyMap(),
				List.of());
	}

	public Optional<String> sendAndGetMessageId(
			String fromEmail,
			Collection<String> toEmails,
			Collection<String> replyToEmails,
			String subject,
			String body,
			boolean html,
			Map<String,String> headers,
			List<DatarouterEmailFileAttachmentDto> fileAttachmentDtos)
	throws MessagingException{
		if(!datarouterEmailSettingsProvider.get().sendDatarouterEmails.get()){
			return Optional.empty();
		}
		Properties props = new Properties();
		DatarouterEmailHostDetails emailHostDetails = datarouterEmailSettingsProvider.get()
				.getDatarouterEmailHostDetails();
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
			// one or more addresses
			String toRecipients = Scanner.of(toEmails)
					.distinct()
					.sort()
					.collect(Collectors.joining(","));
			InternetAddress[] addresses = InternetAddress.parse(toRecipients);
			message.addRecipients(RecipientType.TO, addresses);
			String replyToEmailsString = Scanner.of(replyToEmails)
					.distinct()
					.sort()
					.collect(Collectors.joining(","));
			message.setReplyTo(InternetAddress.parse(replyToEmailsString));
			message.setSubject(subject);
			headers.entrySet().forEach(entry -> MimeMessageTool.setHeader(message, entry.getKey(), entry.getValue()));

			Multipart multipart = new MimeMultipart();
			MimeBodyPart textBodyPart = new MimeBodyPart();
			String subType = html ? "html" : "plain";
			textBodyPart.setText(body, "UTF-8", subType);
			multipart.addBodyPart(textBodyPart);

			Scanner.of(fileAttachmentDtos)
					.map(MimeMessageTool::buildMimeBodyPartForAttachment)
					.forEach(mimeBodyPart -> MimeMessageTool.addBodyPartToMultipart(multipart, mimeBodyPart));

			message.setContent(multipart);
			transport.sendMessage(message, addresses);
			return Optional.ofNullable(message.getMessageID());
		}
	}

}
