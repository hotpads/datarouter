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
import java.util.List;

import io.datarouter.email.html.EmailDto;
import io.datarouter.email.html.J2HtmlDatarouterEmail;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// Injected support lives in datarouter-web
@Singleton
public class IDatarouterEmailService{

	@Inject
	private EmailService emailService;
	@Inject
	private DatarouterEmailFiles files;
	@Inject
	private DatarouterEmailPaths paths;

	public SendEmailRecipients trySend(
			EmailDto email,
			String adminEmail,
			boolean includeSubscribers,
			Collection<String> subscribers){
		String fromEmail;
		if(email.fromAdmin){
			fromEmail = adminEmail;
		}else{
			fromEmail = email.fromEmail;
		}

		List<String> toEmails = email.toEmails;
		if(email.toAdmin){
			toEmails.add(adminEmail);
		}
		if(email.toSubscribers && includeSubscribers){
			toEmails.addAll(subscribers);
		}
		emailService.trySend(fromEmail, toEmails, email.subject, email.content, email.html);
		return new SendEmailRecipients(fromEmail, toEmails);
	}

	public SendEmailRecipients trySendJ2Html(
			J2HtmlDatarouterEmailBuilder emailBuilder,
			String adminEmail,
			boolean includeSubscribers,
			Collection<String> subscribers){
		J2HtmlDatarouterEmail email = emailBuilder.build();

		String fromEmail;
		if(email.fromAdmin){
			fromEmail = adminEmail;
		}else{
			fromEmail = email.fromEmail;
		}

		List<String> toEmails = email.toEmails;
		if(email.toAdmin){
			toEmails.add(adminEmail);
		}
		if(email.toSubscribers && includeSubscribers){
			toEmails.addAll(subscribers);
		}
		String bodyString = email.build().render();
		emailService.trySend(fromEmail, toEmails, emailBuilder.getSubject(), bodyString, true);
		return new SendEmailRecipients(fromEmail, toEmails);
	}

	/**
	 * @deprecated  Use DatarouterEmailLinkClient
	 */
	public DatarouterEmailLinkBuilder startLinkBuilder(String hostPort, String contextPath){
		return new DatarouterEmailLinkBuilder()
				.withProtocol("https")
				.withHostPort(hostPort)
				.withContextPath(contextPath);
	}

	public J2HtmlDatarouterEmailBuilder startEmailBuilderWithOutLogo(
			String serviceName,
			String environmentName){
		return new J2HtmlDatarouterEmailBuilder()
				.withWebappName(serviceName)
				.withEnvironment(environmentName)
				.withIncludeLogo(false);
	}

	public J2HtmlDatarouterEmailBuilder startEmailBuilderWithLogo(
			String hostPort,
			String contextPath,
			String serviceName,
			String environmentName,
			String configuredLogoHref){
		String logoImgSrc;
		if(StringTool.notEmpty(configuredLogoHref)){
			logoImgSrc = configuredLogoHref;
		}else{
			logoImgSrc = startLinkBuilder(hostPort, contextPath)
					.withLocalPath(files.jeeAssets.datarouterLogoPng)
					.build();
		}
		String logoHref = startLinkBuilder(hostPort, contextPath)
				.withLocalPath(paths.datarouter)
				.build();
		return new J2HtmlDatarouterEmailBuilder()
				.withWebappName(serviceName)
				.withEnvironment(environmentName)
				.withIncludeLogo(true)
				.withLogoImgSrc(logoImgSrc)
				.withLogoHref(logoHref);
	}

	public record SendEmailRecipients(
			String from,
			List<String> to){
	}

}
