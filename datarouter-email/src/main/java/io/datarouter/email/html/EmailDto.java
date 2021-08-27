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
package io.datarouter.email.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

// TODO reduce duplicate code with this and J2HtmlDatarouterEmailBuilder
public class EmailDto{

	public final String subject;
	public final String content;

	public final String fromEmail;
	public final boolean fromAdmin;

	public final List<String> toEmails;
	public final boolean toAdmin;
	public final boolean toSubscribers;

	public final boolean html;

	public EmailDto(
			String subject,
			String content,

			String fromEmail,
			boolean fromAdmin,

			List<String> toEmails,
			boolean toAdmin,
			boolean toSubscribers,

			boolean html){
		this.subject = subject;
		this.content = content;

		this.fromEmail = fromEmail;
		this.fromAdmin = fromAdmin;

		this.toEmails = toEmails;
		this.toAdmin = toAdmin;
		this.toSubscribers = toSubscribers;

		this.html = html;
	}

	public static class EmailDtoBuilder{

		private String subject;
		private String content;

		private String fromEmail = null;
		private boolean fromAdmin = false;

		private List<String> toEmails = new ArrayList<>();
		private boolean toAdmin = false;
		private boolean toSubscribers = false;

		private boolean html;

		public EmailDtoBuilder withSubject(String subject){
			this.subject = subject;
			return this;
		}

		public EmailDtoBuilder withContent(String content, boolean html){
			this.content = content;
			this.html = html;
			return this;
		}

		public EmailDtoBuilder from(String fromEmail){
			this.fromEmail = fromEmail;
			return this;
		}

		public EmailDtoBuilder from(String fromEmail, String fromEmailAlt, Supplier<Boolean> condition){
			if(condition.get()){
				this.fromEmail = fromEmail;
			}else{
				this.fromEmail = fromEmailAlt;
			}
			return this;
		}

		public EmailDtoBuilder fromAdmin(){
			this.fromEmail = null;
			this.fromAdmin = true;
			return this;
		}

		public EmailDtoBuilder fromAdmin(String fromEmailAlt, Supplier<Boolean> condition){
			if(condition.get()){
				this.fromEmail = null;
				this.fromAdmin = true;
			}else{
				this.fromEmail = fromEmailAlt;
			}
			return this;
		}

		public EmailDtoBuilder to(Collection<String> tos){
			toEmails.addAll(tos);
			return this;
		}

		public EmailDtoBuilder to(String toEmail){
			toEmails.add(toEmail);
			return this;
		}

		public EmailDtoBuilder to(String toEmail, Supplier<Boolean> condition){
			if(condition.get()){
				toEmails.add(toEmail);
			}
			return this;
		}

		public EmailDtoBuilder to(String toEmail, boolean condition){
			return to(toEmail, () -> condition);
		}

		public EmailDtoBuilder to(Collection<String> toEmails, boolean condition){
			if(condition){
				toEmails.addAll(toEmails);
			}
			return this;
		}

		public EmailDtoBuilder to(String toEmail, String toEmailAlt, Supplier<Boolean> condition){
			if(condition.get()){
				toEmails.add(toEmail);
			}else{
				toEmails.add(toEmailAlt);
			}
			return this;
		}

		public EmailDtoBuilder toSubscribers(){
			this.toSubscribers = true;
			return this;
		}

		public EmailDtoBuilder toSubscribers(boolean condition){
			if(condition){
				this.toSubscribers = true;
			}
			return this;
		}

		public EmailDtoBuilder toAdmin(){
			this.toAdmin = true;
			return this;
		}

		public EmailDtoBuilder toAdmin(boolean condition){
			if(condition){
				this.toAdmin = true;
			}
			return this;
		}

		public EmailDto build(){
			return new EmailDto(
					subject,
					content,

					fromEmail,
					fromAdmin,

					toEmails,
					toAdmin,
					toSubscribers,
					html);
		}

	}

}
