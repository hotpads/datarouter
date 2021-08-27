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
package io.datarouter.email.util;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class MimeMessageTool{

	public static void setHeader(MimeMessage message, String key, String value){
		try{
			message.setHeader(key, value);
		}catch(MessagingException e){
			throw new RuntimeException(e);
		}
	}

	public static MimeBodyPart buildMimeBodyPartForAttachment(String fileName, File file){
		try{
			MimeBodyPart attachmentBodyPart = new MimeBodyPart();
			attachmentBodyPart.attachFile(file);
			attachmentBodyPart.setFileName(fileName);
			return attachmentBodyPart;
		}catch(MessagingException e){
			throw new RuntimeException(e);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static void addBodyPartToMultipart(Multipart multipart, MimeBodyPart mimeBodyPart){
		try{
			multipart.addBodyPart(mimeBodyPart);
		}catch(MessagingException e){
			throw new RuntimeException(e);
		}
	}

}