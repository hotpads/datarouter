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
package io.datarouter.storage.util;

import java.util.Properties;

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
		InternetAddress[] addresses = InternetAddress.parse(toEmail);//one or more addresses
		message.addRecipients(RecipientType.TO, addresses);
		message.setReplyTo(addresses);
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
