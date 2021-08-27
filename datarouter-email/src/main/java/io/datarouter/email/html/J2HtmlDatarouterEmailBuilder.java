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

import io.datarouter.email.type.SimpleEmailType;
import j2html.tags.ContainerTag;

public class J2HtmlDatarouterEmailBuilder{

	private String webappName;
	private String environment;
	private String subject;
	private boolean includeLogo = true;
	private String logoImgSrc;
	private String logoHref;
	private String title;
	private String titleHref;
	private ContainerTag<?> content;

	private String fromEmail = null;
	private boolean fromAdmin = false;

	private List<String> toEmails = new ArrayList<>();
	private boolean toAdmin = false;
	private boolean toSubscribers = false;

	public J2HtmlDatarouterEmailBuilder withWebappName(String webappName){
		this.webappName = webappName;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withEnvironment(String environment){
		this.environment = environment;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withSubject(String subject){
		this.subject = subject;
		return this;
	}

	public String getSubject(){
		return subject == null ? String.format("%s - %s - %s", title, environment, webappName) : subject;
	}

	public J2HtmlDatarouterEmailBuilder withIncludeLogo(boolean includeLogo){
		this.includeLogo = includeLogo;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withLogoImgSrc(String logoImgSrc){
		this.logoImgSrc = logoImgSrc;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withLogoHref(String logoHref){
		this.logoHref = logoHref;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withTitle(String title){
		this.title = title;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withTitleHref(String titleHref){
		this.titleHref = titleHref;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withContent(ContainerTag<?> content){
		this.content = content;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder from(String fromEmail){
		this.fromEmail = fromEmail;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder from(String fromEmail, String fromEmailAlt, Supplier<Boolean> condition){
		if(condition.get()){
			this.fromEmail = fromEmail;
		}else{
			this.fromEmail = fromEmailAlt;
		}
		return this;
	}

	public J2HtmlDatarouterEmailBuilder fromAdmin(){
		this.fromEmail = null;
		this.fromAdmin = true;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder fromAdmin(String fromEmailAlt, Supplier<Boolean> condition){
		if(condition.get()){
			this.fromEmail = null;
			this.fromAdmin = true;
		}else{
			this.fromEmail = fromEmailAlt;
		}
		return this;
	}

	public J2HtmlDatarouterEmailBuilder to(Collection<String> tos){
		toEmails.addAll(tos);
		return this;
	}

	public J2HtmlDatarouterEmailBuilder to(SimpleEmailType simpleEmailType){
		toEmails.addAll(simpleEmailType.tos);
		return this;
	}

	public J2HtmlDatarouterEmailBuilder to(SimpleEmailType simpleEmailType, boolean condition){
		if(condition){
			toEmails.addAll(simpleEmailType.tos);
		}
		return this;
	}

	public J2HtmlDatarouterEmailBuilder to(String toEmail){
		toEmails.add(toEmail);
		return this;
	}

	public J2HtmlDatarouterEmailBuilder to(String toEmail, boolean condition){
		if(condition){
			toEmails.add(toEmail);
		}
		return this;
	}

	public J2HtmlDatarouterEmailBuilder to(Collection<String> toEmails, boolean condition){
		if(condition){
			this.toEmails.addAll(toEmails);
		}
		return this;
	}

	public J2HtmlDatarouterEmailBuilder to(String toEmail, String toEmailAlt, boolean condition){
		if(condition){
			toEmails.add(toEmail);
		}else{
			toEmails.add(toEmailAlt);
		}
		return this;
	}

	public J2HtmlDatarouterEmailBuilder toSubscribers(){
		this.toSubscribers = true;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder toSubscribers(boolean condition){
		if(condition){
			this.toSubscribers = true;
		}
		return this;
	}

	public J2HtmlDatarouterEmailBuilder toAdmin(){
		this.toAdmin = true;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder toAdmin(boolean condition){
		if(condition){
			this.toAdmin = true;
		}
		return this;
	}

	public J2HtmlDatarouterEmail build(){
		return new J2HtmlDatarouterEmail(
				includeLogo,
				logoImgSrc,
				logoHref,
				title,
				titleHref,
				content,

				fromEmail,
				fromAdmin,

				toEmails,
				toAdmin,
				toSubscribers);
	}

}