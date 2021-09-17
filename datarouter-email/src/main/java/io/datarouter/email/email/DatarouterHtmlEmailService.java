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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.config.DatarouterEmailSettingsProvider;
import io.datarouter.email.html.EmailDto;
import io.datarouter.email.html.J2HtmlDatarouterEmail;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.httpclient.client.service.ServiceName;
import io.datarouter.storage.config.DatarouterSubscribersSupplier;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.setting.DatarouterEmailSubscriberSettings;
import io.datarouter.util.string.StringTool;

@Singleton
public class DatarouterHtmlEmailService{
	private static Logger logger = LoggerFactory.getLogger(DatarouterHtmlEmailService.class);

	@Inject
	private DatarouterEmailFiles files;
	@Inject
	private DatarouterEmailPaths paths;
	@Inject
	private ServiceName serviceName;
	@Inject
	private DatarouterEmailService datarouterEmailService;
	@Inject
	private DatarouterEmailSettingsProvider datarouterEmailSettingsProvider;
	@Inject
	private AdminEmail adminEmail;
	@Inject
	private DatarouterSubscribersSupplier subscribersEmail;
	@Deprecated // push this logic down to each email
	@Inject
	private DatarouterEmailSubscriberSettings subscriberSettings;
	@Inject
	private EnvironmentName environmentName;

	public void trySend(EmailDto email){
		String fromEmail;
		if(email.fromAdmin){
			fromEmail = adminEmail.get();
		}else{
			fromEmail = email.fromEmail;
		}

		List<String> toEmails = email.toEmails;
		if(email.toAdmin){
			toEmails.add(adminEmail.get());
		}
		if(email.toSubscribers){
			if(subscriberSettings.includeSubscribers.get()){
				logger.info("subscribers are not included");
				toEmails.addAll(subscribersEmail.get());
			}
		}
		datarouterEmailService.trySend(fromEmail, toEmails, email.subject, email.content, email.html);
	}

	public void trySendJ2Html(J2HtmlDatarouterEmailBuilder emailBuilder){
		J2HtmlDatarouterEmail email = emailBuilder.build();

		String fromEmail;
		if(email.fromAdmin){
			fromEmail = adminEmail.get();
		}else{
			fromEmail = email.fromEmail;
		}

		List<String> toEmails = email.toEmails;
		if(email.toAdmin){
			toEmails.add(adminEmail.get());
		}
		if(email.toSubscribers){
			if(subscriberSettings.includeSubscribers.get()){
				logger.info("subscribers are not included");
				toEmails.addAll(subscribersEmail.get());
			}
		}
		String bodyString = email.build().render();
		datarouterEmailService.trySend(fromEmail, toEmails, emailBuilder.getSubject(), bodyString, true);
	}

	public DatarouterEmailLinkBuilder startLinkBuilder(){
		return datarouterEmailService.startLinkBuilder();
	}

	public J2HtmlDatarouterEmailBuilder startEmailBuilder(){
		boolean includeLogo = datarouterEmailSettingsProvider.get().includeLogo.get();
		var emailBuilder = new J2HtmlDatarouterEmailBuilder()
				.withWebappName(serviceName.get())
				.withEnvironment(environmentName.get())
				.withIncludeLogo(includeLogo);
		if(includeLogo){
			String logoHref = datarouterEmailService.startLinkBuilder()
					.withLocalPath(paths.datarouter)
					.build();
			emailBuilder
					.withLogoImgSrc(getEmailLogoHref())
					.withLogoHref(logoHref);
			logger.warn("building email for service={}, environment={}, includelogo={}, logoHref={}",
					serviceName.get(),
					environmentName.get(),
					includeLogo,
					logoHref);
		}
		return emailBuilder;
	}

	private String getEmailLogoHref(){
		String configuredHref = datarouterEmailSettingsProvider.get().logoImgSrc.get();
		if(StringTool.notEmpty(configuredHref)){
			return configuredHref;
		}
		return datarouterEmailService.startLinkBuilder()
				.withLocalPath(files.jeeAssets.datarouterLogoPng)
				.build();
	}

}
